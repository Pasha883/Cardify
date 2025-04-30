package com.example.cardify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsFragment extends Fragment {

    private TextView textProfileName;
    private LinearLayout layoutAbout;
    private LinearLayout layoutTheme;
    private ImageView imageThemeIcon;
    LinearLayout logoutLayout;

    private DatabaseReference databaseReference;

    private boolean isDarkTheme;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        textProfileName = view.findViewById(R.id.textProfileName);
        layoutAbout = view.findViewById(R.id.layoutAbout);
        layoutTheme = view.findViewById(R.id.layoutTheme);
        imageThemeIcon = view.findViewById(R.id.imageThemeIcon);
        logoutLayout = view.findViewById(R.id.layoutLogout);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        loadUserProfile();
        setupAboutClickListener();
        setupThemeClickListener();
        setupLogoutClickListener();

        updateThemeIcon();

        return view;
    }

    private void loadUserProfile() {
        String userId = "";
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
        databaseReference.child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (name != null) {
                    textProfileName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Лог ошибки если нужно
            }
        });
    }

    private void setupAboutClickListener() {
        layoutAbout.setOnClickListener(v -> showAboutDialog());
    }

    private void setupThemeClickListener() {
        layoutTheme.setOnClickListener(v -> {
            // Меняем тему
            isDarkTheme = !isDarkTheme;
            saveThemePreference(isDarkTheme);
            applyTheme();
        });
    }

    private void setupLogoutClickListener() {
        // Инициализируем Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Находим LinearLayout для выхода


        // Устанавливаем обработчик нажатия
        logoutLayout.setOnClickListener(v -> {
            // Выходим из Firebase Authentication
            mAuth.signOut();

            // Создаем Intent для перезапуска MainActivity
            // Замените MainActivity.class на вашу главную активность, если она называется иначе
            Intent intent = new Intent(requireActivity(), MainActivity.class);

            // Устанавливаем флаги для очистки стека активностей и запуска новой задачи
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Запускаем MainActivity
            startActivity(intent);

            // Закрываем текущую активность (содержащую этот фрагмент)
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void applyTheme() {
        if (isDarkTheme) {
            ThemeManager.saveTheme(requireContext(), true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            ThemeManager.saveTheme(requireContext(), false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("openSettings", true);
        startActivity(intent);
        requireActivity().finish();
        requireActivity().overridePendingTransition(0, 0);
    }



    private void updateThemeIcon() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("settings", getContext().MODE_PRIVATE);
        isDarkTheme = prefs.getBoolean("dark_theme", false);

        if (isDarkTheme) {
            imageThemeIcon.setImageResource(R.drawable.ic_sun); // Солнце для тёмной темы
        } else {
            imageThemeIcon.setImageResource(R.drawable.ic_moon); // Луна для светлой темы
        }
    }

    private void saveThemePreference(boolean darkTheme) {
        SharedPreferences.Editor editor = requireActivity()
                .getSharedPreferences("settings", getContext().MODE_PRIVATE)
                .edit();
        editor.putBoolean("dark_theme", darkTheme);
        editor.apply();
    }

    private void showAboutDialog() {
        String versionName = "1.0"; // На случай ошибки получаем версию 1.0
        try {
            versionName = requireContext()
                    .getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0)
                    .versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        String message = "Название: Cardify\n"
                + "Версия: " + versionName + "\n"
                + "PashaCO 2016–2025\n"
                + "Все права защищены.";

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("О приложении")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
