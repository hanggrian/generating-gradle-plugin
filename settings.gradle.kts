include(RELEASE_ARTIFACT)
includeDir("buildconfig-integration-tests")

fun includeDir(dir: String) = File(dir).walk().filter { it.isDirectory }.forEach { include("$dir:${it.name}") }