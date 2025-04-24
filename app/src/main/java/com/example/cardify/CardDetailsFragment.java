package com.example.cardify;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        setField(view, R.id.container_email, R.id.tv_email, vizitka.email);
        setField(view, R.id.container_phone, R.id.tv_phone, vizitka.phone);
        setField(view, R.id.container_site, R.id.tv_site, vizitka.site);
        setField(view, R.id.container_tg, R.id.tv_tg, vizitka.TG);

        return view;
    }

    private void setField(View view, int containerId, int textViewId, String value) {
        LinearLayout container = view.findViewById(containerId);
        TextView tv = view.findViewById(textViewId);

        if (value == null || value.isEmpty()) {
            container.setVisibility(View.GONE);
        } else {
            container.setVisibility(View.VISIBLE);
            tv.setText(value);
        }
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
}