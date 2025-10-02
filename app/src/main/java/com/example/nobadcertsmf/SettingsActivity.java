package com.example.nobadcertsmf;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch javascriptSwitch;
    private Switch imagesSwitch;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        javascriptSwitch = findViewById(R.id.javascript_switch);
        imagesSwitch = findViewById(R.id.images_switch);

        javascriptSwitch.setChecked(prefs.getBoolean("javascript_enabled", true));
        imagesSwitch.setChecked(prefs.getBoolean("images_enabled", true));

        javascriptSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("javascript_enabled", isChecked).apply();
            }
        });

        imagesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("images_enabled", isChecked).apply();
            }
        });
    }
}