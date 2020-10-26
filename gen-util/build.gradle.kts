group = Projects.baseGroupId
description = "Generator util"

plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

dependencies {
    implementation(Dependencies.javaPoet)
}
