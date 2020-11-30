package com.klaxit.hiddensecrets

import com.google.common.annotations.VisibleForTesting
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.experimental.xor

object Utils {

    /**
     * Transform package name com.klaxit.hidden to com_klaxit_hidden to ingrate in C++ code
     */
    fun getUnderScoredPackageName(packageName: String): String {
        val packageComponents = packageName.split(".")
        var packageStr = ""
        val iterator: Iterator<String> = packageComponents.iterator()
        while (iterator.hasNext()) {
            packageStr += iterator.next()
            if (iterator.hasNext()) {
                packageStr += "_"
            }
        }
        return packageStr
    }

    /**
     * Encode string to sha256
     */
    @VisibleForTesting
    fun sha256(toHash: String): String {
        val bytes = toHash.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    /**
     * Encode a string key to and hex array using package name
     */
    fun encodeSecret(key: String, packageName: String): String {
        //Generate the obfuscator as the SHA256 of the app package name
        val obfuscator = sha256(packageName)
        val obfuscatorBytes = obfuscator.toByteArray()

        //Generate the obfuscated secret bytes array by applying a XOR between the secret and the obfuscator
        val obfuscatedSecretBytes = arrayListOf<Byte>()
        var i = 0
        key.toByteArray(Charset.defaultCharset()).forEach { secretByte ->
            val obfuscatorByte = obfuscatorBytes[i % obfuscatorBytes.size]
            val obfuscatedByte = secretByte.xor(obfuscatorByte)
            obfuscatedSecretBytes.add(obfuscatedByte)
            i++
        }
        var encoded = "{ "
        val iterator: Iterator<Byte> = obfuscatedSecretBytes.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            encoded += "0x" + Integer.toHexString(item.toInt() and 0xff)
            if (iterator.hasNext()) {
                encoded += ", "
            }
        }
        encoded += " }"
        return encoded
    }
}
