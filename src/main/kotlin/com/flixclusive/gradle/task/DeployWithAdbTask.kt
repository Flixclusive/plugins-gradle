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
import com.flixclusive.gradle.entities.Repository
import com.flixclusive.gradle.util.buildValidFilename
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.internal.impldep.com.google.common.reflect.TypeToken
import org.jetbrains.kotlin.com.google.gson.Gson
import se.vidstige.jadb.*
import java.io.File
import java.io.Reader
import java.nio.charset.StandardCharsets

abstract class DeployWithAdbTask : DefaultTask() {
    @get:Input
    @set:Option(option = "wait-for-debugger", description = "Enables debugging flag when starting the main activity")
    var waitForDebugger: Boolean = false

    @get:Input
    @set:Option(option = "debug-app", description = "Load the provider on the debug app")
    var debugApp: Boolean = false

    @TaskAction
    fun deployWithAdb() {
        val android = project.extensions.getByName("android") as BaseExtension

        AdbServerLauncher(Subprocess(), android.adbExecutable.absolutePath).launch()
        val jadbConnection = JadbConnection()
        val devices = jadbConnection.devices.filter {
            try {
                it.state == JadbDevice.State.Device
            } catch (e: JadbException) {
                false
            }
        }

        require(devices.size == 1) {
            "Only one ADB device should be connected, but ${devices.size} were!"
        }

        val device = devices[0]

        if (!pushFilesToLocalPath(device)) {
            return
        }

        val activityPath = if (debugApp) {
            "com.flixclusive.debug/com.flixclusive.mobile.MobileActivity"
        } else "com.flixclusive/com.flixclusive.mobile.MobileActivity"

        val args = arrayListOf("start", "-S", "-n", activityPath)

        if (waitForDebugger) {
            args.add("-D")
        }

        val response = String(
            device.executeShell("am", *args.toTypedArray()).readAllBytes(), StandardCharsets.UTF_8
        )

        if (response.contains("Error")) {
            logger.error(response)
        }

        logger.lifecycle("Deployed to ${device.serial}")
    }


    private fun pushFilesToLocalPath(device: JadbDevice): Boolean {
        val makeTask = project.tasks.getByName("make") as AbstractCopyTask
        val updaterJsonTask = project.rootProject.tasks.getByName("generateUpdaterJson") as AbstractCopyTask

        val providerFile = makeTask.outputs.files.singleFile
        val updaterJsonFile = updaterJsonTask.outputs.files.singleFile

        val randomProvider = fromJson<List<Repository>>(updaterJsonFile.reader())
            .randomOrNull()
            ?: return false
        val sanitizedFolderName = buildValidFilename(randomProvider.url)

        val fullPath = LOCAL_FILE_PATH + "/${sanitizedFolderName}/${providerFile.name}"
        val fileToPush = File(fullPath)

        if (!fileToPush.exists()) {
            val isSuccess = fileToPush.mkdirs()
            if (!isSuccess) {
                logger.error("Failed to create local directories when loading providers.")
                return false
            }
        }

        device.push(providerFile, RemoteFile(fullPath))
        logger.lifecycle("${providerFile.nameWithoutExtension} have been pushed...")

        return true
    }

    private inline fun <reified T> fromJson(
        reader: Reader
    ): T = Gson().fromJson(reader, object : TypeToken<T>() {}.type)

    companion object {
        private const val LOCAL_FILE_PATH = "/storage/emulated/0/Flixclusive/providers/"
    }
}