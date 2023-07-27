package com.klaxit.hiddensecrets

import com.google.common.annotations.VisibleForTesting
import org.gradle.api.Project
import java.io.File
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.experimental.xor

object Utils {

    /**
     * Transform names like com.klaxit.hidden to com_klaxit_hidden to ingrate in C++ code.
     * Java package name and key names needs to escape some characters to call the NDK.
     * From https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/design.html#resolving_native_method_names
     */
    fun getCppName(packageName: String): String {
        return packageName
            .replace("_", "_1")
            .replace(";", "_2")
            .replace("[", "_3")
            .replace(".", "_")
    }

    /**
     * Encode string to sha256
     */
    @VisibleForTesting
    fun sha256(toHash: String): String {
        val bytes = toHash.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * Encode a string key to and hex array using cert key
     */
    fun encodeSecret(key: String, certKey: String): String {
        // Generate the obfuscator as the SHA256 of the app package name
        val obfuscator = sha256(certKey)
        val obfuscatorBytes = obfuscator.toByteArray()

        // Generate the obfuscated secret bytes array by applying a XOR between the secret and the obfuscator
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

    /**
     * Search a file in the finale project, can provide a path to limit the search in some folders
     */
    fun findFileInProject(project: Project, path: String, fileName: String): File? {
        val directory = project.file(path)
        directory.walkBottomUp().forEach {
            if (it.name == fileName) {
                println("$fileName found in ${it.absolutePath}\n")
                return it
            }
        }
        println("$fileName not found in $path")
        return null
    }

    /**
     * Return package from first line of a kotlin file
     */
    fun getKotlinFilePackage(file: File): String {
        var text = file.readLines(Charset.defaultCharset())[0]
        text = text.replace("package ", "")
        // Handle package name using keywords
        text = text.replace("`", "")
        println("Package : $text found in ${file.name}")
        return text
    }
}
