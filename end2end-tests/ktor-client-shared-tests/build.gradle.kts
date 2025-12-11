plugins {
    kotlin("jvm")
    `java-test-fixtures`
}

val ktorVersion: String by rootProject.extra
val junitVersion: String by rootProject.extra

dependencies {
    testFixturesApi("io.ktor:ktor-client-core:$ktorVersion")
    testFixturesApi("io.ktor:ktor-client-cio:$ktorVersion")
    testFixturesApi("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testFixturesApi("io.ktor:ktor-server-test-host:$ktorVersion")
    testFixturesApi("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    testFixturesApi("io.mockk:mockk:1.13.7")
    testFixturesApi("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testFixturesApi("org.wiremock:wiremock:3.3.1")
    testFixturesApi("com.marcinziolo:kotlin-wiremock:2.1.1")
    testFixturesApi(kotlin("test"))
}

val generationDir = layout.buildDirectory.dir("generated")

sourceSets {
    testFixtures {
        java.srcDirs(
            "src/testFixtures/kotlin",
            generationDir.map { it.dir("src/main/kotlin") },
            generationDir.map { it.dir("src/test/kotlin") }
        )
    }
}

val generateCode by tasks.registering(JavaExec::class) {
    val apiFile = "${rootProject.projectDir}/src/test/resources/examples/ktorClient/api.yaml"
    inputs.files(apiFile)
    outputs.dir(generationDir)
    outputs.cacheIf { true }
    classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
    mainClass.set("com.cjbooms.fabrikt.cli.CodeGen")
    args = listOf(
        "--output-directory", generationDir.get().asFile.absolutePath,
        "--base-package", "com.example",
        "--api-file", apiFile,
        "--targets", "http_models",
        "--targets", "client",
        "--http-client-target", "ktor",
        "--serialization-library", "jackson",
        "--validation-library", "no_validation"
    )
    dependsOn(":jar", ":shadowJar")
}

tasks.named("compileTestFixturesKotlin") {
    dependsOn(generateCode)
}