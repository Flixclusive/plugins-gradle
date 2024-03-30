package com.flixclusive.gradle.entities

import kotlinx.serialization.Serializable

/**
 * Represents the language of a provider.
 *
 * @param languageCode The shorthand code representing the language (e.g., "en", "fr", "ph") or "Multiple" for providers with multiple languages.
 */
@Serializable
data class Language(val languageCode: String) {
    companion object {
        val Multiple = Language("Multiple")
    }

    override fun toString(): String {
        return languageCode
    }
}

