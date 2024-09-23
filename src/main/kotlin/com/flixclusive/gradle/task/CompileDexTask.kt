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
import com.android.build.gradle.internal.errors.MessageReceiverImpl
import com.android.build.gradle.options.SyncOptions.ErrorFormatMode
import com.android.builder.dexing.ClassFileInputs
import com.android.builder.dexing.DexArchiveBuilder
import com.android.builder.dexing.DexParameters
import com.android.builder.dexing.r8.ClassFileProviderFactory
import com.flixclusive.gradle.getFlixclusive
import com.flixclusive.gradle.util.findProviderClassName
import com.google.common.io.Closer
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.util.Arrays
import java.util.stream.Collectors

abstract class CompileDexTask : DefaultTask() {
    @InputFiles
    @SkipWhenEmpty
    @IgnoreEmptyDirectories
    val input: ConfigurableFileCollection = project.objects.fileCollection()

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:OutputFile
    abstract val providerClassFile: RegularFileProperty

    @Suppress("UnstableApiUsage")
    @TaskAction
    fun compileDex() {
        val android = project.extensions.getByName("android") as BaseExtension

        val minSdk = android.defaultConfig.minSdkVersion?.apiLevel ?: 21

        val dexOutputDir = outputFile.get().asFile.parentFile

        Closer.create().use { closer ->
            val dexBuilder = DexArchiveBuilder.createD8DexBuilder(
                DexParameters(
                    minSdkVersion = minSdk,
                    debuggable = true,
                    dexPerClass = false,
                    withDesugaring = true,
                    desugarBootclasspath = ClassFileProviderFactory(android.bootClasspath.map(File::toPath))
                        .also { closer.register(it) },
                    desugarClasspath = ClassFileProviderFactory(listOf<Path>()).also { closer.register(it) },
                    coreLibDesugarConfig = null,
                    enableApiModeling = true,
                    messageReceiver = MessageReceiverImpl(
                        ErrorFormatMode.HUMAN_READABLE,
                        LoggerFactory.getLogger(CompileDexTask::class.java),
                    )
                )
            )

            val fileStreams =
                input.map { input -> ClassFileInputs.fromPath(input.toPath()).use { it.entries { _, _ -> true } } }
                    .toTypedArray()

            Arrays.stream(fileStreams).flatMap { it }
                .use { classesInput ->
                    val files = classesInput.collect(Collectors.toList())

                    dexBuilder.convert(
                        input = files.stream(),
                        globalSyntheticsOutput = null,
                        dexOutput = dexOutputDir.toPath()
                    )

                    val className = project.findProviderClassName(files = files)
                    if (className != null) {
                        extensions.getFlixclusive().providerClassName = className
                        providerClassFile.asFile.orNull?.writeText(className)
                    }
                }
        }

        logger.lifecycle("Compiled dex to ${outputFile.get()}")
    }
}