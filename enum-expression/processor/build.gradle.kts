group = "io.github.pak3nuh.util.lang.compiler.enum-expression"
description = "Processor implementation"

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(platform(Dependencies.kotlinPlatform))
    implementation(Dependencies.kotlinStdLib)
    implementation(project(":enum-expression:api"))
    implementation(project(":gen-util"))
    implementation(Dependencies.javaPoet)
}
