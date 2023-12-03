# Release processes

## Teamcity

Trigger a new deployment in [Publish release](https://buildserver.labs.intellij.net/buildConfiguration/Skiko_PublishRelease)
build configuration.
    1. Click "Deploy" button.
    2. Specify the desired version in "Skiko Release Version" text field on the "Parameters" tab.
    3. Choose the desired branch and commit on the "Changes" tab.
    4. Optionally you can check "Put the build to the queue top" option in the "General" tab to speed up a deployment
    (please be mindful about it!).

## Publishing

##### Publish JVM target to Maven local
```bash
./gradlew publishToMavenLocal
```

##### Publish all targets to Maven Local
```bash
./gradlew publishToMavenLocal -Pskiko.native.enabled=true -Pskiko.wasm.enabled=true -Pskiko.android.enabled=true
```
Use flag `-Pskiko.debug=true` to build with debug build type.
Artifact will be published to mavenLocal with postfix "+debug", for example "0.0.0-SNAPSHOT+debug".

##### Publish to `build/repo` directory
```bash
./gradlew publishToBuildRepo
```

##### Publish to Compose repo
Set up environment variables `COMPOSE_REPO_USERNAME` and `COMPOSE_REPO_KEY`.
```bash
./gradlew publishToComposeRepo
```

##### Publish to all repositories
```bash
./gradlew publish
```

##### Publish local version
```bash
./gradlew <PUBLISH_TASK> -Pdeploy.version=0.2.0 -Pdeploy.release=true
```

##### Code signing

macOS for Apple Silicon builds aimed for distribution require mandatory code signing,
so use command like
```bash
./gradlew -Psigner="Apple Distribution: Nikolay Igotti (N462MKSJ7M)" <PUBLISH_TASK>
```
to codesign the JNI library.
Use `security find-identity -v -p codesigning` to find valid signing identities.
