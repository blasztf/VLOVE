# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class value to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file value.
#-renamesourcefileattribute SourceFile

# Depending on your ProGuard (DexGuard) config and usage,
# you may need to include the following lines in your proguard.cfg
# (see http://bumptech.github.io/glide/doc/download-setup.html#proguard for more details):
#-keep public class * implements com.bumptech.glide.module.GlideModule
#-keep public class * extends com.bumptech.glide.module.AppGlideModule
#-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
#  **[] $VALUES;
#  public *;
#}

# VideoDecoder uses API 27 APIs which may cause proguard warnings even though
# the newer APIs won’t be called on devices with older versions of Android.
# If you're targeting any API level less than Android API 27, also include:
#-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

