package com.kallichi.temple;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private ValueCallback<Uri[]> fileChooserCallback;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (fileChooserCallback == null) return;
            Uri[] results = null;
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri data = result.getData().getData();
                if (data != null) results = new Uri[]{data};
            }
            fileChooserCallback.onReceiveValue(results);
            fileChooserCallback = null;
        });

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full-screen with dark status/nav bars matching app theme
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(0xFF16120b);
        getWindow().setNavigationBarColor(0xFF0d0a06);
        WindowInsetsControllerCompat insetsCtrl = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView());
        insetsCtrl.setAppearanceLightStatusBars(false);
        insetsCtrl.setAppearanceLightNavigationBars(false);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        setupWebView();

        // Load app from assets
        webView.loadUrl("file:///android_asset/index.html");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        // Core
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);          // ← localStorage (critical for app data)
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        // Rendering
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setTextZoom(100);

        // Network
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Media
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Enable remote debugging in debug builds
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);

        // JavaScript bridge for native features
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        // Handle navigation
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Allow local files and Google Sheets API
                if (url.startsWith("file://") ||
                    url.startsWith("https://script.google.com") ||
                    url.startsWith("https://docs.google.com") ||
                    url.startsWith("https://fonts.googleapis.com") ||
                    url.startsWith("https://cdnjs.cloudflare.com")) {
                    return false;
                }
                // Open other external URLs in browser
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) {}
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                // Hide splash screen
                View splash = findViewById(R.id.splashView);
                if (splash != null) splash.setVisibility(View.GONE);
                // Inject Android platform flag so JS knows it's running in the app
                view.evaluateJavascript(
                    "window.isAndroidApp = true; " +
                    "document.body.classList.add('android-app');", null);
            }
        });

        // Handle file chooser, alerts, progress
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(WebView wv,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                if (fileChooserCallback != null) {
                    fileChooserCallback.onReceiveValue(null);
                }
                fileChooserCallback = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                filePickerLauncher.launch(Intent.createChooser(intent, "Select Backup File"));
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, 
                    android.webkit.JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton("OK", (d, w) -> result.confirm())
                    .setOnCancelListener(d -> result.cancel())
                    .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                    android.webkit.JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton("OK", (d, w) -> result.confirm())
                    .setNegativeButton("Cancel", (d, w) -> result.cancel())
                    .setOnCancelListener(d -> result.cancel())
                    .show();
                return true;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage msg) {
                return true; // suppress console in release
            }
        });
    }

    // ── JavaScript → Android bridge ──────────────────────────────────────────
    public class AndroidBridge {

        @JavascriptInterface
        public String getPlatform() {
            return "android";
        }

        @JavascriptInterface
        public boolean isOnline() {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }

        @JavascriptInterface
        public void showToast(String message) {
            runOnUiThread(() ->
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
        }

        @JavascriptInterface
        public void saveTextFile(String filename, String content) {
            runOnUiThread(() -> {
                try {
                    File dir = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS);
                    if (!dir.exists()) dir.mkdirs();
                    File file = new File(dir, filename);
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(content.getBytes("UTF-8"));
                    fos.close();
                    Toast.makeText(MainActivity.this,
                        "✅ Saved to Downloads: " + filename,
                        Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this,
                        "❌ Save failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public String getDeviceInfo() {
            return Build.MANUFACTURER + " " + Build.MODEL +
                   " (Android " + Build.VERSION.RELEASE + ")";
        }
    }

    // ── Back button: navigate WebView history ─────────────────────────────────
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Exit Kallichi Amman Temple app?")
                .setPositiveButton("Exit", (d, w) -> super.onBackPressed())
                .setNegativeButton("Stay", null)
                .show();
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        // Trigger sync when app comes back to foreground
        webView.evaluateJavascript(
            "if(typeof pullFromGSheet==='function' && typeof currentRole!=='undefined' && " +
            "currentRole && navigator.onLine){" +
            "  pullFromGSheet(function(){ " +
            "    if(typeof refreshDashboard==='function') refreshDashboard();" +
            "  });" +
            "}", null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
