package com.klaxit.hiddensecrets

/**
 * Helper to generate C++ and Kotlin code
 */
object CodeGenerator {

    /**
     * Return the C++ code to add another key to your project
     */
    fun getCppCode(packageName: String, keyName: String, obfuscatedKey: String): String {

        return "\nextern \"C\"\n" +
                "JNIEXPORT jstring JNICALL\n" +
                "Java_" + Utils.getUnderScoredPackageName(packageName) + "_Secrets_get$keyName(\n" +
                "        JNIEnv* pEnv,\n" +
                "        jobject pThis,\n" +
                "        jstring packageName) {\n" +
                "     char obfuscatedSecret[] = $obfuscatedKey;\n" +
                "     return getOriginalKey(obfuscatedSecret, sizeof(obfuscatedSecret), packageName, pEnv);\n" +
                "}\n"
    }

    /**
     * Kotlin code that will be added in your project
     */
    fun getKotlinCode(keyName: String): String {
        return "\n    external fun get$keyName(packageName: String): String\n" +
                "}"
    }
}
