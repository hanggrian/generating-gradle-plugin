[![download](https://api.bintray.com/packages/hendraanggrian/maven/buildconfig-gradle-plugin/images/download.svg)](https://bintray.com/hendraanggrian/maven/buildconfig-gradle-plugin/_latestVersion)
[![build](https://travis-ci.com/hendraanggrian/buildconfig-gradle-plugin.svg)](https://travis-ci.com/hendraanggrian/buildconfig-gradle-plugin)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![license](https://img.shields.io/github/license/hendraanggrian/buildconfig-gradle-plugin)](http://www.apache.org/licenses/LICENSE-2.0)

BuildConfig Gradle Plugin
=========================
Generate Android-like `BuildConfig` class on any JVM projects.
Currently only supported with **IntelliJ IDEA**.

```java
public final class BuildConfig {
    public static final String NAME = "My App";
    public static final String GROUP = "my.package";
    public static final String VERSION = "1.0";
    public static final String DEBUG = true;

    private BuildConfig() {
    }
}
```

Download
--------
Add plugin to buildscript:

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "com.hendraanggrian:buildconfig-gradle-plugin:$version"
    }
}
```

then apply it in your module, along with idea plugin:

```gradle
apply plugin: 'idea'
apply plugin: 'com.hendraanggrian.buildconfig'
```

that's it, `BuildConfig` are now automatically generated after compilation with default behavior.

Usage
-----
Modify `BuildConfig` fields generation with `buildconfig` closure.

```gradle
group 'com.example' // project group
version '1.0'       // project version

tasks.getByName('buildconfig') {
    packageName 'my.app'    // package name of which R.class will be generated to, default is project group

    appName 'My App'        // `BuildConfig.NAME` value, default is project name
    groupId 'my.app'        // `BuildConfig.GROUP` value, default is project group
    version '2.0'           // `BuildConfig.VERSION` value, default is project version
    debug true              // `BuildConfig.DEBUG` value

    // add custom field specifying its name, type, and value
    addField String.class, "myString", "Hello world!"
    addField double.class, "myDecimal", 12.0
}
```
