package com.example.cardify;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SaveCardFragment extends Fragment {

    private EditText editCardNumber;
    private Button btnAddCard;

    private DatabaseReference database;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_save_card, container, false);

        editCardNumber = view.findViewById(R.id.edit_card_number);
        btnAddCard = view.findViewById(R.id.btn_add_card);

        database = FirebaseDatabase.getInstance().getReference();
        userId = "userID001";

        btnAddCard.setOnClickListener(v -> tryAddCard());

        return view;
    }

    private void tryAddCard() {
        String input = editCardNumber.getText().toString().trim();

        if (TextUtils.isEmpty(input) || input.length() != 3) {
            Toast.makeText(getContext(), "Введите 3-значный номер", Toast.LENGTH_SHORT).show();
            return;
        }

        String cardId = "cardID" + input;

        // Сначала проверяем, добавлял ли пользователь уже эту визитку
        database.child("users").child(userId).child("savedVizitcards").child(cardId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot savedSnapshot = task.getResult();
                        if (savedSnapshot.exists()) {
                            // Визитка уже добавлена
                            Toast.makeText(getContext(), "Визитка уже добавлена", Toast.LENGTH_SHORT).show();
                        } else {
                            // Визитка ещё не добавлена, теперь проверяем её существование в общем списке
                            database.child("vizitcards").child(cardId).get().addOnCompleteListener(cardTask -> {
                                if (cardTask.isSuccessful()) {
                                    DataSnapshot cardSnapshot = cardTask.getResult();
                                    if (cardSnapshot.exists()) {
                                        // Добавляем визитку пользователю
                                        database.child("users").child(userId).child("savedVizitcards").child(cardId)
                                                .setValue("")
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(getContext(), "Визитка добавлена", Toast.LENGTH_SHORT).show();
                                                    // Увеличиваем количество пользователей у визитки
                                                    incrementUserCount(cardId);
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show());
                                    } else {
                                        Toast.makeText(getContext(), "Визитка не найдена", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Ошибка поиска визитки: " + cardTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), "Ошибка проверки сохранённых визиток: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void incrementUserCount(String cardId) {
        DatabaseReference userCountRef = database.child("vizitcards").child(cardId).child("users");

        userCountRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Long currentCount = task.getResult().getValue(Long.class);
                if (currentCount == null) {
                    currentCount = 0L;
                }
                userCountRef.setValue(currentCount + 1);
            }
        });
    }
}
