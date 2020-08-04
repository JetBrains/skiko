## Publishing

##### Publish to Maven local
```
./gradlew publishToMavenLocal
```

##### Publish to `build/repo` directory
```
./gradlew publishToBuildRepo
```

##### Publish to Space
Set up environment variables `SKIKO_SPACE_USERNAME` and `SKIKO_SPACE_KEY`.
```
./gradlew publishToSpace
```

See https://jetbrains.team/p/ui/packages/dev for published packages.

##### Publish to all repositories
```
./gradlew publish
```

##### Customize version
```
./gradlew <PUBLISH_TASK> -Pdeploy.version=0.1.2
```
