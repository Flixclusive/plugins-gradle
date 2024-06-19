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

import com.flixclusive.gradle.GithubData.Companion.toGithubData
import com.flixclusive.gradle.createProgressLogger
import com.flixclusive.gradle.download
import com.flixclusive.gradle.Stubs
import com.flixclusive.gradle.getFlixclusive
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

class GetProviderStubsConfigurationProvider : IConfigurationProvider {

    override val name: String
        get() = "getProviderStubs"

    override fun provide(project: Project, dependency: Dependency) {
        with(project) {
            val extension = extensions.getFlixclusive()
            val stubs = Stubs(
                extension = extension,
                data = dependency.toGithubData()
            ).also { extension.stubs = it }

            if (!stubs.file.exists()) {
                logger.lifecycle("Downloading provider stubs")

                stubs.githubAarDownloadUrl.download(
                    file = stubs.file,
                    progressLogger = createProgressLogger(
                        project = project,
                        loggerCategory = "Download provider stubs"
                    )
                )
            }

            dependencies.add("compileOnly", files(stubs.file))
            dependencies.add("testImplementation", files(stubs.file))
        }
    }
}