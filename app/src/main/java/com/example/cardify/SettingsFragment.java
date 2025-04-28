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

    private DatabaseReference databaseReference;

    private boolean isDarkTheme;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        textProfileName = view.findViewById(R.id.textProfileName);
        layoutAbout = view.findViewById(R.id.layoutAbout);
        layoutTheme = view.findViewById(R.id.layoutTheme);
        imageThemeIcon = view.findViewById(R.id.imageThemeIcon);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        loadUserProfile();
        setupAboutClickListener();
        setupThemeClickListener();

        updateThemeIcon();

        return view;
    }

    private void loadUserProfile() {
        String userId = "userID001";
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

    private void applyTheme() {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("openSettings", true); // <-- Ключевой момент
        startActivity(intent);
        requireActivity().finish();
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
