import com.klaxit.hiddensecrets.HiddenSecretsPlugin
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder

/**
 * Test that HiddenSecrets commands are working.
 */
class HiddenSecretsTest : WordSpec({

    "Apply the plugin" should {
        val testProjectDir = TemporaryFolder()
        testProjectDir.create()
        val buildFile = testProjectDir.newFile("build.gradle")
        buildFile.appendText("""
        plugins {
            id 'com.klaxit.hiddensecrets'
            id 'com.android.application'
        }
        android {
            compileSdkVersion 29
        }
        """.trimIndent())
        val gradleRunner = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withTestKitDir(testProjectDir.newFolder())

        //Properties
        val key = "thisIsATestKey"
        val packageName = "com.package.test"

        "Make command ${HiddenSecretsPlugin.TASK_COPY_CPP} succeed" {
            val result = gradleRunner.withArguments(HiddenSecretsPlugin.TASK_COPY_CPP).build()
            println(result.output)
        }

        "Make command ${HiddenSecretsPlugin.TASK_COPY_KOTLIN} succeed" {
            val result = gradleRunner.withArguments(HiddenSecretsPlugin.TASK_COPY_KOTLIN, "-Ppackage=$packageName").build()
            println(result.output)
        }

        "Make command ${HiddenSecretsPlugin.TASK_OBFUSCATE} succeed" {
            val result = gradleRunner.withArguments(HiddenSecretsPlugin.TASK_OBFUSCATE, "-Pkey=$key", "-Ppackage=$packageName").build()
            println(result.output)
            //Should contain obfuscated key
            result.output shouldContain "{ 0x15, 0x58, 0xb, 0x43, 0x78, 0x4a, 0x23, 0x6d, 0x1, 0x4b, 0x46, 0x7c, 0x57, 0x41 }"
        }

        "Make command ${HiddenSecretsPlugin.TASK_PACKAGE_NAME} succeed" {
            val result = gradleRunner.withArguments(HiddenSecretsPlugin.TASK_PACKAGE_NAME, "-Ppackage=$packageName").build()
            println(result.output)
            result.output shouldContain packageName
        }

        "Make command ${HiddenSecretsPlugin.TASK_FIND_KOTLIN_FILE} succeed" {
            val result = gradleRunner.withArguments(HiddenSecretsPlugin.TASK_FIND_KOTLIN_FILE).build()
            println(result.output)
        }
    }
})
