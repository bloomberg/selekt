## Compatibility

Selekt's packaged native libraries target the following ABIs:

* armeabi-v7a
* arm64-v8a
* x86
* x86_64

The eventual size of an APK can be reduced by filtering out ABIs that are not supported. This can also help prevent APKs from being installed on devices that are not otherwise supported.

### Restricting native libraries

The set of ABIs your application supports can be restricted by applying an ABI filter. For example, to support only the 64-bit ABIs:

=== "Kotlin"
    ``` kotlin
    android {
        ndk {
            abiFilters.addAll(arrayOf("x86_64", "arm64-v8a"))
        }
    }
    ```

=== "Groovy"
    ``` groovy
    android {
        ndk {
            abiFilters 'arm64-v8a', 'x86_64'
        }
    }
    ```

### Excluding native libraries

Alternatively an exclusion filter can be applied when packaging your APK. For example, for an APK that is only intended to support both ARM architectures:

=== "Kotlin"
    ``` kotlin
    android {
        packagingOptions {
            exclude("/lib/x86/*")
            exclude("/lib/x86_64/*")
        }
    }
    ```

=== "Groovy"
    ``` groovy
    android {
        packagingOptions {
            exclude '/lib/x86/*'
            exclude '/lib/x86_64/*'
        }
    }
    ```
