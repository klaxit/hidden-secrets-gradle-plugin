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

You can build the `.jar` file from the code as explained below, or directly get it from [hidden-secrets-gradle-plugin releases](https://github.com/klaxit/hidden-secrets-gradle-plugin/releases)

## Build it
Checkout the code and run `gradle build` to create the `.jar` file in `/build/libs/`.

## Copy the plugin
Copy `HiddenSecretsPlugin-1.0.0.jar` from the gradle plugin folder `/build/libs/` to your Android project in `/app/libs/`.

## Enable the plugin in your project

Add these line in your app level `build.gradle`:
```
buildscript {
    # Folder app/libs/ will be crawled
    repositories {
        flatDir { dirs 'libs' }
    }
    # Add dependency to HiddenSecretsPlugin
    dependencies {
        classpath("com.klaxit.hiddensecrets.gradle:HiddenSecretsPlugin:1.0.0")
    }
}

android {

    ...

    # Enable NDK build
    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
        }
    }
}

...

# Apply HiddenSecretsPlugin to the project
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

# 3 - Ge your secret key in your app
👏 You can now get your secret key from Java/Kotlin code by calling :
```kotlin
Secrets().getYourSecretKeyName(packageName)
```

# Other available commands
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

# Authors

See the list of [contributors](https://github.com/klaxit/micro_bench/contributors) who participated in this project.

## License

Please see [LICENSE](https://github.com/klaxit/hidden-secrets-gradle-plugin/blob/master/LICENSE)
