package com.klaxit.hiddensecrets.task

import org.gradle.api.tasks.TaskAction

/**
 * Obfuscate a key, does not add it to your project. Useful to debug.
 */
open class ObfuscateTask: BaseTask() {

    @TaskAction
    fun obfuscate() {
        getObfuscatedKey()
    }
}
