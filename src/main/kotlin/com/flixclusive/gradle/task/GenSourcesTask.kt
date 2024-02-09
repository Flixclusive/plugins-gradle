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

import com.flixclusive.gradle.createProgressLogger
import com.flixclusive.gradle.download
import com.flixclusive.gradle.getFlixclusive
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.net.URL

abstract class GenSourcesTask : DefaultTask() {
    @TaskAction
    fun genSources() {
        val extension = project.extensions.getFlixclusive()
        val flixclusive = extension.flixclusive!!

        val sourcesJarFile = flixclusive.cache.resolve("flixclusive-sources.jar")

        val url = URL("${flixclusive.urlPrefix}/fat-sources.jar")

        url.download(sourcesJarFile, createProgressLogger(project, "Download fat sources"))
    }
}