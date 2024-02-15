import org.jetbrains.kotlin.ir.backend.js.compile

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.4/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    id("maven-publish")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    // testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    // testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")

    //testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    api ("io.undertow:undertow-core:2.2.27.Final")
//    implementation ("io.undertow:undertow-servlet:2.2.0.Final")
//    implementation ("io.undertow:undertow-websockets-jsr:2.2.10.Final")
    // https://mvnrepository.com/artifact/org.webjars/bootstrap
    implementation("org.webjars:bootstrap:5.3.2")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-text
    implementation("org.apache.commons:commons-text:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}


tasks.register("buildFe") {
    doFirst {
        logger.info("Generating static files.")
        exec {
            workingDir = File(projectDir.parentFile, "fe")
            commandLine = mutableListOf("./gradlew", "zip")
        }
    }
}

sourceSets["main"].resources {
    srcDirs(File(projectDir.parentFile, "fe/build/dist/js"))
}

tasks.findByName("assemble")?.dependsOn("buildFe")

val GROUP_ID = "top.ntutn.ktwebio"
val ARTIFACT_ID = "lib"
val VERSION = latestGitTag().ifEmpty { "0.0.1" }

fun latestGitTag(): String {
    val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0").start()
    return  process.inputStream.bufferedReader().use {bufferedReader ->
        bufferedReader.readText().trim()
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = GROUP_ID
            artifactId = ARTIFACT_ID
            version = VERSION
            afterEvaluate {
                from(components["java"])
            }
        }
    }
}
