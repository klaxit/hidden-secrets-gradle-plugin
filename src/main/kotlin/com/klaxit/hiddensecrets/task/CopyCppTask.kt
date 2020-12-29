package com.klaxit.hiddensecrets.task

import org.gradle.api.tasks.TaskAction

open class CopyCppTask : BaseTask() {

    @TaskAction
    fun copyCppFiles() {
        println("Copy cpp files")
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