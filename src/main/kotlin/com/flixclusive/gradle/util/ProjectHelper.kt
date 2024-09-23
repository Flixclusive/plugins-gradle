package com.flixclusive.gradle.util

import com.android.builder.dexing.ClassFileEntry
import com.flixclusive.model.provider.Language
import com.flixclusive.model.provider.ProviderData
import com.flixclusive.model.provider.ProviderManifest
import com.flixclusive.model.provider.ProviderType
import com.flixclusive.gradle.getFlixclusive
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

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
        name = extension.providerName.get(),
        adult = extension.adult.get(),
        authors = extension.authors.getOrElse(emptyList()),
        description = extension.description.orNull,
        repositoryUrl = extension.repositoryUrl.orNull,
        language = extension.language.getOrElse(Language(languageCode = "en")),
        iconUrl = extension.iconUrl.orNull,
        providerType = extension.providerType.getOrElse(ProviderType(type = "Unknown")),
        changelog = extension.changelog.orNull
    )
}

fun Project.findProviderClassName(files: List<ClassFileEntry>): String? {
    for (file in files) {
        val reader = ClassReader(file.readAllBytes())

        val classNode = ClassNode()
        reader.accept(classNode, 0)

        for (annotation in classNode.visibleAnnotations.orEmpty() + classNode.invisibleAnnotations.orEmpty()) {
            if (annotation.desc == "Lcom/flixclusive/provider/FlixclusiveProvider;") {
                val flixclusive = project.extensions.getFlixclusive()

                require(flixclusive.providerClassName == null) {
                    "Only 1 active provider class per project is supported"
                }

                for (method in classNode.methods) {
                    if (method.name == "getManifest" && method.desc == "()Lcom/flixclusive/provider/ProviderManifest;") {
                        throw IllegalArgumentException("Provider class cannot override getManifest, use manifest.json system!")
                    }
                }

                return classNode.name.replace('/', '.')
            }
        }
    }

    return null
}