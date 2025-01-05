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
    
    val agp = "8.7.3"
    val coreStubs = "1.2.4"
    val kotlin = "2.0.21"
    val shadow = "9.0.0-beta4"

    compileOnly("com.android.tools.build:gradle:$agp")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin")
    implementation("com.github.flixclusiveorg.core-stubs:model-provider:$coreStubs")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:$shadow")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:$kotlin")
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
