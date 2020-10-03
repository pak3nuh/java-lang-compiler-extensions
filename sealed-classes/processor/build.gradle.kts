description = "Processor implementation"

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(platform(Dependencies.kotlinPlatform))
    implementation(Dependencies.kotlinStdLib)
    implementation(project(":sealed-classes:api"))
    implementation(Dependencies.javaPoet)
}
