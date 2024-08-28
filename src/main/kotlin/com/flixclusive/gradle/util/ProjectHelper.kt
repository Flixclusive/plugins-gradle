package com.flixclusive.gradle.util

import com.flixclusive.gradle.entities.Language
import com.flixclusive.gradle.entities.ProviderData
import com.flixclusive.gradle.entities.ProviderManifest
import com.flixclusive.gradle.entities.ProviderType
import com.flixclusive.gradle.getFlixclusive
import org.gradle.api.Project

fun Project.createProviderManifest(): ProviderManifest {
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
        updateUrl = extension.updateUrl.orNull,
        requiresResources = extension.requiresResources.get(),
    )
}

fun Project.createProviderData(): ProviderData {
    val extension = extensions.getFlixclusive()
    val (versionCode, versionName) = extension.getVersionDetails()

    return ProviderData(
        buildUrl = extension.buildUrl.orNull?.let { String.format(it, name) },
        status = extension.status.get(),
        versionName = versionName,
        versionCode = versionCode,
        name = name,
        authors = extension.authors.getOrElse(emptyList()),
        description = extension.description.orNull,
        repositoryUrl = extension.repositoryUrl.orNull,
        language = extension.language.getOrElse(Language(languageCode = "en")),
        iconUrl = extension.iconUrl.orNull,
        providerType = extension.providerType.getOrElse(ProviderType(type = "Unknown")),
        changelog = extension.changelog.orNull
    )
}