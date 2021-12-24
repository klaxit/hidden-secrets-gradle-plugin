[![travis ci status](https://travis-ci.org/klaxit/hidden-secrets-gradle-plugin.svg?branch=master)](https://travis-ci.com/github/klaxit/hidden-secrets-gradle-plugin/branches)
[![MIT license](https://img.shields.io/github/license/klaxit/hidden-secrets-gradle-plugin)](https://github.com/klaxit/hidden-secrets-gradle-plugin/blob/master/LICENSE)

# Gradle plugin to deeply hide secrets on Android

This plugin allows any Android developer to deeply hide secrets in its project. It is an OSS equivalent of what [DexGuard](https://www.guardsquare.com/en/products/dexguard) can offer to prevent **credentials harvesting**.

It uses a combination of obfuscation techniques to do so :
- secret is obfuscated using the reversible XOR operator so it never appears in plain sight,
- obfuscated secret is stored in a NDK binary as an hexadecimal array, so it is really hard to spot / put together from a disassembly,
- the obfuscating string is not persisted in the binary to force runtime evaluation (ie : prevent the compiler from disclosing the secret by optimizing the de-obfuscation logic),
- optionally, anyone can provide its own encoding / decoding algorithm when using the plugin to add an additional security layer.

This plugin is **used in production** at [Klaxit - Covoiturage quotidien](https://play.google.com/store/apps/details?id=com.wayzup.wayzupapp). Our engineering team at Klaxit will provide its best effort to maintain this project.

⚠️ Nothing on the client-side is unbreakable. So generally speaking, **keeping a secret in a mobile package is not a smart idea**. But when you absolutely need to, this is the best method we have found to hide it.

## Compatibility
This gradle plugin can be used with any Android project in Java or Kotlin.

# 1 - Install the plugin
## Using the plugins DSL
In your Module level `build.gradle`:

```gradle
plugins {
    id "com.klaxit.hiddensecrets" version "X.Y.Z"
}
```
ℹ️ If your project sync triggers the issue `Could not find com.android.tools.build:gradle:X.Y.Z`, please use the legacy plugin application below.
## Using legacy plugin application
Add these lines at the beginning of your Module level `build.gradle`:

```gradle
buildscript {
    repositories {
        google()
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "com.klaxit.hiddensecrets:HiddenSecretsPlugin:X.Y.Z"
    }
}

apply plugin: 'com.klaxit.hiddensecrets'
```

For more details about the installation check the [plugin's page](https://plugins.gradle.org/plugin/com.klaxit.hiddensecrets) on gradle.org.

# 2 - Hide secret key in your project

Obfuscate and hide your key in your project :
```shell
./gradlew hideSecret -Pkey=yourKeyToObfuscate [-PkeyName=YourSecretKeyName] [-Ppackage=com.your.package]
```
The parameter `keyName` is optional, by default the key name is randomly generated.
The parameter `package` is optional, by default the `applicationId` of your project will be used.

# 3 - Get your secret key in your app
Enable C++ files compilation by adding this lines in the app level `build.gradle` :
```gradle
android {

    ...

    // Enable NDK build
    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
        }
    }
}
```

👏 Now you can get your secret key from Java/Kotlin code by calling :
```kotlin
// Kotlin
val key = Secrets().getYourSecretKeyName(packageName)
```
```Java
// Java
final String key = new Secrets().getYourSecretKeyName(getPackageName());
```

# 4 - (Optional) Improve your key security
You can improve the security of your keys by using your own custom encoding / decoding algorithm. The keys will be persisted in C++, additionally encoded using your custom algorithm. The decoding algorithm will also be compiled. So an attacker will also have to reverse-engineer it from compiled C++ to find your keys.

As an example, we will use a [rot13 algorithm](https://en.wikipedia.org/wiki/ROT13) to encode / decode our key. Of course, don't use rot13 in your own project, it won't provide any additional security. Find your own "secret" encoding/decoding algorithm!

After a rot13 encoding your key `yourKeyToObfuscate` becomes `lbheXrlGbBoshfpngr`.
Add it in your app :
```shell
./gradlew hideSecret -Pkey=lbheXrlGbBoshfpngr -PkeyName=YourSecretKeyName
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

# Going further
## Generate secrets from properties file
You can generate secrets from properties file as well. 

### Use case
If you are using CI system to provide secrets, that are not hard-coded into the repository itself. It will re-generate those secrets in the app and build it. 

This is useful if you want to split production keys from repository itself, thus increasing security in your project repository. 

### Setting up
1. Create a new properties file in root project directory. 

``` shell
credentials.properties
```

2. Fill in wanted secrets. For ex.: 

``` java-properties
keyName1=yourKeyToObfuscate1
keyName2=yourKeyToObfuscate2
```

3. Run

``` shell
./gradlew hideSecretFromPropertiesFile -PpropertiesFileName=credentials.properties
```

It will regenerate all secret files in the project and update all secrets from the properties file.

# Other available commands

## Copy files
Copy required files to your project :
```shell
./gradlew copyCpp
./gradlew copyKotlin [-Ppackage=your.package.name]
```

## Obfuscate
Create an obfuscated key and display it :
```shell
./gradlew obfuscate -Pkey=yourKeyToObfuscate [-Ppackage=com.your.package]
```
This command can be useful if you modify your app's package name based on `buildTypes` configuration. With this command you can get the obfuscated key for a different package name and manually integrate it in another function in `secrets.cpp`.

# Development

Pull Requests are very welcome!

To get started, checkout the code and run `./gradlew build` to create the `.jar` file in `/build/libs/`.

Before opening a PR :
- make sure that you have tested your code carefully
- `./gradlew test` must succeed
- `./gradlew detekt` must succeed to avoid any style issue

# Authors

See the list of [contributors](https://github.com/klaxit/hidden-secrets-gradle-plugin/contributors) who participated in this project.

# License

Please see [LICENSE](https://github.com/klaxit/hidden-secrets-gradle-plugin/blob/master/LICENSE)

