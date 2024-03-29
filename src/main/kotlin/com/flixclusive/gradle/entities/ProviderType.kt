package com.flixclusive.gradle.entities

/**
 * Represents the type of provider.
 * This sealed class defines various types of providers such as Movies, TV shows, and custom types.
 *
 * @param customType Optional custom type for user-defined provider types.
 *
 * @see All
 * @see Movies
 * @see TvShows
 * @see Custom
 */
sealed class ProviderType(val type: String) {
    /** Represents a provider type that includes all types of providers. */
    object All : ProviderType(type = "All")

    /** Represents a provider type specifically for movies. */
    object Movies : ProviderType(type = "Movies")

    /** Represents a provider type specifically for TV shows. */
    object TvShows : ProviderType(type = "TV Shows")

    /**
     * Represents a custom provider type defined by the user.
     *
     * @param customType The custom type for user-defined provider types.
     */
    class Custom(customType: String) : ProviderType(type = customType)

    override fun toString(): String {
        return type
    }
}
