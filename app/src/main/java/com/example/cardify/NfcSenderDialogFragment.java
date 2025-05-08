package com.example.cardify;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;


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
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireActivity(), R.style.UserInfoDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_nfc_sender, null);
        builder.setView(view);

        TextView textView = view.findViewById(R.id.nfc_text);
        ImageView imageView = view.findViewById(R.id.nfc_image);
        Button stopButton = view.findViewById(R.id.stop_button);

        textView.setText("Поднесите телефон для передачи визитки");

        View[] waves = {
                view.findViewById(R.id.wave1),
                view.findViewById(R.id.wave2),
                view.findViewById(R.id.wave3)
        };

        for (int i = 0; i < waves.length; i++) {
            View wave = waves[i];
            wave.setScaleX(0f);
            wave.setScaleY(0f);
            wave.setAlpha(1f);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(wave, "scaleX", 0f, 3f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(wave, "scaleY", 0f, 3f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(wave, "alpha", 1f, 0f);

            // Set repeat count on each animator
            scaleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            alpha.setRepeatCount(ValueAnimator.INFINITE);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY, alpha);
            animatorSet.setDuration(2000);
            animatorSet.setStartDelay(i * 600); // задержка между волнами
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

            animatorSet.start();
        }


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
        requireActivity().registerReceiver(cardSentReceiver,
                new IntentFilter("com.example.cardify.ACTION_CARD_SENT"));
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().unregisterReceiver(cardSentReceiver);
    }


}
