package com.klaxit.hiddensecrets

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HideSecretTask : DefaultTask() {

    @TaskAction
    fun hideSecret() {
        println("try to hide secret")
    }
}