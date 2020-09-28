import io.kotlintest.specs.WordSpec
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder

class HiddenSecretsTest : WordSpec({

    "Apply the plugin" should {
        val testProjectDir = TemporaryFolder()
        testProjectDir.create()
        val buildFile = testProjectDir.newFile("build.gradle")
        buildFile.appendText("""
        plugins {
            id 'com.klaxit.hiddensecrets'
        }
        """.trimIndent())
        val gradleRunner = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.root)
                .withTestKitDir(testProjectDir.newFolder())

        "Make command copyCpp works" {
            val result = gradleRunner.withArguments("copyCpp").build()
            println(result.output)
        }
    }
})