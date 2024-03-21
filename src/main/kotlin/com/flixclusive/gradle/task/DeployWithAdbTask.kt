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
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import se.vidstige.jadb.*
import java.nio.charset.StandardCharsets

abstract class DeployWithAdbTask : DefaultTask() {
    @get:Input
    @set:Option(option = "wait-for-debugger", description = "Enables debugging flag when starting the discord activity")
    var waitForDebugger: Boolean = false

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

        val make = project.tasks.getByName("make") as AbstractCopyTask

        val file = make.outputs.files.singleFile

        val path = "/storage/emulated/0/Flixclusive/providers"

        device.push(file, RemoteFile(path + file.name))

        val args = arrayListOf("start", "-S", "-n", "com.flixclusive.mobile/com.flixclusive.mobile.MobileActivity")

        if (waitForDebugger) {
            args.add("-D")
        }

        val response = String(
            device.executeShell("am", *args.toTypedArray()).readAllBytes(), StandardCharsets.UTF_8
        )

        if (response.contains("Error")) {
            logger.error(response)
        }

        logger.lifecycle("Deployed $file to ${device.serial}")
    }
}