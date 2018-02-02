BuildConfig
===========
`BuildConfig` class for non-Android Java projects.

```gradle
buildconfig {
    groupId 'com.example'
    artifactId 'app'
    version '1.0'
    debug true
}
```

```java
public final class BuildConfig {
    
    public static final String group = "com.example";
    public static final String artifact = "app";
    public static final String version = "1.0";
    public static final String debug = true;
    
    private BuildConfig() {
    }
}
```

Download
--------
```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.hendraanggrian:buildconfig:0.7'
    }
}
```

Generate
--------
Apply `buildconfig` plugin on the project. (not the root project)

```gradle
apply plugin: 'java'
apply plugin: 'buildconfig'

buildconfig {
    ...
}

dependencies {
    ...
}
```

Then run gradle task `buildconfig`, it will automatically read properties files from your resources folder and generate class accordingly.

```
./gradlew buildConfig
```

Custom field
------------
To generate custom fields, simply invoke `field`.

```gradle
buildconfig {
    field(String.class, "myString", "Hello world!")
    field(double.class, "myDecimal", 12.0)
}
```

Customization
-------------
Declare and modify extension `buildconfig`, note that all of properties are optional.

```gradle
apply plugin: 'java'
apply plugin: 'buildconfig'

buildconfig {
    packageName 'com.example'
    srcDir 'src'
}
```

License
-------
    Copyright 2017 Hendra Anggrian

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
