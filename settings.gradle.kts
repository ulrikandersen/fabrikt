rootProject.name = "fabrikt"

include(
    "end2end-tests:okhttp",
    "end2end-tests:openfeign",
    "end2end-tests:ktor-jackson",
    "end2end-tests:models-jackson",
    "end2end-tests:models-kotlinx",
)
