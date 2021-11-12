In order to simplify working with a complex hierarchical MPP structure,
skiko build defines MPP hierarchy in terms of platforms.

A platform is just an abstract target, which means that 
a platform can extend another platform 
(e.g. both ios and macos platforms extend an abstract darwin os platform).

The platform concept provides the following advantages 
over working with Kotlin targets and source sets directly:
1. A more concise definition of a hierarchy. E.g. instead of
defining `darwinMain`, `darwinTest`, `iosMain`, `iosTest` & setting up
the dependencies between corresponding source sets, one can simply define
two platforms: `darwin` & `ios` and specify that `ios` extends `darwin`.
Each platform automatically configures corresponding `main` and `test` source sets.
2. A more concise and flexible way to use only necessary platforms during development.
Kotlin MPP assumes, that all targets are always configured, but a developer
can choose, which tasks to run. However, enumerating all necessary tasks
can be tricky: for example publishing iosX64 target to maven local requires
running `./gradlew publishKotlinMultiplatformPublicationToMavenLocal publishIosX64PublicationToMavenLocal`.
3. Generally the project becomes more complex & slower to work with, when all targets are always configured.

Specifying the scope of platforms is done via `SKIKO_PLATFORMS` environment variable 
or `skiko.platforms` project property (specified in `gradle.properties` file or as `-Pskiko.platforms=` in CLI):
* Multiple platforms should be separated by commas: `SKIKO_PLATFORMS=jvm,js`;
* If a platform is abstract, the build will configure all platforms, extending it. 
For example, for the following configuration: `native`, `linux` (extends `native`), 
`macos` (extends `native`), requesting `native` also configures `linux` & `macos`.
* A special value `all` can be used to configure all platforms.

To see the list of available platforms, use the `listAvailablePlatforms` task.

Examples:
* Compile one platform:
```
SKIKO_PLATFORMS=jvm ./gradlew assemble
```
* Compile multiple platforms:
```
SKIKO_PLATFORMS=jvm,js ./gradlew assemble
```
* Run tests:
```
SKIKO_PLATFORMS=jvm ./gradlew check
```
* Publish `0.0.0-SNAPSHOT` to Maven Local:
```
SKIKO_PLATFORMS=jvm ./gradlew publishToMavenLocal
```
* Publish custom version to Maven Local:
```
SKIKO_PLATFORMS=jvm ./gradlew publishToMavenLocal -Pdeploy.version=0.1.2 -Pdeploy.release=true
```