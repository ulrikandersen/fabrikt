dependencies {
    add("implementation", libs.kotlin.stdlib.jdk8)
    add("implementation", libs.jakarta.validation.api)
    add("implementation", libs.validation.api)
    add("implementation", platform(libs.jackson.bom))
    add("implementation", libs.jackson.module.kotlin)
    add("implementation", libs.jackson.databind.nullable)
    add("implementation", libs.micronaut.http)
    add("implementation", libs.micronaut.security)
}
