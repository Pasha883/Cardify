package com.example.cardify;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.atomic.AtomicInteger;

public class EditDialogFragment extends DialogFragment {

    private EditText nameEditText, emailEditText, passwordEditText, currentPasswordEditText;
    private Button saveButton;
    private String userId;
    private String userName;
    LayoutInflater inflater;

    public interface OnDialogCloseListener {
        void onUserInfoDialogClosed();
    }

    private InfoDialogFragment.OnDialogCloseListener listener;

    public void setOnDialogCloseListener(InfoDialogFragment.OnDialogCloseListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onUserInfoDialogClosed();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.UserInfoDialog);
        inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_edit_dialog, null);
        builder.setView(view);

        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        saveButton = view.findViewById(R.id.saveButton);
        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        userId = user.getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        if (user != null) {
            userRef.child(userId).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            if (name != null) {
                                nameEditText.setText(name);
                                userName = name;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "loadUserProfile: onCancelled - " + error.getMessage());
                        }
                    });
            emailEditText.setText(user.getEmail());
        }

        saveButton.setOnClickListener(v -> {
            AtomicInteger progress = new AtomicInteger(0);

            String newName = nameEditText.getText().toString().trim();
            String newEmail = emailEditText.getText().toString().trim();
            String newPassword = passwordEditText.getText().toString().trim();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) return;


            // Получение текущего email и запроса пароля (например, через отдельное поле)
            String currentEmail = currentUser.getEmail();
            String currentPassword = currentPasswordEditText.getText().toString().trim(); // добавь поле для этого в layout

            if (TextUtils.isEmpty(currentPassword)) {
                Toast.makeText(getContext(), "Введите текущий пароль для подтверждения", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword);

            currentUser.reauthenticate(credential).addOnSuccessListener(authResult -> {
                // Обновление email
                if (!TextUtils.isEmpty(newEmail) && !newEmail.equals(currentEmail)) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Изменить почту")
                            .setMessage("Вы уверены, что хотите изменить  почту? Это сбросит вашу текщую сессию!")
                            .setPositiveButton("Да", (dialog, which) ->
                                    currentUser.verifyBeforeUpdateEmail(newEmail)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "Письмо для подтверждения email отправлено");
                                                Toast.makeText(getContext(), "Подтвердите новую почту через письмо", Toast.LENGTH_LONG).show();
                                                userRef.child(userId).child("e-mail").setValue(newEmail)
                                                        .addOnSuccessListener(aaVoid -> {
                                                            Log.d(TAG, "Email обновлён");
                                                            Toast.makeText(getContext(), "Email обновлён", Toast.LENGTH_SHORT).show();
                                                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                                            mAuth.signOut();
                                                            Intent intent = new Intent(requireActivity(), MainActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                            if (getActivity() != null) {
                                                                getActivity().finish();
                                                            }
                                                            progress.getAndIncrement();
                                                            if (progress.get() == 3) {
                                                                dismiss();
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e(TAG, "Ошибка при обновлении email: " + e.getMessage());
                                                            Toast.makeText(getContext(), "Ошибка email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Ошибка при обновлении email: " + e.getMessage());
                                                Toast.makeText(getContext(), "Ошибка email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            })
                            )
                            .setNegativeButton("Отмена", null)
                            .show();

                } else{
                    progress.getAndIncrement();
                    if (progress.get() == 3) {
                        dismiss();
                    }
                }

                // Обновление пароля
                if (!TextUtils.isEmpty(newPassword)) {
                    if (newPassword.length() < 6) {
                        Toast.makeText(getContext(), "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show();
                    } else {
                        currentUser.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Пароль обновлён");
                                    Toast.makeText(getContext(), "Пароль обновлён", Toast.LENGTH_SHORT).show();
                                    userRef.child(userId).child("paswd").setValue(newPassword)
                                            .addOnSuccessListener(aaVoid -> {
                                                Log.d(TAG, "Пароль обновлён");
                                                Toast.makeText(getContext(), "Пароль обновлён", Toast.LENGTH_SHORT).show();
                                                progress.getAndIncrement();
                                                if (progress.get() == 3) {
                                                    dismiss();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Ошибка при обновлении пароля: " + e.getMessage());
                                                Toast.makeText(getContext(), "Ошибка пароля: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Ошибка при обновлении пароля: " + e.getMessage());
                                    Toast.makeText(getContext(), "Ошибка пароля: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                } else{
                    progress.getAndIncrement();
                    if (progress.get() == 3) {
                        dismiss();
                    }
                }

                // Обновление имени в базе данных
                if (!TextUtils.isEmpty(newName) && !newName.equals(userName)) {
                    userRef.child(userId).child("name").setValue(newName)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Имя обновлено");
                                Toast.makeText(getContext(), "Имя обновлено", Toast.LENGTH_SHORT).show();
                                progress.getAndIncrement();
                                if (progress.get() == 3) {
                                    dismiss();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Ошибка при обновлении имени: " + e.getMessage());
                                Toast.makeText(getContext(), "Ошибка имени: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                } else {
                    progress.getAndIncrement();
                    if (progress.get() == 3) {
                        dismiss();
                    }
                }

//                if (!anyChanges) {
//                    Toast.makeText(getContext(), "Изменений не найдено", Toast.LENGTH_SHORT).show();
//                }

                //dismiss(); // закрыть диалог

            }).addOnFailureListener(e -> {
                Log.e(TAG, "Ошибка повторной аутентификации: " + e.getMessage());
                Toast.makeText(getContext(), "Неверный текущий пароль: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });

        });



        //AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //builder.setView(view);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // Получаем размеры экрана
                DisplayMetrics metrics = new DisplayMetrics();
                requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int width = (int) (metrics.widthPixels * 0.80); // 80% ширины
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent); // сохраняем прозрачность
            }
        }
    }
}
