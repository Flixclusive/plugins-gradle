package com.flixclusive.gradle.task

import com.flixclusive.gradle.getFlixclusive
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

// Cloudstream3
abstract class CleanCacheTask : DefaultTask() {
    @TaskAction
    fun cleanCache() {
        val extension = project.extensions.getFlixclusive()
        val stubs = extension.stubs ?: return

        if (stubs.file.exists()) {
            stubs.file.delete()
        }
    }
}