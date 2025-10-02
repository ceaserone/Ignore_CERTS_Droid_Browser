# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/user/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit that file to add flags that are common for all your projects

-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

-keep class com.google.gson.examples.android.model.** { *; }