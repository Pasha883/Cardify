package com.example.cardify;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class CardDetailsFragment extends Fragment {

    // Константа для ключа аргумента фрагмента, содержащего объект Vizitka
    public static final String ARG_VIZITKA = "arg_vizitka";

    // Объект Vizitka, который отображается в деталях
    private Vizitka vizitka;

    // Статический метод для создания нового экземпляра фрагмента с передачей объекта Vizitka
    public static CardDetailsFragment newInstance(Vizitka card) {
        // Создаем новый экземпляр фрагмента
        CardDetailsFragment fragment = new CardDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_VIZITKA, card);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_details, container, false);

        // Получаем объект Vizitka из аргументов фрагмента
        vizitka = (Vizitka) getArguments().getSerializable(ARG_VIZITKA);

        // Находим TextView для отображения информации о создателе
        TextView creatorInfo = view.findViewById(R.id.creator_info);

        // Устанавливаем текстовые поля с данными визитки
        setField(view, R.id.tv_company_name, vizitka.companyName);
        setField(view, R.id.tv_company_spec, vizitka.companySpec);
        setField(view, R.id.tv_description, vizitka.description);

        setClickableField(view, R.id.container_email, R.id.tv_email, vizitka.email, FieldType.EMAIL);
        setClickableField(view, R.id.container_phone, R.id.tv_phone, vizitka.phone, FieldType.PHONE);
        setClickableField(view, R.id.container_site, R.id.tv_site, vizitka.site, FieldType.SITE);
        setClickableField(view, R.id.container_tg, R.id.tv_tg, vizitka.TG, FieldType.TG);

        // Запрос к БД Firebase для проверки видимости информации о создателе
        FirebaseDatabase.getInstance().getReference("users")
                .child(vizitka.creatorId)
                .child("isVisible")
                .get()
                .addOnSuccessListener(snapshot -> {
                    Boolean isVisible = snapshot.getValue(Boolean.class);
                    if (isVisible != null && isVisible) {
                        creatorInfo.setVisibility(View.VISIBLE);
                        creatorInfo.setText("Визитка создана пользователем: " + vizitka.creatorId);
                    } else {
                        creatorInfo.setVisibility(View.GONE);
                    }
                }) // Обработка успешного получения данных
                .addOnFailureListener(e -> {
                    // Обработка ошибки получения данных
                    Toast.makeText(getContext(), "Ошибка загрузки данных о создателе", Toast.LENGTH_SHORT).show();
                });

        creatorInfo.setOnClickListener(v -> {
            // При нажатии на информацию о создателе открываем диалог с подробностями
            UserInfoDialogFragment dialog = UserInfoDialogFragment.newInstance(vizitka.creatorId);
            dialog.show(getParentFragmentManager(), "user_info");
        });

        // Кнопка удаления
        Button deleteBtn = view.findViewById(R.id.btn_delete_card);
        deleteBtn.setOnClickListener(v -> deleteVizitcard());

        return view;
    }

    // Метод для установки текста в TextView и управления видимостью
    private void setField(View view, int textViewId, String value) {
        TextView tv = view.findViewById(textViewId);

        if (value == null || value.isEmpty()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(value);
        }
    }

    // Метод для установки текста в TextView внутри LinearLayout и добавления обработчика кликов для открытия соответствующих приложений
    private void setClickableField(View view, int containerId, int textViewId, String value, FieldType type) {
        LinearLayout container = view.findViewById(containerId);
        TextView tv = view.findViewById(textViewId);

        if (value == null || value.isEmpty()) {
            container.setVisibility(View.GONE);
        } else {
            container.setVisibility(View.VISIBLE);
            tv.setText(value);

            container.setOnClickListener(v -> {
                switch (type) {
                    case EMAIL:
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:" + value));
                        startActivity(Intent.createChooser(emailIntent, "Отправить email"));
                        break;
                    // Обработка клика для номера телефона: открываем приложение для звонков
                    case PHONE:
                        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                        phoneIntent.setData(Uri.parse("tel:" + value));
                        startActivity(phoneIntent);
                        break;
                    // Обработка клика для сайта: открываем веб-браузер
                    case SITE:
                        String url = value.startsWith("http") ? value : "http://" + value;
                        Intent siteIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(siteIntent);
                        break;
                    // Обработка клика для Telegram: открываем приложение Telegram
                    case TG:
                        String tgLink = value.startsWith("@") ? "https://t.me/" + value.substring(1) : value;
                        //String tgLink = "tg://resolve?domain=" + value.substring(1); // Альтернативный способ открытия Telegram
                        Intent tgIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(tgLink));
                        startActivity(tgIntent);
                        break;
                }
            });
        }
    }

    // Перечисление для типов полей с контактной информацией
    private enum FieldType {
        EMAIL,
        PHONE,
        SITE,
        TG
    }

    // Метод для уменьшения счетчика пользователей, у которых сохранена данная визитка
    private void decrementUserCount(String cardId) {
        FirebaseDatabase.getInstance()
                .getReference("vizitcards")
                .child(cardId)
                .child("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Long currentCount = task.getResult().getValue(Long.class);
                        if (currentCount != null && currentCount > 0) {
                            long newCount = currentCount - 1;
                            FirebaseDatabase.getInstance()
                                    .getReference("vizitcards")
                                    .child(cardId)
                                    .child("users")
                                    .setValue(newCount);
                        }
                    }
                });
    }

    // Метод для удаления визитки
    private void deleteVizitcard(){
        // Создаем диалог подтверждения удаления
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.DeleteCardDialog)
                .setTitle("Удалить визитку?") // Заголовок диалога
                .setMessage("Вы уверены? Это действие необратимо.")
                .setPositiveButton("Да", (dialogInterface, which) -> {
                    String userId = "";
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        userId = currentUser.getUid();
                    }
                    String cardId = vizitka.id;

                    // Уменьшаем счетчик пользователей у визитки
                    decrementUserCount(cardId);

                    // Удаляем визитку из списка сохраненных у текущего пользователя
                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(userId)
                            .child("savedVizitcards")
                            .child(cardId)
                            // Обработка успешного удаления
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Визитка удалена", Toast.LENGTH_SHORT).show();
                                //requireActivity().onBackPressed();
                                MainActivity activity = (MainActivity) getActivity();
                                if (activity != null) {
                                    activity.goToSavedCardsFragment();
                                }

                            })
                            // Обработка ошибки удаления
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show());
                }) // Кнопка "Да"
                .setNegativeButton("Нет", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();
        // Кнопка "Нет"
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
        // Устанавливаем слушатель на показ диалога для настройки его ширины
        dialog.show();
        // Показываем диалог
    }
}
