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
    HiddenSecretsPlugin.TASK_HIDE_SECRET_FROM_PROPERTIES_FILE shouldBe "hideSecretFromPropertiesFile"
    HiddenSecretsPlugin.TASK_OBFUSCATE shouldBe "obfuscate"
    HiddenSecretsPlugin.TASK_PACKAGE_NAME shouldBe "packageName"
    HiddenSecretsPlugin.TASK_FIND_KOTLIN_FILE shouldBe "findKotlinFile"
    HiddenSecretsPlugin.TASK_CERT_KEY shouldBe "certKey"
})
