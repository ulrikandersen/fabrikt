plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.20"
}

val ktorVersion: String by rootProject.extra
val kotlinxDateTimeVersion: String by rootProject.extra
val junitVersion: String by rootProject.extra

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDateTimeVersion")

    testImplementation(testFixtures(project(":end2end-tests:ktor-client-shared-tests")))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation(kotlin("test"))
}

val generationDir = layout.buildDirectory.dir("generated")

sourceSets {
    main { java.srcDirs(generationDir.map { it.dir("src/main/kotlin") }) }
    test { java.srcDirs(generationDir.map { it.dir("src/test/kotlin") }) }
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
        "--serialization-library", "kotlinx_serialization",
        "--validation-library", "no_validation"
    )
    dependsOn(":jar", ":shadowJar")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(generateCode)
}
