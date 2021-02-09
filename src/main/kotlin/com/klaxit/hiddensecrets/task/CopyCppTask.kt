package com.klaxit.hiddensecrets.task

import org.gradle.api.tasks.TaskAction

/**
 * Task to copy cpp files from the lib to the Android project if they don't exist yet.
 */
open class CopyCppTask : BaseTask() {

    @TaskAction
    fun copyCppFiles() {
        project.file("$tmpFolder/cpp/").listFiles()?.forEach {
            val destination = getCppDestination(it.name)
            if (destination.exists()) {
                println("${it.name} already exists")
            } else {
                println("Copy $it.name to\n$destination")
                it.copyTo(destination, true)
            }
        }
    }
}
