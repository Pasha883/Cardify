package com.example.cardify;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NfcReceiveDialogFragment extends DialogFragment {



    public static NfcReceiveDialogFragment newInstance() {
        return new NfcReceiveDialogFragment();
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireActivity(), R.style.UserInfoDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_nfc_receive, null);
        builder.setView(view);

        Button stopButton = view.findViewById(R.id.stop_button);

        stopButton.setOnClickListener(v -> dismiss());

        resultTextView = view.findViewById(R.id.nfc_text);
        handleIntent(requireActivity().getIntent());

        View[] waves = {
                view.findViewById(R.id.wave1),
                view.findViewById(R.id.wave2),
                view.findViewById(R.id.wave3)
        };

        for (int i = 0; i < waves.length; i++) {
            View wave = waves[i];
            wave.setScaleX(3f);
            wave.setScaleY(3f);
            wave.setAlpha(0f);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(wave, "scaleX", 3f, 0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(wave, "scaleY", 3f, 0f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(wave, "alpha", 0f, 1f, 0f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY, alpha);
            animatorSet.setDuration(2000);
            animatorSet.setStartDelay(i * 600); // волны одна за другой
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            alpha.setRepeatCount(ValueAnimator.INFINITE);
            animatorSet.start();
        }

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
    }

    private TextView resultTextView;

    //public NfcReceiverFragment() {}

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_nfc_receive, container, false);
        resultTextView = view.findViewById(R.id.text_recived_id);
        handleIntent(requireActivity().getIntent());
        return view;
    }*/

    private void handleIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            Log.d("NfcReceiverFragment", "NFC Tag received: " + tag.toString());
            // В норме здесь вы должны инициировать SELECT AID, но Android делает это сам
            resultTextView.setText("Визитка получена. Обработка...");
        } else {
            Log.e("NfcReceiverFragment", "No NFC tag in intent");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(requireContext());
        if (adapter != null) {
            Log.d("NfcReceiverFragment", "Enabling reader mode");
            Bundle options = new Bundle();
            adapter.enableReaderMode(requireActivity(),
                    this::onTagDiscovered,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    options
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(requireContext());
        if (adapter != null) {
            Log.d("NfcReceiverFragment", "Disabling reader mode");
            adapter.disableReaderMode(requireActivity());
        }
    }

    private void onTagDiscovered(Tag tag) {
        Log.d("NfcReceiverFragment", "Tag discovered: " + tag.toString());

        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep != null) {
            try {
                isoDep.connect();
                Log.d("NfcReceiverFragment", "IsoDep connected");

                // Отправляем SELECT AID
                byte[] selectApdu = BuildSelectApdu("F0010203040506"); // AID из apduservice.xml
                Log.d("NfcReceiverFragment", "Sending SELECT AID");
                byte[] response = isoDep.transceive(selectApdu);

                if (response != null) {
                    String cardId = new String(Arrays.copyOf(response, response.length - 2), StandardCharsets.UTF_8);
                    Log.d("NfcReceiverFragment", "Received Card ID: " + cardId);
                    requireActivity().runOnUiThread(() ->
                            resultTextView.setText("Получено ID визитки: " + cardId)
                    );
                    //int numericId = Integer.parseInt(cardId.replaceAll("\\D+", ""));
                    // Переход к фрагменту подтверждения

                    ConfirmAddCardFragment.newInstance(cardId)
                            .show(requireActivity().getSupportFragmentManager(), "confirm_dialog");
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new SaveCardFragment()) // или другой нужный фрагмент
                            .commit();
                    dismiss();

                }

                isoDep.close();
            } catch (Exception e) {
                Log.e("NfcReceiverFragment", "Error reading tag", e);
            }
        } else {
            Log.e("NfcReceiverFragment", "IsoDep not supported on this tag");
        }
    }

    private byte[] BuildSelectApdu(String aid) {
        byte[] header = {
                (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00,
                (byte) (aid.length() / 2)
        };
        byte[] aidBytes = hexStringToByteArray(aid);
        byte[] fullApdu = new byte[header.length + aidBytes.length];
        System.arraycopy(header, 0, fullApdu, 0, header.length);
        System.arraycopy(aidBytes, 0, fullApdu, header.length, aidBytes.length);
        return fullApdu;
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


}
