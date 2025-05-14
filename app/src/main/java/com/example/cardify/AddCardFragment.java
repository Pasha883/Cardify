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

    // Поля ввода для информации о компании
    private EditText etCompanyName, etCompanySpec, etDescription, etEmail, etPhone, etSite, etTG;
    // Кнопка для сохранения изменений
    private MaterialButton btnSave;
    // Ссылки на узлы базы данных Firebase
    private DatabaseReference cardsRef, userRef;
    // ID текущего пользователя Firebase
    // Инициализируется пустой строкой
    // Будет заполнен при получении текущего пользователя
    private  String userId = "";


    // Метод вызывается для создания и возврата представления,
    // которое будет отображаться фрагментом.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Надуваем макет для этого фрагмента.
        View view = inflater.inflate(R.layout.fragment_add_card, container, false);

        // Инициализируем поля ввода и кнопку, находя их в макете.
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
            // Получаем ID текущего пользователя Firebase.
            userId = currentUser.getUid();
        }

        // Получаем ссылки на соответствующие узлы базы данных Firebase.
        cardsRef = FirebaseDatabase.getInstance().getReference("vizitcards");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Устанавливаем слушатель кликов для кнопки "Сохранить".
        btnSave.setOnClickListener(v -> createNewCard());
        return view;
    }

    // Метод для создания новой визитки.
    // Определяет следующий доступный ID визитки и сохраняет новую визитку в базу данных.
    private void createNewCard() {
        // Получаем ссылку на последнюю визитку, отсортированную по ключу (ID).
        cardsRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            // Обработчик события, когда данные успешно получены.
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Инициализируем следующий доступный ID как 1.
                int nextId = 1;
                // Проходим по последним элементам в snapshot (ожидается только один).
                for (DataSnapshot lastCard : snapshot.getChildren()) {
                    // Получаем ключ последней визитки (например, "cardID003").
                    String lastKey = lastCard.getKey();
                    // Проверяем, что ключ не null и начинается с "cardID".
                    if (lastKey != null && lastKey.startsWith("cardID")) {
                        // Извлекаем числовую часть ID (например, "003").
                        String numberPart = lastKey.substring(6);
                        try {
                            // Преобразуем числовую часть в Integer и увеличиваем на 1.
                            nextId = Integer.parseInt(numberPart) + 1;
                            // Если происходит ошибка преобразования, игнорируем ее и оставляем nextId = 1.
                        } catch (NumberFormatException ignored) {}
                    }
                }

                // Форматируем новый ID визитки (например, "cardID004").
                String newCardId = String.format("cardID%03d", nextId);
                // Сохраняем новую визитку со сгенерированным ID.
                saveNewCardToDatabase(newCardId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Ошибка при создании ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Метод для сохранения новой визитки в базу данных Firebase.
    // Принимает сгенерированный ID визитки в качестве параметра.
    private void saveNewCardToDatabase(String cardId) {
        String name = etCompanyName.getText().toString().trim();
        String spec = etCompanySpec.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String site = etSite.getText().toString().trim();
        String tg = etTG.getText().toString().trim();

        // Получаем ID текущего пользователя, который создает визитку.
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Проверяем, что обязательные поля (Название компании и Специализация) не пустые.
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(spec)) {
            Toast.makeText(getContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
            return;
            // Если поля пустые, показываем сообщение и выходим из метода.
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

        // Сохраняем данные визитки в узле "vizitcards" со сгенерированным cardId в качестве ключа.
        cardsRef.child(cardId).setValue(cardData).addOnSuccessListener(unused -> {
            // При успешном сохранении визитки, добавляем ссылку на эту визитку
            // в узел "createdVizitcards" под записью текущего пользователя.
            userRef.child("createdVizitcards").child(cardId).setValue("")
                    .addOnSuccessListener(unused1 -> {
                        // При успешном добавлении ссылки, показываем сообщение о создании визитки.
                        Toast.makeText(getContext(), "Визитка создана", Toast.LENGTH_SHORT).show();
                        // Возвращаемся к предыдущему фрагменту в стеке.
                        requireActivity().getSupportFragmentManager().popBackStack();
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Ошибка при сохранении визитки", Toast.LENGTH_SHORT).show();
        });
    }
}
