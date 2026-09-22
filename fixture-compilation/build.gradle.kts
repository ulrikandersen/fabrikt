import groovy.json.JsonOutput
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") apply false
    kotlin("plugin.serialization") version "2.2.10" apply false
}

data class Fixture(val id: String, val profile: String, val scenario: String, val sources: List<String>)

val examples = rootProject.file("src/test/resources/examples")
val goldenFiles = fileTree(examples) { include("**/*.kt") }.files.sorted()
val fixtures = mutableListOf<Fixture>()
fun addFixture(id: String, profile: String, scenario: String, sources: List<File>) {
    fixtures += Fixture(id, profile, scenario, sources.map { it.relativeTo(examples).invariantSeparatorsPath })
}
fun models(scenario: File) = scenario.resolve("models").listFiles()?.filter { it.extension == "kt" }.orEmpty().sorted()

for ((directory, files) in goldenFiles.groupBy { it.parentFile }) {
    val relative = directory.relativeTo(examples).invariantSeparatorsPath
    val segments = relative.split('/')
    if ("models" in segments) {
        val scenario = segments.takeWhile { it != "models" }.joinToString("/")
        val profile = when {
            scenario == "jackson3" -> "jackson3"
            scenario.startsWith("micronaut") -> "micronaut-models"
            scenario == "quarkusReflectionModels" -> "quarkus-models"
            segments.any { it.startsWith("kotlinx") } || scenario == "kotlinxDateTimeOverrides" -> "kotlinx"
            else -> "jackson"
        }
        addFixture(relative, profile, scenario, files)
    } else if ("controllers" in segments) {
        val scenario = segments.takeWhile { it != "controllers" }.joinToString("/")
        val profile = when (segments.last()) {
            "ktor" -> "ktor-server"
            "micronaut" -> "micronaut"
            "controllers", "spring", "spring-completion-stage" -> "spring"
            else -> error("Unclassified controller fixture: $relative")
        }
        for (file in files.filter { it.name != "TypedApplicationCall.kt" }) {
            addFixture("$relative/${file.nameWithoutExtension}", profile, scenario,
                listOf(file) + files.filter { it.name == "TypedApplicationCall.kt" } + models(examples.resolve(scenario)))
        }
    } else if ("client" in segments) {
        val scenario = segments.takeWhile { it != "client" }.joinToString("/")
        val supportNames = setOf("HttpUtil.kt", "HttpResilience4jUtil.kt", "OAuth.kt", "ApiModels.kt")
        val okhttp = files.filter { it.name in supportNames || it.name in setOf("ApiClient.kt", "ApiService.kt") || segments.last() == "okhttp" }
        if (okhttp.isNotEmpty()) {
            addFixture("$relative/okhttp", if (scenario == "jackson3") "okhttp-jackson3" else "okhttp", scenario,
                okhttp + models(examples.resolve(scenario)))
        }
        if ("ktor" in segments) {
            addFixture(relative, "ktor-client", scenario, files)
        } else {
            for (file in files - okhttp.toSet()) {
                val profile = when {
                    "OpenFeign" in file.name -> "feign"
                    "SpringHttpInterface" in file.name || segments.last() == "spring" -> "spring"
                    else -> error("Unclassified client fixture: $file")
                }
                addFixture("$relative/${file.nameWithoutExtension}", profile, scenario, listOf(file) + models(examples.resolve(scenario)))
            }
        }
    } else {
        error("Unclassified golden directory: $relative")
    }
}

check(fixtures.map { it.profile }.toSet().all { profile -> subprojects.any { it.name == profile } }) {
    "Every fixture dependency profile must have a compilation project registered in settings.gradle.kts"
}

val manifestFile = layout.buildDirectory.file("fixtures.json")
val manifest = JsonOutput.prettyPrint(JsonOutput.toJson(fixtures.map {
    mapOf("id" to it.id, "profile" to it.profile, "scenario" to it.scenario, "sources" to it.sources)
}))
val writeManifest by tasks.registering {
    inputs.property("manifest", manifest)
    outputs.file(manifestFile)
    doLast { manifestFile.get().asFile.apply { parentFile.mkdirs(); writeText(manifest) } }
}
val supportDirectory = layout.buildDirectory.dir("support")
val prepareFixtureSupport by tasks.registering(JavaExec::class) {
    dependsOn(writeManifest, rootProject.tasks.named("testClasses"))
    classpath = rootProject.extensions.getByType<SourceSetContainer>()["test"].runtimeClasspath
    mainClass.set("com.cjbooms.fabrikt.util.FixtureCompilationSupport")
    args(manifestFile.get().asFile, examples, supportDirectory.get().asFile)
    inputs.file(manifestFile)
    inputs.dir(examples)
    outputs.dir(supportDirectory)
}
val verifyFixtureCoverage by tasks.registering {
    val covered = fixtures.flatMap { it.sources }.toSet()
    inputs.files(goldenFiles)
    inputs.property("covered", covered.sorted())
    doLast {
        val actual = fileTree(examples) { include("**/*.kt") }.files
            .map { it.relativeTo(examples).invariantSeparatorsPath }.toSet()
        check(actual == covered) { "Golden compilation coverage mismatch. Missing: ${actual - covered}; stale: ${covered - actual}" }
        logger.lifecycle("Compiling ${actual.size} golden files in ${fixtures.size} isolated fixture compilations.")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    if (name == "kotlinx" || name == "ktor-client") apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    val profile = name
    val kotlinExtension = extensions.getByType<KotlinJvmProjectExtension>()
    kotlinExtension.jvmToolchain(17)
    for (fixture in fixtures.filter { it.profile == profile }) {
        val compilationName = fixture.id.split('/', '-', '.').joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }.replaceFirstChar(Char::lowercaseChar)
        val sourceSet = extensions.getByType<SourceSetContainer>().create(compilationName)
        configurations[sourceSet.implementationConfigurationName].extendsFrom(configurations["implementation"])
        kotlinExtension.sourceSets.named(compilationName) {
            kotlin.setSrcDirs(listOf(examples, supportDirectory.map { it.dir(fixture.id) }))
            kotlin.include(fixture.sources + listOf("**/fixture-support/**/*.kt"))
            if (fixture.scenario == "customTypeMapping") kotlin.srcDir(rootProject.file("fixture-compilation/support"))
        }
        val compile = tasks.named<KotlinCompile>(sourceSet.getCompileTaskName("kotlin")) {
            dependsOn(prepareFixtureSupport)
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
                optIn.add("kotlin.time.ExperimentalTime")
                if (profile == "kotlinx" || profile == "ktor-client") optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }
        tasks.named("check") { dependsOn(compile) }
    }
}

tasks.named("check") {
    dependsOn(verifyFixtureCoverage, subprojects.map { "${it.path}:check" })
}
rootProject.tasks.named("check") { dependsOn(tasks.named("check")) }
