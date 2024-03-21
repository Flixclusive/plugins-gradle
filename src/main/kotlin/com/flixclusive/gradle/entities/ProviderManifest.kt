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

package com.flixclusive.gradle.entities

/**
 * Represents an author entity with associated information such as name, github link.
 *
 * @property name The name of the author.
 * @property githubLink The optional link associated with the author's profile.
 */
data class Author(
    val name: String,
    val githubLink: String? = null,
)

/**
 * Represents the manifest information of a provider.
 *
 * @property providerClassName The fully qualified class name of the provider.
 * @property name The name of the provider.
 * @property versionName The version name of the provider.
 * @property versionCode The version code of the provider.
 * @property requiresResources Indicates whether the provider requires resources from the main application/apk.
 */
data class ProviderManifest(
    val providerClassName: String,
    val name: String,
    val versionName: String,
    val versionCode: Long,
    val requiresResources: Boolean,
    val updateUrl: String?,
)

