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

import kotlinx.serialization.Serializable

/**
 * Represents the data associated with a provider.
 *
 * @property authors The list of [Author]s who contributed to the provider.
 * @property repositoryUrl The main repository URL of the provider, if available.
 * @property buildUrl The URL for downloading the provider build.
 * @property changelog The changelog of the provider, if available.
 * @property changelogMedia The media associated with the changelog, if available.
 * @property versionName The version name of the provider.
 * @property versionCode The version code of the provider.
 * @property description The description of the provider.
 * @property iconUrl The URL to the icon/image associated with the provider, if available.
 * @property language The primary [Language] supported by this provider.
 * @property name The name of the provider.
 * @property providerType The [ProviderType] of the provider.
 * @property status The [Status] of the provider.
 *
 * @see Status
 * @see Language
 * @see ProviderType
 * @see Author
 */
@Serializable
data class ProviderData(
    val authors: List<Author>,
    val repositoryUrl: String?,
    val buildUrl: String?,
    val changelog: String? = null,
    val changelogMedia: String? = null,
    val versionName: String,
    val versionCode: Long,
    // ==================== \\
    val description: String?,
    val iconUrl: String?,
    val language: Language,
    val name: String,
    val providerType: ProviderType?,
    val status: Status,
)
