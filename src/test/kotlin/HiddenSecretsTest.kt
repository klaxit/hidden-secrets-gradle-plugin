import com.klaxit.hiddensecrets.HiddenSecretsPlugin
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.WordSpec
import org.gradle.testfixtures.ProjectBuilder

class HiddenSecretsTest : WordSpec ({

    "Using the Plugin ID" should {
        "Apply the Plugin" {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("com.klaxit.HiddenSecrets")

            val plugin = project.plugins.getPlugin(HiddenSecretsPlugin::class.java)
            plugin shouldNotBe  null
        }
    }
})