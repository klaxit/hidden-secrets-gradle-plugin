import com.klaxit.hiddensecrets.HiddenSecretsPlugin
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.WordSpec
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder

class HiddenSecretsTest : WordSpec({

    "Using the Plugin ID" should {
        "Apply the Plugin" {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("com.klaxit.HiddenSecrets")

            val plugin = project.plugins.getPlugin(HiddenSecretsPlugin::class.java)
            plugin shouldNotBe null
        }
    }

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
        "Make command copyKotlin works" {
            val result = gradleRunner.withArguments("copyCpp").build()
            println(result.output)
        }
    }
})