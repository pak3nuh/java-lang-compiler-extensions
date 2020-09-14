group = "pt.pak3nuh.util.lang.compiler-extensions.enum-expression"

dependencies {
    annotationProcessor(project(":enum-expression:processor"))
    implementation(project(":enum-expression:api"))
    implementation(project(":enum-expression:processor"))
}
