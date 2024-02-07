package com.flixclusive.gradle.entities

/**
 * Represents the status of a plugin.
 *
 * @see Down
 * @see Maintenance
 * @see Beta
 * @see Working
 */
enum class Status {
    /** Indicates that the plugin is currently down. */
    Down,

    /** Indicates that the plugin is under maintenance. */
    Maintenance,

    /** Indicates that the plugin is in beta testing. */
    Beta,

    /** Indicates that the plugin is working without issues. */
    Working
}
