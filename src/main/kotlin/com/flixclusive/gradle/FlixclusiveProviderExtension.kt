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

import com.flixclusive.model.provider.Author
import com.flixclusive.model.provider.Language
import com.flixclusive.model.provider.ProviderType
import com.flixclusive.model.provider.Repository.Companion.toValidRepositoryLink
import com.flixclusive.model.provider.Status
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.net.URL
import javax.inject.Inject

internal const val APK_STUBS_DEPRECATED_MESSAGE = "This class is deprecated. See https://github.com/flixclusiveorg/core-stubs for more details."

const val FLX_PROVIDER_EXTENSION_NAME = "flxProvider"

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

    @Deprecated(APK_STUBS_DEPRECATED_MESSAGE)
    var stubs: Stubs? = null
        internal set

    internal var providerClassName: String? = null

    /** The name of the provider. Defaults to the project name - [Project.getName]. */
    val providerName: Property<String> = project.objects.property(String::class.java)
        .convention(project.name)

    /** Determines whether the provider is an adult-only provider. Defaults to false. */
    val adult: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

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
     * Toggle this if this provider has its own resources. Defaults to false.
     */
    val requiresResources: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)
    /**
     * The current status of this provider. Defaults to Beta.
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
     * Set this if the provider is still on beta. Defaults to false.
     * */
    val excludeFromUpdaterJson: Property<Boolean> =
        project.objects.property(Boolean::class.java).convention(false)

    @Deprecated(APK_STUBS_DEPRECATED_MESSAGE)
    val userCache = project.gradle.gradleUserHomeDir
        .resolve("caches")

    /**
     * Adds an author to the list of authors.
     *
     * @param name The name of the author.
     * @param image The optional image or icon associated with the author.
     * @param socialLink The optional link associated with the author's social.
     */
    fun author(
        name: String,
        image: String? = null,
        socialLink: String? = null
    ) {
        authors.add(
            Author(
                name = name,
                image = image,
                socialLink = socialLink
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

@Deprecated(APK_STUBS_DEPRECATED_MESSAGE)
data class GithubData(
    val owner: String,
    val repository: String,
    val tag: String,
) {
    companion object {
        private const val DEFAULT_GITHUB_REPOSITORY_OWNER = "rhenwinch"
        private const val DEFAULT_GITHUB_REPOSITORY = "Flixclusive"
        private const val DEFAULT_GITHUB_RELEASE_TAG = "pre-release"

        @Deprecated(APK_STUBS_DEPRECATED_MESSAGE)
        fun Dependency.toGithubData(): GithubData
            = GithubData(
                owner = group ?: DEFAULT_GITHUB_REPOSITORY_OWNER,
                repository = name ?: DEFAULT_GITHUB_REPOSITORY,
                tag = version ?: DEFAULT_GITHUB_RELEASE_TAG,
            )
    }
}


@Deprecated(APK_STUBS_DEPRECATED_MESSAGE)
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
    return getByName(FLX_PROVIDER_EXTENSION_NAME) as FlixclusiveProviderExtension
}

fun ExtensionContainer.findFlixclusive(): FlixclusiveProviderExtension? {
    return findByName(FLX_PROVIDER_EXTENSION_NAME) as FlixclusiveProviderExtension?
}