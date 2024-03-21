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
import com.flixclusive.gradle.entities.ProviderType
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

    private var buildBranch = "builds"

    var versionMajor = 0
    var versionMinor = 0
    var versionPatch = 0
    var versionBuild = 0

    val repositoryUrl: Property<String> = project.objects.property(String::class.java)
    val updateUrl: Property<String> = project.objects.property(String::class.java)
    val buildUrl: Property<String> = project.objects.property(String::class.java)

    val changelog: Property<String> = project.objects.property(String::class.java)
    val changelogMedia: Property<String> = project.objects.property(String::class.java)

    var flixclusive: FlixclusiveInfo? = null
        internal set

    internal var providerClassName: String? = null

    val description: Property<String> = project.objects.property(String::class.java)
    val iconUrl: Property<String> = project.objects.property(String::class.java)
    val language: Property<Language> = project.objects.property(Language::class.java)
    val providerType: Property<ProviderType> = project.objects.property(ProviderType::class.java)
    val requiresResources: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)
    val status: Property<Status> = project.objects.property(Status::class.java)
        .convention(Status.Beta)

    /**
     *
     * Excludes this provider from the updater, meaning it won't show up for users.
     * Set this if the provider is still on beta.
     * */
    val excludeFromUpdaterJson: Property<Boolean> =
        project.objects.property(Boolean::class.java).convention(false)

    val userCache = project.gradle.gradleUserHomeDir.resolve("caches").resolve("flixclusive")

    /**
     * Adds an author to the list of authors.
     *
     * @param name The name of the author.
     * @param githubLink The optional link associated with the author's github profile.
     */
    fun author(
        name: String,
        githubLink: String? = null
    ) {
        authors.add(
            Author(
                name = name,
                githubLink = githubLink
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


    /**
     *
     * @return a pair that contains the version name and code
     * using the [versionMajor], [versionMinor], [versionPatch]
     * and [versionBuild]
     * */
    fun getVersionDetails(): Pair<Long, String> {
        val versionCode = versionMajor * 10000L + versionMinor * 1000 + versionPatch * 100 + versionBuild
        val versionName = "${versionMajor}.${versionMinor}.${versionPatch}"

        return versionCode to versionName
    }
}

class FlixclusiveInfo(extension: FlixclusiveExtension, version: String) {
    val cache = extension.userCache.resolve("flixclusive")

    val urlPrefix =
        "https://github.com/rhenwinch/Flixclusive/releases/download/${version}"
    val jarFile = cache.resolve("flixclusive.jar")
}

fun ExtensionContainer.getFlixclusive(): FlixclusiveExtension {
    return getByName("flixclusive") as FlixclusiveExtension
}

fun ExtensionContainer.findFlixclusive(): FlixclusiveExtension? {
    return findByName("flixclusive") as FlixclusiveExtension?
}