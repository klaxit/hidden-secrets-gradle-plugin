package com.klaxit.hiddensecrets

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.klaxit.hiddensecrets.task.*
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskAction

/**
 * Available gradle tasks from HiddenSecretsPlugin
 */
open class HiddenSecretsPlugin : Plugin<Project> {
    companion object {
        //Tasks
        const val TASK_UNZIP_HIDDEN_SECRETS = "unzipHiddenSecrets"
        const val TASK_COPY_CPP = "copyCpp"
        const val TASK_COPY_KOTLIN = "copyKotlin"
        const val TASK_HIDE_SECRET = "hideSecret"
        const val TASK_OBFUSCATE = "obfuscate"
        const val TASK_PACKAGE_NAME = "packageName"
        const val TASK_FIND_KOTLIN_FILE = "findKotlinFile"
    }

    /**
     * Create or register all tasks available in the project.
     */
    override fun apply(project: Project) {
        // Unzip plugin into tmp directory
        project.tasks.create(TASK_UNZIP_HIDDEN_SECRETS, Copy::class.java,
            object : Action<Copy?> {
                @TaskAction
                override fun execute(copy: Copy) {
                    val tmpFolder = java.lang.String.format("%s/hidden-secrets-tmp", project.buildDir)
                    // in the case of buildSrc dir
                    copy.from(project.zipTree(javaClass.protectionDomain.codeSource.location!!.toExternalForm()))
                    println("Unzip jar to $tmpFolder")
                    copy.into(tmpFolder)
                }
            })

        // Copy C++ files to your project
        project.tasks.register(TASK_COPY_CPP, CopyCppTask::class.java)

        // Copy Kotlin file to your project
        project.tasks.register(TASK_COPY_KOTLIN, CopyKotlinTask::class.java)

        // Get an obfuscated key from command line
        project.tasks.register(TASK_OBFUSCATE, ObfuscateTask::class.java)
        
        // Main task : obfuscate a key and add it to your Android project
        project.tasks.register(TASK_HIDE_SECRET, HideSecretTask::class.java).dependsOn(TASK_UNZIP_HIDDEN_SECRETS)

        // Print the package name of the app
        project.tasks.register(TASK_PACKAGE_NAME, PackageNameTask::class.java)

        // Find Secrets.kt file in the project
        project.tasks.register(TASK_FIND_KOTLIN_FILE, FindKotlinFileTask::class.java)
    }
}
