package com.flixclusive.gradle.entities

/**
 * Represents the language of a plugin.
 *
 * @param languageCode The shorthand code representing the language (e.g., "en", "fr", "ph").
 *
 * @see Multiple
 * @see Specific
 */
sealed class Language(val languageCode: String? = null) {
    /**
     * Represents a plugin with multiple languages.
     */
    data object Multiple : Language()

    /**
     * Represents a plugin with a specific language.
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
