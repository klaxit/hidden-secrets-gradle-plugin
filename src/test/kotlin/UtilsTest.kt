import com.klaxit.hiddensecrets.Utils
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.io.File

/**
 * Test Utils methods.
 */
class UtilsTest : WordSpec({

    val packageName = "com.klaxit.test"

    "Using getUnderScoredPackageName()" should {
        "transform package separator" {
            Utils.getSnakeCasePackageName(packageName) shouldBe "com_klaxit_test"
        }
    }

    "Using sha256()" should {
        "encode String in sha256" {
            val key = "youCanNotFindMySecret!"
            Utils.sha256(key) shouldBe "7bdc2b5992ef7b4cce0e06295f564f4fad0c96e5f82a0bcf9cd8323d3a3bcfbd"
        }
    }

    "Using encodeSecret()" should {
        "encode String with a seed" {
            val key = "keyToEncode"
            Utils.encodeSecret(
                key,
                packageName
            ) shouldBe "{ 0x5b, 0x6, 0x18, 0x31, 0xb, 0x72, 0x57, 0x5, 0x5d, 0x57, 0x3 }"
        }
        "encode String with special characters" {
            val key = "@&é(§èçà)-ù,;:=#°_*%£?./+"
            Utils.encodeSecret(
                key,
                packageName
            ) shouldBe "{ 0x70, 0x45, 0xa2, 0xcc, 0x4c, 0xf5, 0x9e, 0xa5, 0x9a, 0xf0, 0xc1, 0xa6, 0x92, 0x4a, 0x4e, 0xa6, 0x8a, 0x1a, 0xc, 0x5e, 0x5, 0x14, 0xf7, 0x86, 0x6b, 0x13, 0x40, 0xf5, 0x9a, 0xc, 0x16, 0x16, 0x19 }"
        }
    }

    "Using getKotlinFilePackage()" should {
        "find package name" {
            val kotlinFile = File("filename.kt")
            kotlinFile.writeText(
                "package com.test.activity\n" +
                    "\n" +
                    "import android.test.Intent\n" +
                    "import android.test.Bundle"
            )
            val kotlinPackage = Utils.getKotlinFilePackage(kotlinFile)
            kotlinFile.delete()
            kotlinPackage shouldBe "com.test.activity"
        }
        "find package name with escaping characters" {
            val kotlinFile = File("filename.kt")
            kotlinFile.writeText("package com.test.`object`\n" +
                "\n" +
                "import com.test.Hidden\n" +
                "import com.test.constant.NetworkConstants")
            val kotlinPackage = Utils.getKotlinFilePackage(kotlinFile)
            kotlinFile.delete()
            kotlinPackage shouldBe "com.test.object"
        }
    }
})
