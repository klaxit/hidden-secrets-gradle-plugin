package YOUR_PACKAGE_GOES_HERE

class Secrets {

    //Method calls will be added by gradle task addObfuscatedKey
    //external fun getWellHiddenSecret(packageName: String): String

    companion object {
        init {
            System.loadLibrary("secrets")
        }
    }
}