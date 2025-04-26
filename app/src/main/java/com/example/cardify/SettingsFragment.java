package com.example.cardify;

import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsFragment extends Fragment {

    private TextView textProfileName;
    private LinearLayout layoutAbout;

    private DatabaseReference databaseReference;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        textProfileName = view.findViewById(R.id.textProfileName);
        layoutAbout = view.findViewById(R.id.layoutAbout);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        loadUserProfile();
        setupAboutClickListener();

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

    private void showAboutDialog() {
        String versionName = "1.0"; // На случай ошибки получаем версию 1.0
        try {
            PackageInfo packageInfo = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String message = "Название: Cardify\n"
                + "Версия: " + versionName + "\n"
                + "PashaCO 2016–2025\n"
                + "Все права защищены.";

        new AlertDialog.Builder(requireContext())
                .setTitle("О приложении")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
