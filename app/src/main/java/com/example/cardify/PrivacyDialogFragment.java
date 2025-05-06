package com.example.cardify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class PrivacyDialogFragment extends DialogFragment {

    private boolean isVisible;

    public PrivacyDialogFragment(boolean currentValue) {
        this.isVisible = currentValue;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_privacy_dialog, null);
        SwitchCompat switchVisibility = view.findViewById(R.id.switchVisibility);
        switchVisibility.setChecked(isVisible);

        switchVisibility.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isVisible = isChecked;
            // Сохрани значение, например в Firebase или SharedPreferences
            saveVisibilityToFirebase(isVisible);
        });

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle("Приватность")
                .setPositiveButton("ОК", null)
                .create();
    }

    private void saveVisibilityToFirebase(boolean isVisible) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("isVisible")
                .setValue(isVisible);
    }
}
