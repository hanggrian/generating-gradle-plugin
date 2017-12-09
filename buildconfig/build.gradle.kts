plugins {
    `java-library`
    kotlin("jvm")
    id("com.novoda.bintray-release")
}

group = bintrayGroup
version = bintrayPublish

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly(gradleApi())
    implementation(kotlin("stdlib", kotlinVersion))
    implementation(javapoet(javapoetVersion))
    testImplementation(junit(junitVersion))
}

publish {
    userOrg = bintrayUser
    groupId = bintrayGroup
    artifactId = bintrayArtifact
    publishVersion = bintrayPublish
    desc = bintrayDesc
    website = bintrayWeb
}
