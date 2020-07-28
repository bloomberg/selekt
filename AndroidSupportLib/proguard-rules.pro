# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-dontobfuscate

-keepattributes SourceFile,LineNumberTable,MethodParameters
-renamesourcefileattribute SourceFile

-keep public class com.bloomberg.selekt.android.support.* {
    public protected *;
}
