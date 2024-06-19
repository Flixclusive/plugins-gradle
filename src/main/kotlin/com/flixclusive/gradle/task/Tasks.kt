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
import com.flixclusive.gradle.getFlixclusive
import com.flixclusive.gradle.util.createProviderManifest
import groovy.json.JsonBuilder
import groovy.json.JsonGenerator
import org.gradle.api.Project
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

const val TASK_GROUP = "aliucord"

fun registerTasks(project: Project) {
    val extension = project.extensions.getFlixclusive()
    val intermediates = project.buildDir.resolve("intermediates")

    if (project.rootProject.tasks.findByName("generateUpdaterJson") == null) {
        project.rootProject.tasks.register("generateUpdaterJson", GenerateUpdaterJsonTask::class.java) {
            it.group = TASK_GROUP

            it.outputs.upToDateWhen { false }

            it.outputFile.set(it.project.buildDir.resolve("updater.json"))
        }
    }

    val providerClassFile = intermediates.resolve("providerClass")

    val compileDex = project.tasks.register("compileDex", CompileDexTask::class.java) {
        it.group = TASK_GROUP

        it.providerClassFile.set(providerClassFile)

        // Doing this since KotlinCompile does not inherit AbstractCompile no more.
        val compileKotlinTask = project.tasks.findByName("compileDebugKotlin") as KotlinCompile?
        if (compileKotlinTask != null) {
            it.dependsOn(compileKotlinTask)
            it.input.from(compileKotlinTask.destinationDirectory)
        }

        val compileJavaWithJavac = project.tasks.findByName("compileDebugJavaWithJavac") as AbstractCompile?
        if (compileJavaWithJavac != null) {
            it.dependsOn(compileJavaWithJavac)
            it.input.from(compileJavaWithJavac.destinationDirectory)
        }

        it.outputFile.set(intermediates.resolve("classes.dex"))
    }

    val compileResources = project.tasks.register("compileResources", CompileResourcesTask::class.java) {
        it.group = TASK_GROUP

        val processManifestTask = project.tasks.getByName("processDebugManifest") as ProcessLibraryManifest
        it.dependsOn(processManifestTask)

        val android = project.extensions.getByName("android") as BaseExtension
        it.input.set(android.sourceSets.getByName("main").res.srcDirs.single())
        it.manifestFile.set(processManifestTask.manifestOutputFile)

        it.outputFile.set(intermediates.resolve("res.apk"))

        it.doLast { _ ->
            val resApkFile = it.outputFile.asFile.get()

            if (resApkFile.exists()) {
                project.tasks.named("make", AbstractCopyTask::class.java) { makeTask ->
                    makeTask.from(project.zipTree(resApkFile)) { copySpec ->
                        copySpec.exclude("AndroidManifest.xml")
                    }
                }
            }
        }
    }

    project.afterEvaluate {
        val make = project.tasks.register("make", Zip::class.java) {
            it.group = TASK_GROUP
            val compileDexTask = compileDex.get()
            it.dependsOn(compileDexTask)

            val manifestFile = intermediates.resolve("manifest.json")

            it.from(manifestFile)
            it.doFirst {
                val (versionCode, _) = extension.getVersionDetails()
                require(versionCode > 0L) {
                    "No version is set"
                }

                if (extension.providerClassName == null) {
                    if (providerClassFile.exists()) {
                        extension.providerClassName = providerClassFile.readText()
                    }
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

            it.from(compileDexTask.outputFile)

            val zip = it as Zip
            if (extension.requiresResources.get()) {
                zip.dependsOn(compileResources.get())
            }

            zip.isPreserveFileTimestamps = false
            zip.archiveBaseName.set(project.name)
            zip.archiveExtension.set("flx")
            zip.archiveVersion.set("")
            zip.destinationDirectory.set(project.buildDir)

            it.doLast { task ->
                task.logger.lifecycle("Made Flixclusive package at ${task.outputs.files.singleFile}")
            }
        }

        project.rootProject.tasks.getByName("generateUpdaterJson").dependsOn(make)
        project.tasks.register("deployWithAdb", DeployWithAdbTask::class.java) {
            it.group = TASK_GROUP
            it.dependsOn("make")
            it.dependsOn(":generateUpdaterJson")
        }

        project.tasks.register("cleanCache", CleanCacheTask::class.java) {
            it.group = TASK_GROUP
        }
    }
}