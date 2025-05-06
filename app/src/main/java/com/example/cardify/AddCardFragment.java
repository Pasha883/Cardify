package com.example.cardify;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AddCardFragment extends Fragment {

    private EditText etCompanyName, etCompanySpec, etDescription, etEmail, etPhone, etSite, etTG;
    private MaterialButton btnSave;
    private DatabaseReference cardsRef, userRef;
    private  String userId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_card, container, false);

        etCompanyName = view.findViewById(R.id.et_company_name);
        etCompanySpec = view.findViewById(R.id.et_company_spec);
        etDescription = view.findViewById(R.id.et_description);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        etSite = view.findViewById(R.id.et_site);
        etTG = view.findViewById(R.id.et_telegram);
        btnSave = view.findViewById(R.id.btn_save_changes);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        cardsRef = FirebaseDatabase.getInstance().getReference("vizitcards");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);



        btnSave.setOnClickListener(v -> createNewCard());

        return view;
    }

    private void createNewCard() {
        cardsRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int nextId = 1;
                for (DataSnapshot lastCard : snapshot.getChildren()) {
                    String lastKey = lastCard.getKey(); // Например, "cardID003"
                    if (lastKey != null && lastKey.startsWith("cardID")) {
                        String numberPart = lastKey.substring(6);
                        try {
                            nextId = Integer.parseInt(numberPart) + 1;
                        } catch (NumberFormatException ignored) {}
                    }
                }

                String newCardId = String.format("cardID%03d", nextId);
                saveNewCardToDatabase(newCardId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Ошибка при создании ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNewCardToDatabase(String cardId) {
        String name = etCompanyName.getText().toString().trim();
        String spec = etCompanySpec.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String site = etSite.getText().toString().trim();
        String tg = etTG.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(spec)) {
            Toast.makeText(getContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Сохраняем визитку как HashMap, чтобы добавить поле "users"
        Map<String, Object> cardData = new HashMap<>();
        cardData.put("id", cardId);
        cardData.put("companyName", name);
        cardData.put("companySpec", spec);
        cardData.put("description", desc);
        cardData.put("email", email);
        cardData.put("phone", phone);
        cardData.put("site", site);
        cardData.put("TG", tg);
        cardData.put("users", 0); // Важно: users = 0 при создании
        cardData.put("creatorId", userId);

        cardsRef.child(cardId).setValue(cardData).addOnSuccessListener(unused -> {
            userRef.child("createdVizitcards").child(cardId).setValue("")
                    .addOnSuccessListener(unused1 -> {
                        Toast.makeText(getContext(), "Визитка создана", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Ошибка при сохранении визитки", Toast.LENGTH_SHORT).show();
        });
    }
}
