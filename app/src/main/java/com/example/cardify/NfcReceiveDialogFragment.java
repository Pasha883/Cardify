package com.example.cardify;

import android.app.Dialog;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

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
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_nfc_receive, null);

        Button stopButton = view.findViewById(R.id.stop_button);

        stopButton.setOnClickListener(v -> dismiss());

        resultTextView = view.findViewById(R.id.text_recived_id);
        handleIntent(requireActivity().getIntent());

        return new AlertDialog.Builder(requireActivity())
                .setView(view)
                .setCancelable(false)
                .create();
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
