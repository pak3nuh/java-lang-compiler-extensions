subprojects {
    group = "${Projects.baseGroupId}.sealed"
    apply(plugin = "java-library")

    if (!name.endsWith("-test")) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
    }
}
