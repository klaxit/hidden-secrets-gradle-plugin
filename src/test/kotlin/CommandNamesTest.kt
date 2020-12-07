import com.klaxit.hiddensecrets.HiddenSecretsPlugin
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Test that command names did not change.
 */
class CommandNamesTest : StringSpec({
    HiddenSecretsPlugin.TASK_UNZIP_HIDDEN_SECRETS shouldBe  "unzipHiddenSecrets"
    HiddenSecretsPlugin.TASK_COPY_CPP shouldBe "copyCpp"
    HiddenSecretsPlugin.TASK_COPY_KOTLIN shouldBe "copyKotlin"
    HiddenSecretsPlugin.TASK_HIDE_SECRET shouldBe "hideSecret"
    HiddenSecretsPlugin.TASK_OBFUSCATE shouldBe "obfuscate"
    HiddenSecretsPlugin.TASK_PACKAGE_NAME shouldBe "packageName"
})