package com.example.nobadcertsmf;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS = "settings";
    public static final String KEY_JS          = "javascript_enabled";
    public static final String KEY_IMAGES      = "images_enabled";
    public static final String KEY_IGNORE_SSL  = "ignore_ssl_errors";
    public static final String KEY_DOM_STORAGE = "dom_storage_enabled";
    public static final String KEY_MIXED       = "mixed_content_allowed";
    public static final String KEY_DESKTOP_UA  = "desktop_user_agent";
    public static final String KEY_ZOOM_CTRL   = "zoom_controls_visible";
    public static final String KEY_DARK_THEME  = "dark_theme_enabled";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean dark = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_DARK_THEME, true);
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        SwitchMaterial sJs        = findViewById(R.id.javascript_switch);
        SwitchMaterial sImages    = findViewById(R.id.images_switch);
        SwitchMaterial sIgnoreSsl = findViewById(R.id.ignore_ssl_switch);
        SwitchMaterial sDom       = findViewById(R.id.dom_storage_switch);
        SwitchMaterial sMixed     = findViewById(R.id.mixed_content_switch);
        SwitchMaterial sDesktopUa = findViewById(R.id.desktop_ua_switch);
        SwitchMaterial sZoom      = findViewById(R.id.zoom_controls_switch);
        SwitchMaterial sDark      = findViewById(R.id.dark_theme_switch);
        MaterialButton btnCookies = findViewById(R.id.clear_cookies_button);

        sJs.setChecked(prefs.getBoolean(KEY_JS, true));
        sImages.setChecked(prefs.getBoolean(KEY_IMAGES, true));
        sIgnoreSsl.setChecked(prefs.getBoolean(KEY_IGNORE_SSL, false));
        sDom.setChecked(prefs.getBoolean(KEY_DOM_STORAGE, true));
        sMixed.setChecked(prefs.getBoolean(KEY_MIXED, false));
        sDesktopUa.setChecked(prefs.getBoolean(KEY_DESKTOP_UA, false));
        sZoom.setChecked(prefs.getBoolean(KEY_ZOOM_CTRL, true));
        sDark.setChecked(prefs.getBoolean(KEY_DARK_THEME, true));

        sJs.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean(KEY_JS, v).apply());
        sImages.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean(KEY_IMAGES, v).apply());
        sIgnoreSsl.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean(KEY_IGNORE_SSL, v).apply());
        sDom.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean(KEY_DOM_STORAGE, v).apply());
        sMixed.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean(KEY_MIXED, v).apply());
        sDesktopUa.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean(KEY_DESKTOP_UA, v).apply());
        sZoom.setOnCheckedChangeListener((b, v) -> prefs.edit().putBoolean(KEY_ZOOM_CTRL, v).apply());

        sDark.setOnCheckedChangeListener((b, v) -> {
            prefs.edit().putBoolean(KEY_DARK_THEME, v).apply();
            AppCompatDelegate.setDefaultNightMode(
                    v ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate();
        });

        btnCookies.setOnClickListener(v -> {
            CookieManager cm = CookieManager.getInstance();
            cm.removeAllCookies(null);
            cm.flush();
            Toast.makeText(this, "Cookies cleared", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}