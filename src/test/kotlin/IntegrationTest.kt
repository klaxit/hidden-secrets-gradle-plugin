import com.klaxit.hiddensecrets.HiddenSecretsPlugin
import io.kotest.core.spec.style.WordSpec
import io.kotest.data.Row4
import io.kotest.data.Row5
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Test that correct names of keys are present in java and cpp files.
 *
 * From @pratclot
 * https://github.com/klaxit/hidden-secrets-gradle-plugin/pull/64
 */
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

        val test = this
        "Make command ${HiddenSecretsPlugin.TASK_HIDE_SECRET} succeed" {
            test.withData(
                Row5("thisIsATestKey", "thisIsATestKeyName", "thisIsATestKeyName", "com.package.test", "certKey1"),
                Row5(
                    "this_is_a_test_key",
                    "this_is_a_test_key_name",
                    "this_1is_1a_1test_1key_1name",
                    "com.package.test",
                    "cert_key1"
                ),
            ) { (key, keyName, cppKeyName, packageName, certKey) ->
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
                            "-Ppackage=$packageName",
                            "-PcertKey=$certKey",
                        )
                        .build()
                    println(result.output)

                    // Search key name in java
                    var javaCorrectNameFound = false
                    fileJava.bufferedReader().forEachLine {
                        if (it.contains(keyName)) {
                            javaCorrectNameFound = true
                        }
                    }
                    javaCorrectNameFound shouldBe true
                    // Search function name in ccp
                    val expectedCppFunctionName = "Java_${packageName.replace('.', '_')}_Secrets_get$cppKeyName("
                    println("expectedFunctionName: $expectedCppFunctionName")
                    var cppCorrectNameFound = false
                    fileCpp.bufferedReader().forEachLine {
                        if (it.contains(expectedCppFunctionName)) {
                            cppCorrectNameFound = true
                        }
                    }
                    cppCorrectNameFound shouldBe true

                    fileJava.delete()
                    fileCpp.delete()
                    packageDirJava.delete()
                    packageDirCpp.delete()
                }
            }
        }
    }
})
