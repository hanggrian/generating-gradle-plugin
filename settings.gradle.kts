include(RELEASE_ARTIFACT)
include("website")
includeDir("buildconfig-integration-tests")

fun includeDir(name: String) = file(name)
    .listFiles()
    .filter { it.isDirectory }
    .forEach { include("$name:${it.name}") }