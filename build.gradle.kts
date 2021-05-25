plugins {
    `java-library`
    `maven-publish`
    signing
    id("org.cadixdev.licenser") version "0.6.0"
}

group = "io.github.juuxel"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:20.1.0")
}

license {
    header(file("HEADER.txt"))
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])

        pom {
            name.set("LibNinePatch")
            description.set("A simple nine-patch renderer")
            url.set("https://github.com/Juuxel/LibNinePatch")

            licenses {
                license {
                    name.set("Mozilla Public License Version 2.0")
                    url.set("https://www.mozilla.org/en-US/MPL/2.0/")
                }
            }

            developers {
                developer {
                    id.set("Juuxel")
                    name.set("Juuxel")
                    email.set("juuzsmods@gmail.com")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/Juuxel/LibNinePatch.git")
                developerConnection.set("scm:git:ssh://github.com:Juuxel/LibNinePatch.git")
                url.set("https://github.com/Juuxel/LibNinePatch")
            }
        }
    }

    repositories {
        maven {
            name = "ossrh"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials(PasswordCredentials::class)
        }
    }
}

tasks {
    jar {
        from("LICENSE")
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(8)
    }

    processResources {
        inputs.property("version", version)

        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }
    }
}

if (project.hasProperty("signing.keyId")) {
    signing {
        sign(publishing.publications)
    }
}
