group = "io.github.pak3nuh.util.lang.compiler.sealed"

dependencies {
    annotationProcessor(project(":sealed-classes:processor"))
    implementation(project(":sealed-classes:api"))
}
