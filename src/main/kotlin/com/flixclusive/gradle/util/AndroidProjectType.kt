package com.flixclusive.gradle.util

import org.gradle.api.Project

enum class AndroidProjectType {
    APPLICATION,
    LIBRARY,
    UNKNOWN
}

fun Project.getAndroidProjectType(): AndroidProjectType {
    return when {
        plugins.hasPlugin("com.android.application") -> AndroidProjectType.APPLICATION
        plugins.hasPlugin("com.android.library") -> AndroidProjectType.LIBRARY
        else -> AndroidProjectType.UNKNOWN
    }
}