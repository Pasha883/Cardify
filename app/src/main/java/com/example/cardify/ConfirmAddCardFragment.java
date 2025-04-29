package com.example.cardify;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConfirmAddCardFragment extends DialogFragment {

    private static final String ARG_CARD_ID = "card_id";
    private String cardId;
    private String userId;

    private TextView companyNameView;
    private TextView companySpecView;
    private DatabaseReference database;

    public static ConfirmAddCardFragment newInstance(String cardId) {
        ConfirmAddCardFragment fragment = new ConfirmAddCardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CARD_ID, cardId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_add_card, container, false);

        companyNameView = view.findViewById(R.id.text_company_name);
        companySpecView = view.findViewById(R.id.text_company_spec);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        if (getArguments() != null) {
            cardId = getArguments().getString(ARG_CARD_ID);
            loadCardData(cardId);
        }

        btnSave.setOnClickListener(v -> saveCard(cardId));
        btnCancel.setOnClickListener(v -> {
            dismiss();
            /*requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SavedCardsFragment()) // или другой нужный фрагмент
                    .commit();*/
        });

        database = FirebaseDatabase.getInstance().getReference();
        userId = "userID001";

        return view;
    }

    private void loadCardData(String cardId) {
        FirebaseDatabase.getInstance().getReference("vizitcards").child(cardId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        VizitkaCreated card = snapshot.getValue(VizitkaCreated.class);
                        if (card != null) {
                            companyNameView.setText(card.companyName);
                            companySpecView.setText(card.companySpec);
                        } else {
                            Toast.makeText(getContext(), "Визитка не найдена", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
    }

    private void saveCard2(String cardId) {
        String currentUserId = "userID001"; // или получить текущего пользователя
        FirebaseDatabase.getInstance().getReference("users")
                .child(currentUserId)
                .child("savedVizitcards")
                .child(cardId)
                .setValue(true)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Визитка сохранена", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveCard(String cardId) {

        // Сначала проверяем, добавлял ли пользователь уже эту визитку
        database.child("users").child(userId).child("savedVizitcards").child(cardId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot savedSnapshot = task.getResult();
                        if (savedSnapshot.exists()) {
                            // Визитка уже добавлена
                            Toast.makeText(getContext(), "Визитка уже добавлена", Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            // Визитка ещё не добавлена, теперь проверяем её существование в общем списке
                            database.child("vizitcards").child(cardId).get().addOnCompleteListener(cardTask -> {
                                if (cardTask.isSuccessful()) {
                                    DataSnapshot cardSnapshot = cardTask.getResult();
                                    if (cardSnapshot.exists()) {
                                        // Добавляем визитку пользователю
                                        FirebaseDatabase.getInstance().getReference("users")
                                                .child(userId)
                                                .child("savedVizitcards")
                                                .child(cardId)
                                                .setValue(true)
                                                .addOnSuccessListener(unused -> {
                                                    Toast.makeText(getContext(), "Визитка сохранена", Toast.LENGTH_SHORT).show();
                                                    incrementUserCount(cardId);
                                                    dismiss();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                                                    dismiss();
                                                });

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

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(dpToPx(320), WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(Gravity.CENTER);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                requireContext().getResources().getDisplayMetrics()
        );
    }
}
