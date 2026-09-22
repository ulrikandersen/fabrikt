dependencies {
    add("implementation", libs.kotlin.stdlib.jdk8)
    add("implementation", libs.jakarta.validation.api)
    add("implementation", libs.validation.api)
    add("implementation", platform(libs.jackson3.bom))
    add("implementation", libs.jackson3.module.kotlin)
    add("implementation", libs.okhttp)
    add("implementation", libs.resilience4j.circuitbreaker)
}
