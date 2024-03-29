plugins {
    kotlin("jvm") version "1.9.10"
    id("java-gradle-plugin")
    id("maven-publish")
    kotlin("plugin.serialization") version "1.9.10"
}

group = "com.flixclusive"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib", kotlin.coreLibrariesVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    compileOnly(gradleApi())

    compileOnly("com.google.guava:guava:30.1.1-jre")
    compileOnly("com.android.tools:sdk-common:31.0.0")
    compileOnly("com.android.tools.build:gradle:7.2.2")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")

    implementation("org.ow2.asm:asm:9.4")
    implementation("org.ow2.asm:asm-tree:9.4")
    implementation("com.github.vidstige:jadb:master-SNAPSHOT")
}

gradlePlugin {
    plugins {
        create("com.flixclusive.gradle") {
            id = "com.flixclusive.gradle"
            implementationClass = "com.flixclusive.gradle.FlixclusiveProvider"
        }
    }
}

publishing {
    repositories {
        mavenLocal()

        val token = System.getenv("GITHUB_TOKEN")

        if (token != null) {
            maven {
                credentials {
                    username = "flixclusive"
                    password = token
                }
                setUrl("https://maven.pkg.github.com/flixclusive/gradle")
            }
        }
    }
}
