![Travis-CI Status](https://travis-ci.org/java-native/jssc.svg?branch=master)

A small, single Java library for working with serial ports across various systems based on the work from [scream3r/java-simple-serial-connector](https://github.com/scream3r/java-simple-serial-connector).

### Usage
* [Download `.jar` manually](../../releases) or add using maven
```xml
<dependency>
    <groupId>io.github.java-native</groupId>
    <artifactId>jssc</artifactId>
    <version>2.9.2</version>
</dependency>
```
* or Gradle (KTS)
```gradle
repositories {
    mavenCentral()
}
dependencies {
    implementation("io.github.java-native:jssc:2.9.2")
}
```
* or Gradle (Groovy)
```gradle
repositories {
    mavenCentral()
}
dependencies {
    implementation 'io.github.java-native:jssc:2.9.2'
}
```
* [API code examples](../../wiki/examples)

### Support
* [Report a bug or request a feature](../../issues/new)

### Developers
* [Compile this project from source](../../wiki/compiling)
* [Chat on Slack](https://join.slack.com/t/java-native/shared_invite/zt-7oy9i5j8-yje5mtdLcLBtqhYWcMsDOg)
