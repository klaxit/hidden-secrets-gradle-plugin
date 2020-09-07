# Gradle plugin to deeply hide secrets on Android

This repository is the gradle plugin version of this POC : [HiddenSecrets](https://github.com/klaxit/HiddenSecrets)

It is highly inspired from https://www.splinter.com.au/2014/09/16/storing-secret-keys/

It uses a combination of obfuscation techniques to do so :
- secret is obfuscated using the reversible XOR operator so it never appears in plain sight,
- obfuscated secret is stored in a NDK binary as an hexadecimal array, so it is really hard to spot / put together from a disassembly,
- the obfuscating string is not persisted in the binary to force runtime evaluation (ie : prevent the compiler from disclosing the secret by optimizing the de-obfuscation logic).

⚠️ Nothing on the client-side is unbreakable. So generally speaking, **keeping a secret in a mobile package is not a smart idea**. But when you absolutely need to, this is the best method we have found to hide it.

## This is a kotlin gradle plugin
This project is also a demonstration on how to create a full Kotlin gradle plugin for Android projects.

## Compatibility
This gradle plugin can be used with any Android project in Java or Kotlin.

# 1 - Get the plugin

You can build the `.jar` file from the code as explained below, or directly get it from [releases](https://github.com/klaxit/hidden-secrets-gradle-plugin/releases).

## Build it
Checkout the code and run `gradle build` to create the `.jar` file in `/build/libs/`.

## Copy the plugin
Copy `HiddenSecretsPlugin-1.0.0.jar` from the gradle plugin folder `/build/libs/` to your Android project in `/app/libs/`.

## Enable the plugin in your project

Add these line in your app level `build.gradle`:

```gradle
buildscript {
    // Folder app/libs/ will be crawled
    repositories {
        flatDir { dirs 'libs' }
    }
    // Add dependency to HiddenSecretsPlugin
    dependencies {
        classpath("com.klaxit.hiddensecrets.gradle:HiddenSecretsPlugin:1.0.0")
    }
}

android {

    ...

    // Enable NDK build
    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
        }
    }
}

...

// Apply HiddenSecretsPlugin to the project
apply plugin: 'com.klaxit.hiddensecrets'
```

Then sync your Android project.

# 2 - Hide secret key in your project

Prepare your project by adding required files :
```shell
gradle setupHiddenSecrets -Ppackage=com.your.package
```

Obfuscate and hide your key in your project :
```shell
gradle hideSecretKey -Pkey=yourKeyToObfuscate -PkeyName=YourSecretKeyName -Ppackage=com.your.package
```

# 3 - Get your secret key in your app
👏 You can now get your secret key from Java/Kotlin code by calling :
```kotlin
// Kotlin
val key = Secrets().getYourSecretKeyName(packageName)
```
```Java
// Java
String key = new Secrets().getYourSecretKeyName(getPackageName());
```

# 4 - (Optional) Improve your key security
You can improve the security of your keys by using your own custom encoding / decoding algorithm. The keys will be persisted in C++, additionally encoded using your custom algorithm. The decoding algorithm will also be compiled. So an attacker will also have to reverse-engineer it from compiled C++ to find your keys.

As an example, we will use a [rot13 algorithm](https://en.wikipedia.org/wiki/ROT13) to encode / decode our key. Of course, don't use rot13 in your own project, it won't provide any additional security. Find your own "secret" encoding/decoding algorithm!

After a rot13 encoding your key `yourKeyToObfuscate` becomes `lbheXrlGbBoshfpngr`.
Add it in your app :
```shell
gradle hideSecretKey -Pkey=lbheXrlGbBoshfpngr -PkeyName=YourSecretKeyName -Ppackage=com.your.package
```

Then in `secrets.cpp` you need to add your own decoding code in `customDecode` method:
```cpp
void customDecode(char *str) {
    int c = 13;
    int l = strlen(str);
    const char *alpha[2] = { "abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLMNOPQRSTUVWXYZ"};
    int i;
    for (i = 0; i < l; i++)
    {
        if (!isalpha(str[i]))
            continue;
        if (isupper(str[i]))
            str[i] = alpha[1][((int)(tolower(str[i]) - 'a') + c) % 26];
        else
            str[i] = alpha[0][((int)(tolower(str[i]) - 'a') + c) % 26];
    }
}
```

This method is automatically called and will revert the rot13 applied on your key when you will call :
```kotlin
Secrets().getYourSecretKeyName(packageName)
```

## Other available commands
Unzip `.jar` file in `/build/` temporary directory :
```shell
gradle unzipHiddenSecrets
```

Copy required files to your project :
```shell
gradle copyCpp
gradle copyKotlin -Ppackage=your.package.name
```

Create an obfuscated key and display it :
```shell
gradle obfuscateKey -Pkey=yourKeyToObfuscate -Ppackage=com.your.package
```

## Development

Pull Requests are very welcome!

Please make sure that you have tested your code carefully before opening a PR, and make sure as well that you have no style issues.

## Authors

See the list of [contributors](https://github.com/klaxit/hidden-secrets-gradle-plugin/contributors) who participated in this project.

## License

Please see [LICENSE](https://github.com/klaxit/hidden-secrets-gradle-plugin/blob/master/LICENSE)
