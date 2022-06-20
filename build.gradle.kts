import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
    `java-library`
    `maven-publish`
    signing
    jacoco
    id("pl.allegro.tech.build.axion-release") version "1.13.14"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.sonarqube") version "3.4.0.2513"
}

repositories {
    mavenCentral()
}

dependencies {
    shadow("org.springframework:spring-web:5.3.20")
    shadow("commons-codec:commons-codec:1.15")
    testImplementation("org.springframework:spring-web:5.3.20")
    testImplementation("commons-codec:commons-codec:1.15")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

group = "com.github.bgalek.utils"
version = scmVersion.version

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name, "Implementation-Version" to project.version))
    }
}

tasks.shadowJar {
    minimize()
}

tasks.create<ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks["shadowJar"] as ShadowJar
}

tasks["shadowJar"].dependsOn(tasks["relocateShadowJar"])

tasks.test {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(FAILED, SKIPPED)
    }
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

jacoco {
    toolVersion = "0.8.5"
}