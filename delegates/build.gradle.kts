subprojects {
    group = "${Projects.baseGroupId}.delegates"
    apply(plugin = "java-library")

    if (!name.endsWith("-test")) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
    }
}