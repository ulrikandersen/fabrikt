val fabrikt: Configuration by configurations.creating

val generationDir = "$buildDir/generated"
val apiFile = "$projectDir/openapi/api.yaml"

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

val kotlinxSerializationVersion: String by rootProject.extra
val kotlinxDateTimeVersion: String by rootProject.extra
val junitVersion: String by rootProject.extra
val ktorVersion: String by rootProject.extra

dependencies {
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDateTimeVersion")

    // ktor server
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    // ktor test
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-server-auth:$ktorVersion")
    testImplementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    testImplementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.20")

    testImplementation("io.mockk:mockk:1.13.7")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks {
    val generateCode by creating(JavaExec::class) {
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
            "--targets", "controllers",
            "--http-controller-target", "ktor",
            "--serialization-library", "KOTLINX_SERIALIZATION",
            "--http-model-opts", "SEALED_INTERFACES_FOR_ONE_OF",
        )
        dependsOn(":jar")
        dependsOn(":shadowJar")
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
        dependsOn(generateCode)
    }


    withType<Test> {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
    }
}
