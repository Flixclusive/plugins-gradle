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

import com.android.build.api.dsl.LibraryExtension
import com.flixclusive.gradle.FLX_PROVIDER_EXTENSION_NAME
import com.flixclusive.gradle.FlixclusiveProviderExtension
import com.flixclusive.gradle.configuration.registerConfigurations
import com.flixclusive.gradle.task.registerTasks
import com.flixclusive.gradle.util.configureAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
abstract class FlixclusiveProvider : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("com.gradleup.shadow")
            }

            extensions.create(FLX_PROVIDER_EXTENSION_NAME, FlixclusiveProviderExtension::class.java, project)


            extensions.configure<LibraryExtension> {
                configureAndroid(commonExtension = this@configure)
            }
        }

        registerTasks(project)
        registerConfigurations(project)
    }
}