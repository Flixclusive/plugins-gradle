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

import com.flixclusive.gradle.entities.PluginData
import com.flixclusive.gradle.getFlixclusive
import com.flixclusive.gradle.util.createPluginData
import groovy.json.JsonBuilder
import groovy.json.JsonGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.LinkedList

abstract class GenerateUpdaterJsonTask : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generateUpdaterJson() {
        val list = LinkedList<PluginData>()

        for (subproject in project.allprojects) {
            val flixclusive = subproject.extensions.getFlixclusive()

            if (flixclusive.excludeFromUpdaterJson.get()) {
                continue
            }

            list += subproject.createPluginData()
        }

        outputFile.asFile.get().writeText(
            JsonBuilder(
                /* content = */ list,
                /* generator = */ JsonGenerator.Options()
                    .excludeNulls()
                    .build()
            ).toPrettyString()
        )

        logger.lifecycle("Created ${outputFile.asFile.get()}")
    }
}