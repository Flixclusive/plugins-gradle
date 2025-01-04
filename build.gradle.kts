import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("com.google.guava:guava:30.1.1-jre")
    compileOnly("com.android.tools:sdk-common:31.0.0")
    compileOnly("org.ow2.asm:asm:9.4")
    compileOnly("org.ow2.asm:asm-tree:9.4")
    compileOnly("com.github.vidstige:jadb:master-SNAPSHOT")

    implementation("com.android.tools.build:gradle:8.7.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.0.0-beta4")
    implementation("com.github.flixclusiveorg.core-stubs:model-provider:1.2.3")
}

gradlePlugin {
    plugins {
        register("flixclusiveProvider") {
            id = "flx-provider"
            implementationClass = "FlixclusiveProvider"
        }
    }
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from("src/main/kotlin")
}

group = "com.github.flixclusive"
version = "1.2.3"

publishing {
    repositories {
        mavenLocal()
    }

    publications {
        create<MavenPublication>("release") {
            artifact(sourcesJar)
        }
    }
}
