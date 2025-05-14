package com.example.cardify;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditCardFragment extends Fragment {

    // Поля ввода и кнопка сохранения
    private EditText editCompanyName, editCompanySpec, editPhone, editEmail, editAddress, editWebsite, editDescription;
    private Button btnSaveChanges;

    // Редактируемая визитка
    private VizitkaCreated card;

    public static EditCardFragment newInstance(VizitkaCreated card) {
        EditCardFragment fragment = new EditCardFragment();
        Bundle args = new Bundle();
        args.putSerializable("card", card);
        fragment.setArguments(args);
        return fragment;
    }

    // Инициализация View и привязка элементов интерфейса
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Надуваем макет фрагмента
        View view = inflater.inflate(R.layout.fragment_edit_card, container, false);

        editCompanyName = view.findViewById(R.id.edit_company_name);
        editCompanySpec = view.findViewById(R.id.edit_company_spec);
        editPhone = view.findViewById(R.id.edit_phone);
        editEmail = view.findViewById(R.id.edit_email);
        editAddress = view.findViewById(R.id.edit_address);
        editWebsite = view.findViewById(R.id.edit_website);
        editDescription = view.findViewById(R.id.edit_description);
        btnSaveChanges = view.findViewById(R.id.btn_save_changes);

        // Получаем объект визитки из аргументов фрагмента и заполняем поля
        if (getArguments() != null) {
            card = (VizitkaCreated) getArguments().getSerializable("card");
            if (card != null) {
                fillFields(card);
            }
        }

        // Устанавливаем слушатели для кнопок
        btnSaveChanges.setOnClickListener(v -> saveChanges());
        Button deleteBtn = view.findViewById(R.id.btn_delete_card);
        deleteBtn.setOnClickListener(v -> deleteVizitcard());

        return view;

    }

    private void fillFields(VizitkaCreated card) {
        editCompanyName.setText(card.companyName);
        editCompanySpec.setText(card.companySpec);
        editPhone.setText(card.phone);
        editEmail.setText(card.email);
        editAddress.setText(card.TG);
        editWebsite.setText(card.site);
        editDescription.setText(card.description);
    }

    // Сохраняет изменения визитки в базе данных
    private void saveChanges() {
        String updatedName = editCompanyName.getText().toString().trim();
        String updatedSpec = editCompanySpec.getText().toString().trim();
        String updatedPhone = editPhone.getText().toString().trim();
        String updatedEmail = editEmail.getText().toString().trim();
        String updatedAddress = editAddress.getText().toString().trim();
        String updatedWebsite = editWebsite.getText().toString().trim();
        String updatedDescription = editDescription.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Проверка на заполненность обязательного поля
        if (TextUtils.isEmpty(updatedName)) {
            editCompanyName.setError("Название обязательно");
            return;
        }

        card.companyName = updatedName;
        card.companySpec = updatedSpec;
        card.phone = updatedPhone;
        card.email = updatedEmail;
        card.TG = updatedAddress;
        card.site = updatedWebsite;
        card.description = updatedDescription;
        card.creatorId = userId;

        // Получаем ссылку на узел визитки в базе данных
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("vizitcards")
                .child(card.id);

        // Обновляем данные визитки
        ref.setValue(card)
                .addOnSuccessListener(unused -> { // При успешном сохранении
                    Toast.makeText(getContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Удаляет визитку после подтверждения пользователя
    private void deleteVizitcard(){
        // Создание диалогового окна для подтверждения удаления
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.DeleteCardDialog)
                .setTitle("Удалить визитку?")
                // Сообщение в диалоговом окне
                .setMessage("Вы уверены? Это действие необратимо.")
                .setPositiveButton("Да", (dialogInterface, which) -> {
                    String userId = "";
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        userId = currentUser.getUid();
                    }
                    String cardId = card.id;

                    DatabaseReference db = FirebaseDatabase.getInstance().getReference();

                    // Удаляем визитку из общего списка визиток
                    db.child("vizitcards").child(cardId).removeValue();

                    // Удаляем ссылку на визитку из списка созданных визиток пользователя
                    db.child("users").child(userId).child("createdVizitcards").child(cardId).removeValue()
                            .addOnSuccessListener(aVoid -> { // При успешном удалении
                                Toast.makeText(getContext(), "Визитка удалена", Toast.LENGTH_SHORT).show();
                                requireActivity().onBackPressed();
                            })
                            .addOnFailureListener(e -> // При ошибке удаления
                                    Toast.makeText(getContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Нет", (dialogInterface, which) -> dialogInterface.dismiss())
                .create(); // Создание AlertDialog

        dialog.setOnShowListener(dialogInterface -> {
            // Получаем ширину экрана
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;

            // Задаем желаемую ширину в пикселях (например, 80% от ширины экрана)
            int dialogWidth = (int) (screenWidth * 0.8); // 80% от ширины экрана

            // Устанавливаем ширину окна диалога
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        // Отображение диалогового окна
        dialog.show();
    }
}
