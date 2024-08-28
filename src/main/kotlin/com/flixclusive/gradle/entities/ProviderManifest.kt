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
 * Represents the manifest information of a provider.
 *
 * @property providerClassName The fully qualified class name of the provider.
 * @property name The name of the provider.
 * @property versionName The version name of the provider.
 * @property versionCode The version code of the provider.
 * @property requiresResources Indicates whether the provider requires resources from the main application/apk.
 */
@Serializable
data class ProviderManifest(
    val providerClassName: String,
    val name: String,
    val versionName: String,
    val versionCode: Long,
    val requiresResources: Boolean,
    val updateUrl: String?,
)

