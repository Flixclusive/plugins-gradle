package com.flixclusive.gradle.entities

/**
 * Represents the language of a provider.
 *
 * @param languageCode The shorthand code representing the language (e.g., "en", "fr", "ph").
 *
 * @see Multiple
 * @see Specific
 */
sealed class Language(val languageCode: String? = null) {
    /**
     * Represents a provider with multiple languages.
     */
    object Multiple : Language()

    /**
     * Represents a provider with a specific language.
     *
     * @param languageCode The shorthand code representing the specific language.
     */
    class Specific(languageCode: String) : Language(languageCode)

    override fun toString(): String {
        return when(this) {
            is Specific -> languageCode!!
            else -> super.toString()
        }
    }
}
