package com.example.cardify;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class NfcSenderDialogFragment extends DialogFragment {

    private static String cardIdToSend = "cardID123";

    private final BroadcastReceiver cardSentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("NfcSenderDialog", "Card sent, dismissing dialog.");
            requireActivity().stopService(new Intent(getActivity(), CardHCEService.class));
            dismiss();
        }
    };

    public static NfcSenderDialogFragment newInstance(String cardId) {
        NfcSenderDialogFragment fragment = new NfcSenderDialogFragment();
        Bundle args = new Bundle();
        args.putString("cardId", cardId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_nfc_sender, null);

        TextView textView = view.findViewById(R.id.nfc_text);
        ImageView imageView = view.findViewById(R.id.nfc_image);
        Button stopButton = view.findViewById(R.id.stop_button);

        textView.setText("Поднесите телефон для передачи визитки");

        if (getArguments() != null) {
            cardIdToSend = getArguments().getString("cardId");
            CardHCEService.cardId = cardIdToSend;
            int numericId = Integer.parseInt(cardIdToSend.replaceAll("\\D+", ""));
            Log.d("NfcSenderDialog", "Numeric ID: " + numericId);
        }

        // Запускаем HCE-сервис
        Intent intent = new Intent(getActivity(), CardHCEService.class);
        intent.putExtra("cardId", cardIdToSend);
        requireActivity().startService(intent);

        stopButton.setOnClickListener(v -> {
            requireActivity().stopService(new Intent(getActivity(), CardHCEService.class));
            dismiss();
        });

        return new AlertDialog.Builder(requireActivity())
                .setView(view)
                .setCancelable(false)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().registerReceiver(cardSentReceiver,
                new IntentFilter("com.example.cardify.ACTION_CARD_SENT"));
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().unregisterReceiver(cardSentReceiver);
    }
}
