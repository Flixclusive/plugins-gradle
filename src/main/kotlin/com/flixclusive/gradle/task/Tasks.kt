/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.flixclusive.gradle.task

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.tasks.ProcessLibraryManifest
import com.android.builder.dexing.ClassFileInputs
import com.flixclusive.gradle.FLX_PROVIDER_EXTENSION_NAME
import com.flixclusive.gradle.FlixclusiveProviderExtension
import com.flixclusive.gradle.getFlixclusive
import com.flixclusive.gradle.util.AndroidProjectType
import com.flixclusive.gradle.util.createProviderManifest
import com.flixclusive.gradle.util.findProviderClassName
import com.flixclusive.gradle.util.getAndroidProjectType
import com.flixclusive.gradle.util.isValidFilename
import groovy.json.JsonBuilder
import groovy.json.JsonGenerator
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Arrays
import java.util.stream.Collectors

const val TASK_GROUP = "flixclusive"

fun registerTasks(project: Project) {
    val extension = project.extensions.getFlixclusive()
    val intermediates = project.buildDir.resolve("intermediates")

    if (project.rootProject.tasks.findByName("generateUpdaterJson") == null) {
        project.rootProject.tasks.register("generateUpdaterJson", GenerateUpdaterJsonTask::class.java) {
            group = TASK_GROUP

            outputs.upToDateWhen { false }

            outputFile.set(this.project.buildDir.resolve("updater.json"))
        }
    }

    val providerClassFile = intermediates.resolve("providerClass")

    val compileDex = project.tasks.register("compileDex", CompileDexTask::class.java) {
        group = TASK_GROUP

        this@register.providerClassFile.set(providerClassFile)

        // Doing this since KotlinCompile does not inherit AbstractCompile no more.
        val compileKotlinTask = project.tasks.findByName("compileDebugKotlin") as KotlinCompile?
        if (compileKotlinTask != null) {
            dependsOn(compileKotlinTask)
            input.from(compileKotlinTask.destinationDirectory)
        }

        val compileJavaWithJavac = project.tasks.findByName("compileDebugJavaWithJavac") as AbstractCompile?
        if (compileJavaWithJavac != null) {
            dependsOn(compileJavaWithJavac)
            input.from(compileJavaWithJavac.destinationDirectory)
        }

        outputFile.set(intermediates.resolve("classes.dex"))
    }

    val compileResources = project.tasks.register("compileResources", CompileResourcesTask::class.java) {
        group = TASK_GROUP

        val processManifestTask = project.tasks.getByName("processDebugManifest") as ProcessLibraryManifest
        dependsOn(processManifestTask)

        val android = project.extensions.getByName("android") as BaseExtension
        input.set(android.sourceSets.getByName("main").res.srcDirs.single())
        manifestFile.set(processManifestTask.manifestOutputFile)

        outputFile.set(intermediates.resolve("res.apk"))

        doLast {
            val resApkFile = outputFile.asFile.get()

            if (resApkFile.exists()) {
                project.tasks.named("make", AbstractCopyTask::class.java) {
                    from(project.zipTree(resApkFile)) {
                        exclude("AndroidManifest.xml")
                    }
                }
            }
        }
    }

    val androidManifestPath = project.file("src/main/AndroidManifest.xml")
    val generateAndroidManifest = project.tasks.register("generateAndroidManifest") {
        androidManifestPath.writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <application
                    android:allowBackup="true"
                    android:label="Flixclusive Provider"
                    android:supportsRtl="true">
                    
                    <!-- This is a fake/mimic AndroidManifest.xml needed for compiling this provider - do NOT delete! -->
                    
                </application>
            </manifest>
        """.trimIndent())
    }

    project.afterEvaluate {
        val make = project.tasks.register("make", Zip::class.java) {
            group = TASK_GROUP

            val generateAndroidManifestTask = generateAndroidManifest.get()

            val compileKotlinTask = project.tasks.findByName("compileDebugKotlin") as KotlinCompile?

            when (project.getAndroidProjectType()) {
                AndroidProjectType.APPLICATION -> {
                    dependsOn(generateAndroidManifestTask)

                    val assembleTask = project.tasks.getByName("assembleDebug")
                    dependsOn(assembleTask)

                    val apkFile = project.file("build/outputs/apk/debug/${project.name}-debug.apk")
                    from(project.zipTree(apkFile)) {
                        duplicatesStrategy = DuplicatesStrategy.INCLUDE
                    }
                }
                AndroidProjectType.LIBRARY -> {
                    val compileDexTask = compileDex.get()
                    dependsOn(compileDexTask)

                    from(compileDexTask.outputFile)

                    if (extension.requiresResources.get()) {
                        dependsOn(compileResources.get())
                    }
                }
                AndroidProjectType.UNKNOWN -> throw IllegalStateException("Non-android providers are not supported YET!")
            }

            doFirst {
                val manifestFile = intermediates.resolve("manifest.json")
                from(manifestFile)

                val (versionCode, _) = extension.getVersionDetails()
                require(versionCode > 0L) {
                    "No version is set"
                }

                if (project.getAndroidProjectType() == AndroidProjectType.APPLICATION && compileKotlinTask != null) {
                    val fileStreams = compileKotlinTask.destinationDirectory.asFile.get().listFiles()
                        ?.map { input ->
                            ClassFileInputs.fromPath(input.toPath())
                                .use { it.entries { _, _ -> true } }
                        }?.toTypedArray()

                    logger.lifecycle("Finding annotated provider class...")
                    Arrays.stream(fileStreams).flatMap { it }
                        .use { classesInput ->
                            val files = classesInput.collect(Collectors.toList())
                            val className = findProviderClassName(files)
                            if (className != null) {
                                logger.lifecycle("Annotated provider class: $className")
                                extension.providerClassName = className
                            }
                        }
                } else if (project.getAndroidProjectType() == AndroidProjectType.LIBRARY && extension.providerClassName == null && providerClassFile.exists()) {
                    extension.providerClassName = providerClassFile.readText()
                }


                require(extension.providerClassName != null) {
                    "No provider class found, make sure your provider class is annotated with @FlixclusiveProvider"
                }

                manifestFile.writeText(
                    JsonBuilder(
                        project.createProviderManifest(),
                        JsonGenerator.Options()
                            .excludeNulls()
                            .build()
                    ).toPrettyString()
                )
            }

            val flxProvider = project.extensions.getByName(FLX_PROVIDER_EXTENSION_NAME) as FlixclusiveProviderExtension
            val projectName = flxProvider.providerName.get()

            if (!isValidFilename(projectName)) {
                throw IllegalStateException("Invalid project name: $projectName")
            }

            isPreserveFileTimestamps = false
            archiveBaseName.set(projectName)
            archiveExtension.set("flx")
            archiveVersion.set("")
            destinationDirectory.set(project.buildDir)

            doLast {
                if (androidManifestPath.exists()) {
                    androidManifestPath.delete()
                }

                logger.lifecycle("Provider package ${projectName}.flx created at ${outputs.files.singleFile}")
            }
        }

        project.rootProject.tasks.getByName("generateUpdaterJson")
            .dependsOn(make.get())
        project.tasks.register("deployWithAdb", DeployWithAdbTask::class.java) {
            group = TASK_GROUP
            dependsOn(make.get())
            dependsOn(":generateUpdaterJson")
        }

        project.tasks.register("cleanCache", CleanCacheTask::class.java) {
            group = TASK_GROUP
        }
    }
}