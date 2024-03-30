package com.flixclusive.gradle.entities

import kotlinx.serialization.Serializable

/**
 * Represents the type of content the provider offers.
 *
 * @param type The provider type (e.g., "Movies", "TV Shows", or custom type).
 */
@Serializable
data class ProviderType(val type: String) {
    companion object {
        val All = ProviderType("Movies, TV Shows, etc.")
        val Movies = ProviderType("Movies")
        val TvShows = ProviderType("TV Shows")
    }

    override fun equals(other: Any?): Boolean {
        return when(other) {
            is ProviderType -> other.type.equals(type, true)
            is String -> other.equals(type, true)
            else -> super.equals(other)
        }
    }

    override fun toString(): String {
        return type
    }

    override fun hashCode(): Int {
        return type.hashCode() * 31
    }
}

