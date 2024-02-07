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
 * Represents an author entity with associated information such as name, user link, and Discord ID.
 *
 * @property name The name of the author.
 * @property userLink The optional link associated with the author's profile.
 * @property discordId The optional Discord ID of the author.
 */
data class Author(
    val name: String,
    val userLink: String? = null,
    val discordId: Long? = null,
)

/**
 * Represents the manifest information of a plugin.
 *
 * @property pluginClassName The fully qualified class name of the plugin.
 * @property name The name of the plugin.
 * @property version The version of the plugin.
 * @property requiresResources Indicates whether the plugin requires resources from the main application/apk.
 */
data class PluginManifest(
    val pluginClassName: String,
    val name: String,
    val version: String,
    val requiresResources: Boolean,
    val updateUrl: String?,
)

