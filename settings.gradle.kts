include(RELEASE_ARTIFACT)

File("buildconfig-integration-tests")
    .walk()
    .filter { it.isDirectory }
    .forEach { include("buildconfig-integration-tests:${it.name}") }