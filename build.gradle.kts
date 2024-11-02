import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
    `java-library`
    `maven-publish`
    signing
    jacoco
    id("pl.allegro.tech.build.axion-release") version "1.18.14"
    id("org.sonarqube") version "5.1.0.4882"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-web:5.3.23")
    implementation("commons-codec:commons-codec:1.17.1")
    testImplementation("org.springframework:spring-web:5.3.23")
    testImplementation("commons-codec:commons-codec:1.17.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
}

group = "com.github.bgalek.utils"
version = scmVersion.version

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name, "Implementation-Version" to project.version))
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(FAILED, SKIPPED)
    }
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
    }
}

publishing {
    publications {
        create<MavenPublication>("sonatype") {
            artifactId = "url-signer"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("url-signer")
                description.set("Easy way to ensure that link was not tempered with")
                url.set("https://github.com/bgalek/url-signer/")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("bgalek")
                        name.set("Bartosz Gałek")
                        email.set("bartosz@galek.com.pl")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/bgalek/url-signer.git")
                    developerConnection.set("scm:git:ssh://github.com:bgalek/url-signer.git")
                    url.set("https://github.com/bgalek/url-signer/")
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}

System.getenv("GPG_KEY_ID")?.let {
    signing {
        useInMemoryPgpKeys(
            System.getenv("GPG_KEY_ID"),
            System.getenv("GPG_PRIVATE_KEY"),
            System.getenv("GPG_PRIVATE_KEY_PASSWORD")
        )
        sign(publishing.publications)
    }
}
