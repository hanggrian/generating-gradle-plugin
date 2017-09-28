rsync
=====
Common Java application uses properties files and ResourcesBundle to handle internationalization.
Unfortunately as properties gets bigger, it's easy to misspell or even lost some of their entries.
Inspired by Android's R class, this gradle plugin generates similar class to keep track of those properties' keys.

```java
// say there are these properties
src/main/resources
    \__some.xml
    \__other.xml
    \__integer.properties
        \__one=1
        \__two=2
    \_strings_en.properties
        \__hello=Hello
        \__world=Hello
    \_strings_in.properties
        \__hello=Halo
        \__world=Dunia

// will result in
public final class R {
    public static final class xml {
        public static final String some = "some";
        public static final String other = "other";    
    }
    public static final class integer {
        public static final String one = "one";
        public static final String two = "two";    
    }
    public static final class string {
        public static final String hello = "hello";
        public static final String world = "world";    
    }
}
```

Usage
-----
Apply `rsync` plugin on the project. (not the root project)

```gradle
apply plugin: 'java'
apply plugin: 'rsync'

dependencies {
    ...
}
```

Then simply run gradle task `rsync`,
it will automatically read properties files from your resources folder and generate class accordingly.

```
./gradlew rsync
```

#### Customization
Declare and modify extension rsync, note that all of this is optional.

```gradle
apply plugin: 'java'
apply plugin: 'rsync'

rsync {
    packageName = 'com.example'
    className = 'R'
    pathToResources = 'src/resources'
    ignore = [
        'ignore.properties',
        'another.properties'
    ]
}
```

#### Limitation
Class file overwriting is currently not supported.
Therefore, current class must be deleted before running the task `rsync` again to apply any changes made in properties files.

Download
--------
```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.hendraanggrian:rsync:0.4'
    }
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
