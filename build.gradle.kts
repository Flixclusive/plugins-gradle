import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(kotlin("stdlib", kotlin.coreLibrariesVersion))
    compileOnly(gradleApi())

    compileOnly("com.google.guava:guava:30.1.1-jre")
    compileOnly("com.android.tools:sdk-common:31.0.0")
    compileOnly("com.android.tools.build:gradle:8.2.0")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")

    implementation("com.github.johnrengelman:shadow:8.1.0")
    implementation("org.ow2.asm:asm:9.4")
    implementation("org.ow2.asm:asm-tree:9.4")
    implementation("com.github.vidstige:jadb:master-SNAPSHOT")
    implementation("com.github.flixclusiveorg.core-stubs:model-provider:1.0.4")
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
version = "1.2.2"

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
