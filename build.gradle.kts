plugins {
    `java-library`
    `maven-publish`
    signing
    id("net.kyori.indra.licenser.spotless") version "3.0.1"
}

group = "io.github.juuxel"
version = "1.2.0-beta.1"

val demoSources = sourceSets.create("demo")
val modularityJavaVersion = 9
val modularitySources = sourceSets.create("modularity") {
    compileClasspath = files(sourceSets.main.map { it.compileClasspath })

    java {
        srcDirs(sourceSets.main.map { it.java.srcDirs })
    }
}

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
    demoSources.implementationConfigurationName(sourceSets.main.get().output)
}

indraSpotlessLicenser {
    licenseHeaderFile("HEADER.txt")
    newLine(true)
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
        from(modularitySources.output) {
            include("module-info.class")
            into("META-INF/versions/$modularityJavaVersion")
        }
        manifest {
            attributes(
                "Multi-Release" to "true",
                "Fabric-Loom-Remap" to "false",
            )
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(8)
    }

    "compileModularityJava"(JavaCompile::class) {
        options.release.set(modularityJavaVersion)
        options.compilerArgs.addAll(listOf("--module-version", project.version.toString()))
    }

    "sourcesJar"(Jar::class) {
        from("LICENSE")
        from(modularitySources.allSource) {
            include("module-info.java")
            into("META-INF/versions/$modularityJavaVersion")
        }
    }

    javadoc {
        source = modularitySources.allJava
        classpath = modularitySources.compileClasspath
    }

    processResources {
        inputs.property("version", version)

        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }
    }

    register<JavaExec>("runDemo") {
        classpath(demoSources.output)
        classpath(demoSources.runtimeClasspath)
        mainClass.set("juuxel.libninepatch.demo.Main")
    }
}

if (project.hasProperty("signing.keyId")) {
    signing {
        sign(publishing.publications)
    }
}
