plugins {
    java
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "io.test-gear"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

configure(subprojects) {
    group = "io.test-gear"
    version = version

    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "java")

    publishing {
        repositories {
            maven {
            name = "OSSRH"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
            }
        }
        publications {
            create<MavenPublication>("maven") {
                suppressAllPomMetadataWarnings()
                versionMapping {
                    allVariants {
                        fromResolutionResult()
                    }
                }
                pom {
                    name.set(project.name)
                    description.set("Module ${project.name} of TestGear Framework.")
                    url.set("https://github.com/TestGear-TMS/adapters-java")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("integration")
                            name.set("Integration team")
                            email.set("integrations@test-gear.io")
                        }
                    }
                    scm {
                        developerConnection.set("scm:git:git://github.com/TestGear-TMS/adapters-java")
                        connection.set("scm:git:git://github.com/TestGear-TMS/adapters-java")
                        url.set("https://github.com/TestGear-TMS/adapters-java")
                    }
                    issueManagement {
                        system.set("GitHub Issues")
                        url.set("https://github.com/TestGear-TMS/adapters-java/issues")
                    }
                }
            }
        }
    }

    signing {
        sign(publishing.publications["maven"])
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    tasks.withType<Sign>().configureEach {
        onlyIf { !project.version.toString().endsWith("-SNAPSHOT") }
    }

    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }

    tasks.jar {
        manifest {
            attributes(mapOf(
            "Specification-Title" to project.name,
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
            ))
        }
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }

    publishing.publications.named<MavenPublication>("maven") {
        pom {
            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}