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

package com.flixclusive.gradle

import com.flixclusive.gradle.entities.Author
import com.flixclusive.gradle.entities.Language
import com.flixclusive.gradle.entities.PluginType
import com.flixclusive.gradle.entities.Repository.Companion.toValidRepositoryLink
import com.flixclusive.gradle.entities.Status
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class FlixclusiveExtension @Inject constructor(val project: Project) {
    /**
     *
     * [Author]s of the extension
     * */
    val authors: ListProperty<Author> = project.objects.listProperty(Author::class.java)

    var buildBranch = "builds"

    val repositoryUrl: Property<String> = project.objects.property(String::class.java)
    val updateUrl: Property<String> = project.objects.property(String::class.java)
    val buildUrl: Property<String> = project.objects.property(String::class.java)

    val changelog: Property<String> = project.objects.property(String::class.java)
    val changelogMedia: Property<String> = project.objects.property(String::class.java)

    var flixclusive: FlixclusiveInfo? = null
        internal set

    internal var pluginClassName: String? = null

    val description: Property<String> = project.objects.property(String::class.java)
    val iconUrl: Property<String> = project.objects.property(String::class.java)
    val language: Property<Language> = project.objects.property(Language::class.java)
    val pluginType: Property<PluginType> = project.objects.property(PluginType::class.java)
    val requiresResources = false
    val status: Status = Status.Beta

    /**
     *
     * Excludes this plugin from the updater, meaning it won't show up for users.
     * Set this if the plugin is still on beta.
     * */
    val excludeFromUpdaterJson: Property<Boolean> =
        project.objects.property(Boolean::class.java).convention(false)

    val userCache = project.gradle.gradleUserHomeDir.resolve("caches").resolve("flixclusive")

    /**
     * Adds an author to the list of authors.
     *
     * @param name The name of the author.
     * @param userLink The optional link associated with the author's profile.
     * @param discordId The optional Discord ID of the author.
     */
    fun author(
        name: String,
        userLink: String? = null,
        discordId: Long? = null,
    ) {
        authors.add(
            Author(
                name = name,
                userLink = userLink,
                discordId = discordId
            )
        )
    }

    fun setRepository(url: String) {
        url.toValidRepositoryLink()
            .run {
                updateUrl.set(getRawLink(filename = "updater.json", branch = buildBranch))
                buildUrl.set(getRawLink(filename = "%s.flx", branch = buildBranch))
                repositoryUrl.set(this.url)
            }
    }
}

class FlixclusiveInfo(extension: FlixclusiveExtension, version: String) {
    val cache = extension.userCache.resolve("flixclusive")

    val urlPrefix =
        "https://github.com/rhenwinch/Flixclusive/releases/download/${version}"
    val apkFile = cache.resolve("flixclusive.apk")
    val jarFile = cache.resolve("flixclusive.jar")
}

fun ExtensionContainer.getFlixclusive(): FlixclusiveExtension {
    return getByName("flixclusive") as FlixclusiveExtension
}

fun ExtensionContainer.findFlixclusive(): FlixclusiveExtension? {
    return findByName("flixclusive") as FlixclusiveExtension?
}