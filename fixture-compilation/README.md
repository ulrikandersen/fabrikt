# Golden fixture compilation

`./gradlew build` compiles every Kotlin golden under `src/test/resources/examples`, in addition to running the existing generator comparisons. Run only the compilation checks with:

```sh
./gradlew :fixture-compilation:check --continue
```

There is one project per dependency profile and one Kotlin compilation per fixture variant. Each compilation has its own output directory and classpath; it cannot resolve classes from another fixture or from Fabrikt's test runtime. Dependency versions are maintained in `gradle/libs.versions.toml`. The kotlinx model and Ktor client profiles enable the Kotlin serialization compiler plugin. Compilation targets JDK 17.

The parent build discovers model directories, controller variants, and client variants automatically. Alternative serializers, suspend clients, response wrappers, and controller implementations compile separately. A coverage check fails if any golden Kotlin file is unclassified. Add new framework profiles explicitly in the parent build and `settings.gradle.kts`; their dependencies belong in their profile's `build.gradle.kts`.

For example, compile just the Spring controllers for `arrays` with:

```sh
./gradlew :fixture-compilation:spring:compileArraysControllersSpringControllersKotlin
```

Some golden snapshots contain only a controller, client, or selected model. `FixtureCompilationSupport` generates the missing companion models and library files into `build/support`, using the fixture's spec and package. Existing golden files are compiled directly and are never rewritten by this check. Ktor clients use generated kotlinx models instead of the alternative Jackson model snapshots. The historical `validationAnnotations/jakarta` snapshot uses its corresponding `jakartaValidationAnnotations` spec for missing companion types. The custom type mapping fixture supplies a real example serializer in `support/`.

Compiler failures are not skipped or accepted as a baseline. A new fixture participates automatically, and a new compilation error fails `check`. Use `--continue` to see failures across all profiles in one run. Gradle skips unchanged compilations on subsequent runs; `--build-cache` can also reuse compilation outputs when a build cache is available.
