# Add project specific ProGuard rules here.
-keep class com.socialmedia.app.data.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
