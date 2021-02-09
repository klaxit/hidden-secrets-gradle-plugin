package com.klaxit.hiddensecrets.task

import com.klaxit.hiddensecrets.Utils
import org.gradle.api.tasks.TaskAction

/**
 * Find Secrets.kt file in the project, useful to debug.
 */
open class FindKotlinFileTask : BaseTask() {

    /**
     * Print the path to Secret.kt file in the Android project.
     */
    @TaskAction
    fun findKotlinFile() {
        Utils.getKotlinFile(project)
    }
}
