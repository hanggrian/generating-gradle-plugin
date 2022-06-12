[![Plugin Portal](https://img.shields.io/maven-metadata/v?label=plugin-portal&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fcom%2Fhendraanggrian%2Fgenerating%2Fcom.hendraanggrian.generating.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/com.hendraanggrian.generating)
[![Travis CI](https://img.shields.io/travis/com/hendraanggrian/generating-gradle-plugin)](https://travis-ci.com/github/hendraanggrian/generating-gradle-plugin)
[![OpenJDK](https://img.shields.io/badge/JDK-1.8+-informational)](https://openjdk.java.net/projects/jdk8)

# Generating Gradle Plugin

Generate Android-like `BuildConfig` and `R` class on any JVM projects.
Currently only supported with **IntelliJ IDEA**.

```java
public final class BuildConfig {
    public static final String NAME = "My App";
    public static final String GROUP = "my.package";
    public static final String VERSION = "1.0";
    public static final String DEBUG = true;
}

public final class R {
    public static final class font {
        public static final String MyriadPro = "/font/MyriadPro.ttf";
        public static final String SegoeUI = "/font/SegoeUI.ttf";
    }
    public static final class layout {
        public static final String main = "/layout/main.fxml";
        public static final String about = "/layout/about.fxml";
    }
}
```

## Download

Using plugins DSL:

```gradle
plugins {
    id('com.hendraanggrian.generating') version "$version"
}
```

Using legacy plugin application:

```gradle
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.hendraanggrian:generating-gradle-plugin:$version")
    }
}

apply plugin: 'com.hendraanggrian.generating'
```

## Usage

### BuildConfig

Modify `BuildConfig` class generation with `generateBuildConfig` task.

```gradle
group 'com.example' // project group
version '1.0'       // project version

tasks.generateBuildConfig {
    packageName.set('my.app') // package name of which R.class will be generated to, default is project group

    appName.set('My App')     // `BuildConfig.NAME` value, default is project name
    groupId.set('my.app')     // `BuildConfig.GROUP` value, default is project group
    version.set('2.0')        // `BuildConfig.VERSION` value, default is project version
    debug.set(true)           // `BuildConfig.DEBUG` value

    // add custom field specifying its name, type, and value
    addField String.class, "myString", "Hello world!"
    addField double.class, "myDecimal", 12.0
}
```

### R

Modify `R` class generation with `generateR` task.

```gradle
group 'com.example' // project group
version '1.0'       // project version

tasks.generateR {
    packageName.set('my.app')
    resourceDirectory.set(new File('my/path/resources'))
    exclusions.addAll('some_file', 'some_other_file')
    configureCss()
    configureJson()
    configureProperties()
}
```
