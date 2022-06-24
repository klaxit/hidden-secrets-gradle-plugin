import com.klaxit.hiddensecrets.Utils
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.io.File

/**
 * Test Utils methods.
 */
class UtilsTest : WordSpec({

    fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }

    val packageName = "com.klaxit.test"

    "Using getCppPackageName()" should {
        "transform package separator" {
            Utils.getCppPackageName(packageName) shouldBe "com_klaxit_test"
        }
        "transform package with underscore" {
            Utils.getCppPackageName("com.klaxit.test_with_underscore") shouldBe "com_klaxit_test_1with_1underscore"
        }
        "transform package with escaping characters" {
            Utils.getCppPackageName("com[test.klaxit;test.test_with_underscore") shouldBe "com_3test_klaxit_2test_test_1with_1underscore"
        }
    }

    "Using sha256()" should {
        "encode String in sha256" {
            val key = "youCanNotFindMySecret!"
            Utils.sha256(key) shouldBe byteArrayOfInts( 0x7b, 0xdc, 0x2b, 0x59, 0x92, 0xef, 0x7b, 0x4c, 0xce, 0x0e, 0x06, 0x29, 0x5f, 0x56, 0x4f, 0x4f, 0xad, 0x0c, 0x96, 0xe5, 0xf8, 0x2a, 0x0b, 0xcf, 0x9c, 0xd8, 0x32, 0x3d, 0x3a, 0x3b, 0xcf, 0xbd) /*"7bdc2b5992ef7b4cce0e06295f564f4fad0c96e5f82a0bcf9cd8323d3a3bcfbd"*/
        }
    }

    "Using encodeSecret()" should {
        "encode String with a seed" {
            val key = "keyToEncode"
            Utils.encodeSecret(
                key,
                packageName
            ) shouldBe "{ 0x67, 0xcb, 0xae, 0xcb, 0x4c, 0xbb, 0x42, 0xad, 0x59, 0x19, 0xe2 }" //"{ 0x5b, 0x6, 0x18, 0x31, 0xb, 0x72, 0x57, 0x5, 0x5d, 0x57, 0x3 }"
        }
        "encode String with special characters" {
            val key = "@&é(§èçà)-ù,;:=#°_*%£?./+"
            Utils.encodeSecret(
                key,
                packageName
            ) shouldBe "{ 0x4c, 0x88, 0x14, 0x36, 0xb, 0x3c, 0x8b, 0xd, 0x9e, 0xbe, 0x20, 0x95, 0xe9, 0xce, 0xbe, 0x4a, 0x94, 0xf1, 0xb2, 0x7d, 0x4c, 0x70, 0x51, 0x91, 0x69, 0x98, 0x4d, 0xf7, 0x8a, 0xbc, 0xb1, 0xa2, 0x27 }"
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
