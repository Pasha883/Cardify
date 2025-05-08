package com.example.cardify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
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
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), R.style.FilterDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_privacy_dialog, null);
        builder.setView(view);

        SwitchMaterial switchVisibility = view.findViewById(R.id.switchVisibility);
        switchVisibility.setChecked(isVisible);

        switchVisibility.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isVisible = isChecked;
            saveVisibilityToFirebase(isVisible);
        });

        return builder
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