# Keep JavaScript bridge methods
-keep class com.kallichi.temple.MainActivity$AndroidBridge { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
# Keep WebView related classes
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
