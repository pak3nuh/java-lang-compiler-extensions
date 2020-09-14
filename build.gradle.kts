plugins {
    kotlin("jvm") version "1.4.10"
    idea
    `maven-publish`
    signing
}

allprojects {
    version = "0.0.1"
    group = "pt.pak3nuh.util.lang.compiler-extensions"

    repositories {
        jcenter()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "idea")

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    }

    ifNotTestProject(this) {
        apply(plugin = "kotlin")
        apply(plugin = "maven-publish")

        java {
            withSourcesJar()
        }

        publishing {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                }
            }
        }

        //todo signing

        tasks {
            compileKotlin {
                kotlinOptions.jvmTarget = "1.8"
            }
            compileTestKotlin {
                kotlinOptions.jvmTarget = "1.8"
            }
        }

        dependencies {
            implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        }
    }

}

fun ifNotTestProject(project: Project, block: () -> Unit) {
    if (!project.name.endsWith("-test")) {
        block()
    }
}
