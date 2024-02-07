package com.flixclusive.gradle.entities

/**
 * Represents the type of plugin.
 * This sealed class defines various types of plugins such as Movies, TV shows, and custom types.
 *
 * @param customType Optional custom type for user-defined plugin types.
 *
 * @see All
 * @see Movies
 * @see TvShows
 * @see Custom
 */
sealed class PluginType(
    val customType: String? = null
) {
    /** Represents a plugin type that includes all types of plugins. */
    data object All : PluginType()

    /** Represents a plugin type specifically for movies. */
    data object Movies : PluginType()

    /** Represents a plugin type specifically for TV shows. */
    data object TvShows : PluginType()

    /**
     * Represents a custom plugin type defined by the user.
     *
     * @param customType The custom type for user-defined plugin types.
     */
    class Custom(customType: String) : PluginType(customType)

    override fun toString(): String {
        return when(this) {
            is Custom -> customType!!
            else -> super.toString()
        }
    }
}
