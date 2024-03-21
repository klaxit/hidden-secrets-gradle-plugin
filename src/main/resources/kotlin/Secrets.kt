package YOUR_PACKAGE_GOES_HERE

public class Secrets {

    // Method calls will be added by gradle task hideSecret
    // Example : external fun getWellHiddenSecret(packageName: String): String

    internal companion object {
        init {
            System.loadLibrary("secrets")
        }
    }
}