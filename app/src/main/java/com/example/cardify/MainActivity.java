package com.example.cardify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private FrameLayout fragmentContainer;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        fragmentContainer = findViewById(R.id.fragment_container);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Смотрим, что передано через Intent
        boolean openSettings = getIntent().getBooleanExtra("openSettings", false);

        if (savedInstanceState == null) {
            if (openSettings) {
                // Если передано открытие настроек
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .commit();
                bottomNav.setSelectedItemId(R.id.nav_settings);
            } else {
                // Иначе по умолчанию SavedCardsFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SavedCardsFragment())
                        .commit();
                bottomNav.setSelectedItemId(R.id.nav_saved);
            }
        }

        // Автоматическая установка нижнего отступа под BottomNavigationView
        bottomNavigationView.post(() -> {
            int bottomHeight = bottomNavigationView.getHeight();
            fragmentContainer.setPadding(0, 0, 0, bottomHeight);
        });
    }

    private final BottomNavigationView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_saved) {
                    selectedFragment = new SavedCardsFragment();
                } else if (item.getItemId() == R.id.nav_my) {
                    selectedFragment = new MyCardsFragment();
                } else if (item.getItemId() == R.id.nav_add) {
                    selectedFragment = new SaveCardFragment();
                } else if (item.getItemId() == R.id.nav_settings) {
                    selectedFragment = new SettingsFragment();
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();

                return true;
            };
}
