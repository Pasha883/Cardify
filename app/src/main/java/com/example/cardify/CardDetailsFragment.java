package com.example.cardify;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

    public static final String ARG_VIZITKA = "arg_vizitka";

    private Vizitka vizitka;

    public static CardDetailsFragment newInstance(Vizitka card) {
        CardDetailsFragment fragment = new CardDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_VIZITKA, card);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_details, container, false);

        vizitka = (Vizitka) getArguments().getSerializable(ARG_VIZITKA);

        setField(view, R.id.tv_company_name, vizitka.companyName);
        setField(view, R.id.tv_company_spec, vizitka.companySpec);
        setField(view, R.id.tv_description, vizitka.description);

        setClickableField(view, R.id.container_email, R.id.tv_email, vizitka.email, FieldType.EMAIL);
        setClickableField(view, R.id.container_phone, R.id.tv_phone, vizitka.phone, FieldType.PHONE);
        setClickableField(view, R.id.container_site, R.id.tv_site, vizitka.site, FieldType.SITE);
        setClickableField(view, R.id.container_tg, R.id.tv_tg, vizitka.TG, FieldType.TG);

        // Кнопка удаления
        Button deleteBtn = view.findViewById(R.id.btn_delete_card);
        deleteBtn.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Удалить визитку?")
                    .setMessage("Вы уверены? Это действие необратимо.")
                    .setPositiveButton("Да", (dialogInterface, which) -> {
                        String userId = "";
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            userId = currentUser.getUid();
                        }
                        String cardId = vizitka.id;

                        decrementUserCount(cardId);

                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(userId)
                                .child("savedVizitcards")
                                .child(cardId)
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Визитка удалена", Toast.LENGTH_SHORT).show();
                                    //requireActivity().onBackPressed();
                                    MainActivity activity = (MainActivity) getActivity();
                                    if (activity != null) {
                                        activity.goToSavedCardsFragment();
                                    }

                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Нет", (dialogInterface, which) -> dialogInterface.dismiss())
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
            });

            dialog.show();
        });

        return view;
    }

    private void setField(View view, int textViewId, String value) {
        TextView tv = view.findViewById(textViewId);

        if (value == null || value.isEmpty()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(value);
        }
    }

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
                    case PHONE:
                        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                        phoneIntent.setData(Uri.parse("tel:" + value));
                        startActivity(phoneIntent);
                        break;
                    case SITE:
                        String url = value.startsWith("http") ? value : "http://" + value;
                        Intent siteIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(siteIntent);
                        break;
                    case TG:
                        String tgLink = value.startsWith("@") ? "https://t.me/" + value.substring(1) : value;
                        Intent tgIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(tgLink));
                        startActivity(tgIntent);
                        break;
                }
            });
        }
    }

    private enum FieldType {
        EMAIL,
        PHONE,
        SITE,
        TG
    }

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
}
