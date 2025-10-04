# Keep WebView reflection bits safe (usually fine without this, but harmless)
-keepclassmembers class * extends android.webkit.WebViewClient {
    public *;
}