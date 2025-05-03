package com.example.cardify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditDialogFragment extends DialogFragment {

    private EditText nameEditText, emailEditText, passwordEditText;
    private Button saveButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_edit_dialog, null);

        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        saveButton = view.findViewById(R.id.saveButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            nameEditText.setText(user.getDisplayName());
            emailEditText.setText(user.getEmail());
        }

        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newEmail = emailEditText.getText().toString().trim();
            String newPassword = passwordEditText.getText().toString().trim();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) return;

            if (!newEmail.equals(currentUser.getEmail())) {
                currentUser.updateEmail(newEmail);
            }

            if (!TextUtils.isEmpty(newPassword)) {
                currentUser.updatePassword(newPassword);
            }

            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            currentUser.updateProfile(profileUpdate).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Данные обновлены", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Ошибка при обновлении", Toast.LENGTH_SHORT).show();
                }
            });
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        return builder.create();
    }
}
