package com.example.cardify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {
    private FrameLayout fragmentContainer;
    private BottomNavigationView bottomNavigationView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Применяем тему до инициализации UI
        if (ThemeManager.isDarkTheme(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Закрываем MainActivity, чтобы пользователь не мог вернуться назад
            return;
        }
        setContentView(R.layout.activity_main);

        fragmentContainer = findViewById(R.id.fragment_container);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        // Проверка, нужно ли открыть настройки
        boolean openSettings = getIntent().getBooleanExtra("openSettings", false);
        Intent intent = getIntent();

        if (savedInstanceState == null) {
            if (openSettings) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .commit();
                bottomNavigationView.setSelectedItemId(R.id.nav_settings);
            } else if (!(Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null)){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SavedCardsFragment())
                        .commit();
                bottomNavigationView.setSelectedItemId(R.id.nav_saved);
            }
        }

        // Обработка deep link

        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            Uri data = intent.getData();
            String cardId = data.getQueryParameter("cardId");
            if (cardId != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SaveCardFragment())
                        .commit();
                bottomNavigationView.setSelectedItemId(R.id.nav_add);

                // Отключаем обработку выбора нижнего меню, чтобы не перезаписался фрагмент
                bottomNavigationView.setOnItemSelectedListener(null);

                // Открываем фрагмент подтверждения добавления визитки
                ConfirmAddCardFragment.newInstance(cardId)
                        .show(getSupportFragmentManager(), "confirm_dialog");

                // Можно визуально установить выбранный пункт, но обработка при этом выключена
                bottomNavigationView.setSelectedItemId(R.id.nav_add);

                // Возвращаем слушатель обратно
                bottomNavigationView.setOnItemSelectedListener(navListener);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SavedCardsFragment())
                        .commit();

                setIntent(new Intent(getIntent()).setAction(null).setData(null));
                bottomNavigationView.setSelectedItemId(R.id.nav_add);
            }
        }

        // Устанавливаем нижний отступ под BottomNavigationView
        bottomNavigationView.post(() -> {
            int bottomHeight = bottomNavigationView.getHeight();
            fragmentContainer.setPadding(0, 0, 0, bottomHeight);
        });
    }
}
