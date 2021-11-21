plugins {
    kotlin("jvm") version Versions.kotlin
    idea
    `maven-publish`
    signing
}

allprojects {
    version = "0.3.0"
    group = Projects.baseGroupId

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "idea")

    this.afterEvaluate {

        if (plugins.hasPlugin("java-base")) {
            tasks.withType<Test> {
                useJUnitPlatform()
            }

            dependencies {
                testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
                testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
            }
        }

        val publishingEnabled: String? by project
        if (publishingEnabled?.toBoolean() == true && plugins.hasPlugin("maven-publish")) {
            java {
                withSourcesJar()
                withJavadocJar()
            }

            publishing {
                publications {
                    create<MavenPublication>("mavenJava") {
                        from(components["java"])
                        pom {
                            name.set(this@afterEvaluate.name)
                            description.set(this@afterEvaluate.description)
                            url.set("https://github.com/pak3nuh/java-lang-compiler-extensions")
                            developers {
                                developer {
                                    id.set("pak3nuh")
                                    name.set("Nuno Caro")
                                    email.set("nuno.pik@gmail.com")
                                }
                            }
                            scm {
                                connection.set("scm:git:git://github.com/pak3nuh/java-lang-compiler-extensions.git")
                                developerConnection.set("scm:git:ssh://github.com:pak3nuh/java-lang-compiler-extensions.git")
                                url.set("https://github.com/pak3nuh/java-lang-compiler-extensions")
                            }
                            licenses {
                                license {
                                    name.set("MIT")
                                    url.set("https://opensource.org/licenses/MIT")
                                }
                            }
                        }
                    }
                }
                repositories {
                    val ossrhUsername: String by project
                    val ossrhPassword: String by project
                    maven {
                        name = "SonatypeOSS"
                        url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                        credentials.username = ossrhUsername
                        credentials.password = ossrhPassword
                    }
                }
            }

            signing {
                sign(publishing.publications["mavenJava"])
            }
        }

        if (plugins.hasPlugin("kotlin")) {
            tasks {
                compileKotlin {
                    kotlinOptions.jvmTarget = "1.8"
                }
                compileTestKotlin {
                    kotlinOptions.jvmTarget = "1.8"
                }
            }
        }
    }

}
