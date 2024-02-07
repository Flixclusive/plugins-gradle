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

package com.flixclusive.gradle.configuration

import com.flixclusive.gradle.createProgressLogger
import com.flixclusive.gradle.download
import com.flixclusive.gradle.FlixclusiveInfo
import com.flixclusive.gradle.getFlixclusive
import com.googlecode.d2j.dex.Dex2jar
import com.googlecode.d2j.reader.BaseDexFileReader
import com.googlecode.d2j.reader.MultiDexFileReader
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import java.net.URL
import java.nio.file.Files

class FlixclusiveConfigurationProvider : IConfigurationProvider {

    override val name: String
        get() = "Flixclusive"

    override fun provide(project: Project, dependency: Dependency) {
        with(project) {
            val extension = extensions.getFlixclusive()
            val flixclusive = FlixclusiveInfo(extension, dependency.version ?: "pre-release").also { extension.flixclusive = it }

            flixclusive.cache.mkdirs()

            if (!flixclusive.apkFile.exists()) {
                logger.lifecycle("Downloading Flixclusive apk")

                val url = URL("${flixclusive.urlPrefix}/flixclusive-prerelease.apk")

                url.download(flixclusive.apkFile, createProgressLogger(project, "Download Flixclusive apk"))
            }

            if (!flixclusive.jarFile.exists()) {
                logger.lifecycle("Converting Flixclusive apk to jar")

                val reader: BaseDexFileReader = MultiDexFileReader.open(Files.readAllBytes(flixclusive.apkFile.toPath()))

                Dex2jar.from(reader)
                    .topoLogicalSort()
                    .skipDebug(false)
                    .noCode(true)
                    .to(flixclusive.jarFile.toPath())
            }

            dependencies.add("compileOnly", files(flixclusive.jarFile))
        }
    }
}