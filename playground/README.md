# Fabrikt Playground

The Fabrikt Playground is a web based tools that allows you to play around with 
Fabrikt without installing it locally.

The goal is to make lower the barrier for trying out Fabrikt and to hopefully prove to 
people that Fabrikt can be a useful tool for the user and encourage them to embed it in 
their development workflow either via the CLI or via Gradle/Maven.

## Technical Details

The playground is built with these amazing Open Source libraries ♥️
* [Ktor](https://github.com/ktorio/ktor) for HTTP
* [kotlinx.html](https://github.com/Kotlin/kotlinx.html) for HTML without writing HTML 
* [htmx](https://github.com/bigskysoftware/htmx) for interactivity
* [PrismJS](https://github.com/PrismJS/prism) for syntax highlighting
* [Ace Editor](https://github.com/ajaxorg/ace) for YAML editing
* [Normalize.css](https://github.com/necolas/normalize.css) for default styling
* [BassCSS](https://basscss.com/) for utility CSS

## Developing Locally

Use Ktor's Auto Reload feature when developing locally; this way the server is automatically reloaded when you make 
changes to the code.

In one terminal run:
```shell
./gradlew -t :playground:build
```

In another terminal run:
```shell
./gradlew :playground:run
```

Then point your browser to [http://localhost:8080](http://localhost:8080). The changes you make should now be reflected 
in the browser as soon as Gradle building the changed files.

## Building and Deploying

1. Build the jar 
```shell
gradle :playground:shadowJar
```

2. Build the Docker image

```shell
docker build -t fabrikt-playground .
```

3. Deploy to the PaaS of choice :rocket: