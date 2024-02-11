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
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import java.net.URL

class FlixclusiveConfigurationProvider : IConfigurationProvider {

    override val name: String
        get() = "flixclusive"

    override fun provide(project: Project, dependency: Dependency) {
        with(project) {
            val extension = extensions.getFlixclusive()
            val flixclusive = FlixclusiveInfo(extension, dependency.version ?: "pre-release").also { extension.flixclusive = it }

            flixclusive.cache.mkdirs()

            if (!flixclusive.jarFile.exists()) {
                logger.lifecycle("Downloading Flixclusive JAR")

                val url = URL("${flixclusive.urlPrefix}/classes.jar")

                url.download(flixclusive.jarFile, createProgressLogger(project, "Download Flixclusive JAR"))
            }

            dependencies.add("compileOnly", files(flixclusive.jarFile))
            dependencies.add("testImplementation", files(flixclusive.jarFile))
        }
    }
}