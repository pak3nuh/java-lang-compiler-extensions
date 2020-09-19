group = "io.github.pak3nuh.util.lang.compiler.enum-expression"

dependencies {
    annotationProcessor(project(":enum-expression:processor"))
    implementation(project(":enum-expression:api"))
    implementation(project(":enum-expression:processor"))
}
