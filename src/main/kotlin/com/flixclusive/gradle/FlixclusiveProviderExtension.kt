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
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.net.URL
import javax.inject.Inject

@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class FlixclusiveProviderExtension @Inject constructor(val project: Project) {
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

    /**
     * Changelogs of the provider
     * */
    val changelog: Property<String> = project.objects.property(String::class.java)

    var stubs: Stubs? = null
        internal set

    internal var providerClassName: String? = null

    /** The provider's description */
    val description: Property<String> = project.objects.property(String::class.java)
    /**
     * If your provider has an icon, put its image url here.
     *
     * This is an optional property.
     * */
    val iconUrl: Property<String> = project.objects.property(String::class.java)
    /**
     * The main language of your provider.
     *
     * There are two supported values:
     * - Language.Multiple
     *      - Obviously for providers w/ multiple language support.
     * - Language("en")
     *      - For specific languages only. NOTE: Use the language's short-hand code.
     */
    val language: Property<Language> = project.objects.property(Language::class.java)
    /**
     * The main type that your provider supports.
     *
     * These are the possible values you could set:
     * - ProviderType.All
     * - ProviderType.TvShows
     * - ProviderType.Movies
     * - ProviderType(customType: String) // i.e., ProviderType("Anime")
     */
    val providerType: Property<ProviderType> = project.objects.property(ProviderType::class.java)
    /**
     * Toggle this if this provider has its own resources.
     */
    val requiresResources: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)
    /**
     * The current status of this provider.
     *
     * These are the possible values you could set:
     * - Status.Beta
     * - Status.Maintenance
     * - Status.Down
     * - Status.Working
     */
    val status: Property<Status> = project.objects.property(Status::class.java)
        .convention(Status.Beta)

    /**
     *
     * Excludes this provider from the updater, meaning it won't show up for users.
     * Set this if the provider is still on beta.
     * */
    val excludeFromUpdaterJson: Property<Boolean> =
        project.objects.property(Boolean::class.java).convention(false)

    val userCache = project.gradle.gradleUserHomeDir
        .resolve("caches")

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

    /**
     * Sets the repository URL this provider belongs to
     *
     * @param url The url of the repository.
     */
    fun setRepository(url: String) {
        url.toValidRepositoryLink()
            .run {
                updateUrl.set(getRawLink(filename = "updater.json", branch = buildBranch))
                buildUrl.set(getRawLink(filename = "%s.flx", branch = buildBranch))
                repositoryUrl.set(this.url)
            }
    }


    /**
     * Calculates and returns version details.
     *
     * @return A Pair containing the version code (Long) and version name (String)
     * calculated using the [versionMajor], [versionMinor], [versionPatch], and
     * [versionBuild] properties.
     */
    fun getVersionDetails(): Pair<Long, String> {
        val versionCode = versionMajor * 10000L + versionMinor * 1000 + versionPatch * 100 + versionBuild
        val versionName = "${versionMajor}.${versionMinor}.${versionPatch}"

        return versionCode to versionName
    }
}

data class GithubData(
    val owner: String,
    val repository: String,
    val tag: String,
) {
    companion object {
        private const val DEFAULT_GITHUB_REPOSITORY_OWNER = "rhenwinch"
        private const val DEFAULT_GITHUB_REPOSITORY = "Flixclusive"
        private const val DEFAULT_GITHUB_RELEASE_TAG = "pre-release"

        fun Dependency.toGithubData(): GithubData
            = GithubData(
                owner = group ?: DEFAULT_GITHUB_REPOSITORY_OWNER,
                repository = name ?: DEFAULT_GITHUB_REPOSITORY,
                tag = version ?: DEFAULT_GITHUB_RELEASE_TAG,
            )
    }
}

class Stubs(
    extension: FlixclusiveProviderExtension,
    data: GithubData
) {
    private val cache = extension.userCache.resolve("provider-stubs")

    val githubAarDownloadUrl =
        URL("https://github.com/${data.owner}/${data.repository}/releases/download/${data.tag}/provider-stubs.aar")
    val file = cache.resolve("provider-stubs.aar")

    init {
        cache.mkdirs()
    }
}

fun ExtensionContainer.getFlixclusive(): FlixclusiveProviderExtension {
    return getByName("flxProvider") as FlixclusiveProviderExtension
}

fun ExtensionContainer.findFlixclusive(): FlixclusiveProviderExtension? {
    return findByName("flxProvider") as FlixclusiveProviderExtension?
}