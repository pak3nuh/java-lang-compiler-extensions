subprojects {
    group = "${Projects.baseGroupId}.enum-expression"
    apply(plugin = "java-library")

    if (!name.endsWith("-test")) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
    }
}
