package com.example.nobadcertsmf;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlInput;
    private ProgressBar progress;
    private ImageButton backBtn, fwdBtn;
    private Button settingsBtn, clipBtn, goBtn;

    private SharedPreferences prefs;

    private boolean ignoreSsl, enableJs, enableImages, enableDom,
            allowMixed, showZoom, desktopUa, darkTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        prefs = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);
        boolean dark = prefs.getBoolean(SettingsActivity.KEY_DARK_THEME, true);
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPrefs();

        urlInput    = findViewById(R.id.urlInput);
        progress    = findViewById(R.id.progress);
        backBtn     = findViewById(R.id.backBtn);
        fwdBtn      = findViewById(R.id.fwdBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        clipBtn     = findViewById(R.id.clipBtn);
        goBtn       = findViewById(R.id.goBtn);
        webView     = findViewById(R.id.webview);

        applyWebSettings();

        // Copy URL
        clipBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            android.content.ClipboardManager cm =
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(android.content.ClipData.newPlainText("URL", url));
            Toast.makeText(this, "Copied URL", Toast.LENGTH_SHORT).show();
        });

        // WebView clients
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (ignoreSsl) handler.proceed(); else handler.cancel();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) { return false; }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) { return false; } // pre-L

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress.setProgress(0);
                urlInput.setText(url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                Toast.makeText(MainActivity.this,
                        "Load error: " + err.getDescription(),
                        Toast.LENGTH_SHORT).show();
                super.onReceivedError(view, req, err);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity.this,
                        "Load error: " + description,
                        Toast.LENGTH_SHORT).show();
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int newProgress) {
                progress.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }
        });

        // GO button + IME action
        goBtn.setOnClickListener(v -> loadUrlFromInput());
        urlInput.setImeOptions(EditorInfo.IME_ACTION_GO);
        urlInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnter = actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN);
            if (isEnter) { loadUrlFromInput(); return true; }
            return false;
        });

        // Navigation
        backBtn.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        fwdBtn.setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });

        // Settings button shows overflow (Ignore SSL toggle + Settings)
        settingsBtn.setOnClickListener(this::showOverflowMenu);

        // Launch target
        if (getIntent() != null && Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
            urlInput.setText(getIntent().getData().toString());
            loadUrlFromInput();
        } else {
            urlInput.setText("");  // user types
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean newDark = prefs.getBoolean(SettingsActivity.KEY_DARK_THEME, true);
        if (newDark != darkTheme) {
            AppCompatDelegate.setDefaultNightMode(
                    newDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate();
            return;
        }
        loadPrefs();
        applyWebSettings();
    }

    private void showOverflowMenu(View anchor) {
        PopupMenu pm = new PopupMenu(this, anchor);
        pm.getMenuInflater().inflate(R.menu.main_menu, pm.getMenu());
        MenuItem sslItem = pm.getMenu().findItem(R.id.menu_ssl_toggle);
        if (sslItem != null) sslItem.setChecked(ignoreSsl);

        pm.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_ssl_toggle) {
                boolean newState = !item.isChecked();
                item.setChecked(newState);
                prefs.edit().putBoolean(SettingsActivity.KEY_IGNORE_SSL, newState).apply();
                ignoreSsl = newState;
                Toast.makeText(this, "Ignore SSL: " + (newState ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
        pm.show();
    }

    private void loadPrefs() {
        ignoreSsl   = prefs.getBoolean(SettingsActivity.KEY_IGNORE_SSL, false);
        enableJs    = prefs.getBoolean(SettingsActivity.KEY_JS, true);
        enableImages= prefs.getBoolean(SettingsActivity.KEY_IMAGES, true);
        enableDom   = prefs.getBoolean(SettingsActivity.KEY_DOM_STORAGE, true);
        allowMixed  = prefs.getBoolean(SettingsActivity.KEY_MIXED, false);
        showZoom    = prefs.getBoolean(SettingsActivity.KEY_ZOOM_CTRL, true);
        desktopUa   = prefs.getBoolean(SettingsActivity.KEY_DESKTOP_UA, false);
        darkTheme   = prefs.getBoolean(SettingsActivity.KEY_DARK_THEME, true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void applyWebSettings() {
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(enableJs);
        ws.setLoadsImagesAutomatically(enableImages);
        ws.setDomStorageEnabled(enableDom);
        ws.setDatabaseEnabled(enableDom);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(showZoom);

        ws.setMixedContentMode(allowMixed
                ? WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                : WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        if (desktopUa) {
            ws.setUserAgentString(
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"
            );
        } else {
            ws.setUserAgentString(null);
        }

        WebView.setWebContentsDebuggingEnabled(true);
    }

    /** Heuristic: treat obvious non-URLs as a search query. */
    private boolean isProbablyUrl(String s) {
        if (s == null || s.isEmpty()) return false;
        String t = s.trim();

        // Contains spaces -> likely a query
        if (t.contains(" ")) return false;

        // If it starts with a scheme, it's a URL
        if (t.matches("^(?i)(http|https)://.*")) return true;

        // Looks like domain? check a dot and allowed hostname chars
        boolean hasDot = t.contains(".");
        boolean allowed = Pattern.matches("^[A-Za-z0-9._~%+\\-:/?#\\[\\]@!$&'()*+,;=]+$", t);
        return hasDot && allowed;
    }

    private void loadUrlFromInput() {
        String raw = urlInput.getText().toString().trim();
        if (raw.isEmpty()) return;

        String toLoad;
        if (isProbablyUrl(raw)) {
            // Add scheme if missing
            if (!raw.startsWith("http://") && !raw.startsWith("https://")) {
                toLoad = "https://" + raw;
            } else {
                toLoad = raw;
            }
        } else {
            // Treat as search query
            String q = URLEncoder.encode(raw, StandardCharsets.UTF_8);
            toLoad = "https://www.google.com/search?q=" + q;
        }

        String current = webView.getUrl();
        if (current != null && current.equals(toLoad)) {
            webView.reload();
        } else {
            webView.loadUrl(toLoad);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}