val fabrikt: Configuration by configurations.creating

val generationDir = "$buildDir/generated"

sourceSets {
    main { java.srcDirs("$generationDir/src/main/kotlin") }
    test { java.srcDirs("$generationDir/src/test/kotlin") }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.20"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val jacksonVersion: String by rootProject.extra
val junitVersion: String by rootProject.extra
val ktorVersion: String by rootProject.extra
val kotlinxDateTimeVersion: String by rootProject.extra

dependencies {
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    // ktor client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-resources:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDateTimeVersion")

    // ktor test
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-server-auth:$ktorVersion")
    testImplementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    testImplementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    testImplementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.20")

    testImplementation("io.mockk:mockk:1.13.7")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks {
    val generateCode = createCodeGenerationTask(
        "generateKtorCode",
        "src/test/resources/examples/ktorResources/api.yaml"
    )

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
        dependsOn(generateCode)
    }


    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
    }
}

fun TaskContainer.createCodeGenerationTask(
    name: String, apiFilePath: String, opts: List<String> = emptyList()
): TaskProvider<JavaExec> = register(name, JavaExec::class) {
    val apiFile = "${rootProject.projectDir}/$apiFilePath"
    inputs.files(apiFile)
    outputs.dir(generationDir)
    outputs.cacheIf { true }
    classpath = rootProject.files("./build/libs/fabrikt-${rootProject.version}.jar")
    mainClass.set("com.cjbooms.fabrikt.cli.CodeGen")
    args = listOf(
        "--output-directory", generationDir,
        "--base-package", "com.example",
        "--api-file", apiFile,
        "--targets", "http_models",
        "--targets", "client",
        "--http-client-target", "ktor_routing",
        "--serialization-library", "kotlinx_serialization"
    ) + opts
    dependsOn(":jar")
    dependsOn(":shadowJar")
}
