package com.flixclusive.gradle.util

import com.flixclusive.gradle.FlixclusiveProviderExtension
import com.flixclusive.model.provider.ProviderManifest
import com.flixclusive.gradle.getFlixclusive
import com.flixclusive.model.provider.ProviderMetadata
import org.gradle.api.Project

internal fun Project.createProviderManifest(): ProviderManifest {
    val extension = this.extensions.getFlixclusive()
    val (versionCode, versionName) = extension.getVersionDetails()

    require(extension.providerClassName != null) {
        "No provider class found, make sure your provider class is annotated with @FlixclusiveProvider"
    }

    return ProviderManifest(
        providerClassName = extension.providerClassName!!,
        name = name,
        versionName = versionName,
        versionCode = versionCode,
        updateUrl = extension.updateUrl,
        requiresResources = extension.requiresResources,
    )
}

internal fun Project.createProviderMetadata(): ProviderMetadata {
    val extension = extensions.getFlixclusive()
    val (versionCode, versionName) = extension.getVersionDetails()

    validateId(name, extension.id)

    return ProviderMetadata(
        buildUrl = extension.buildUrl?.let { String.format(it, name) },
        status = extension.status,
        versionName = versionName,
        versionCode = versionCode,
        name = extension.providerName,
        adult = extension.adult,
        authors = extension.authors.getOrElse(emptyList()),
        description = extension.description,
        repositoryUrl = extension.repositoryUrl,
        language = extension.language,
        iconUrl = extension.iconUrl,
        providerType = extension.providerType,
        changelog = extension.changelog,
        id = extension.id
    )
}

private const val MIN_ID_LENGTH = 8
private const val MAX_ID_LENGTH = 25
private fun validateId(name: String, id: String?) {
    require(id != null) {
        "The provider '${name}' does not have an ID set. Please ensure that a VERY unique value is specified for the `id` property in the provider's `build.gradle.kts` file."
    }

    require(id.length in MIN_ID_LENGTH..MAX_ID_LENGTH) {
        "The provider '${name}' has an ID that has a length of ${id.length}. IDs must have at least $MIN_ID_LENGTH-$MAX_ID_LENGTH characters length."
    }

    val bannedCharacters = Regex("[^\\p{Cc}\\\"\\\\\\u2028\\u2029]+?")
    require(bannedCharacters.matches(id)) {
        """
            The provider '${name}' has an ID character that is invalid. Make sure it does not contain the following characters:
            - Control characters (\p{Cc})
            - Double quotes (")
            - Backslash (\)
            - Line separator (\u2028 or \n)
            - Paragraph separator (\u2029)
        """.trimIndent()
    }
}