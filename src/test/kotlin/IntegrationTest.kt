import com.klaxit.hiddensecrets.HiddenSecretsPlugin
import io.kotest.core.spec.style.WordSpec
import io.kotest.data.Row4
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import java.io.File

class IntegrationTest : WordSpec({

    "Apply the plugin" should {
        val testProjectDir = TemporaryFolder()
        testProjectDir.create()
        val buildFile = testProjectDir.newFile("build.gradle")
        buildFile.appendText(
            """
        plugins {
            id 'com.klaxit.hiddensecrets'
            id 'com.android.application'
        }
        android {
            compileSdkVersion 29
        }
        """.trimIndent()
        )

        val gradleRunner = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withTestKitDir(testProjectDir.newFolder())

        withData(
            Row4("thisIsATestKey", "thisIsATestKeyName", "thisIsATestKeyName", "com.package.test"),
            Row4("this_is_a_test_key", "this_is_a_test_key_name", "this_1is_1a_1test_1key_1name", "com.package.test"),
        ) { (key, keyName, keyNameCorrected, packageName) ->
            "Make command ${HiddenSecretsPlugin.TASK_HIDE_SECRET} succeed" {
                testProjectDir.run {
                    val packagePath = packageName.replace('.', '/')
                    val packageDirJava = newFolder("src/main/java/$packagePath")
                    val packageDirCpp = newFolder("src/main/cpp/")

                    val fileJava = File(packageDirJava, "Secrets.kt")
                    val fileCpp = File(packageDirCpp, "secrets.cpp")

                    var inputStream = javaClass.classLoader.getResourceAsStream("kotlin/Secrets.kt")
                    inputStream?.bufferedReader()?.lines()?.forEach {
                        fileJava.appendText(it + "\n")
                    }
                    inputStream?.close()

                    inputStream = javaClass.classLoader.getResourceAsStream("cpp/secrets.cpp")
                    inputStream?.bufferedReader()?.forEachLine {
                        fileCpp.appendText(it + "\n")
                    }
                    inputStream?.close()

                    gradleRunner.withArguments()
                    val result = gradleRunner
                        .withArguments(
                            HiddenSecretsPlugin.TASK_HIDE_SECRET,
                            "-Pkey=$key",
                            "-PkeyName=$keyName",
                            "-Ppackage=$packageName"
                        )
                        .build()
                    println(result.output)

                    var correctFunNameNotFound = true
                    val expectedFunctionName = "Java_${packageName.replace('.', '_')}_Secrets_get$keyNameCorrected("
                    println("expectedFunctionName: $expectedFunctionName")

                    fileCpp.bufferedReader().forEachLine {
                        if (correctFunNameNotFound && it.contains(keyNameCorrected)) {
                            if (it.contains(expectedFunctionName)) {
                                correctFunNameNotFound = false
                            }
                        }
                    }
                    correctFunNameNotFound shouldBe false

                    fileJava.delete()
                    fileCpp.delete()
                    packageDirJava.delete()
                    packageDirCpp.delete()
                }
            }
        }
    }
})
