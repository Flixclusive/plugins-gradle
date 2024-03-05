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
 * Represents the data associated with a plugin.
 *
 * @property authors The list of [Author]s who contributed to the plugin.
 * @property repositoryUrl The main repository URL of the plugin, if available.
 * @property buildUrl The URL for downloading the plugin build.
 * @property changelog The changelog of the plugin, if available.
 * @property changelogMedia The media associated with the changelog, if available.
 * @property versionName The version name of the plugin.
 * @property versionCode The version code of the plugin.
 * @property description The description of the plugin.
 * @property iconUrl The URL to the icon/image associated with the plugin, if available.
 * @property language The primary [Language] supported by this plugin.
 * @property name The name of the plugin.
 * @property pluginType The [PluginType] of the plugin.
 * @property status The [Status] of the plugin.
 *
 * @see Status
 * @see Language
 * @see PluginType
 * @see Author
 */
data class PluginData(
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
    val pluginType: PluginType?,
    val status: Status,
)
