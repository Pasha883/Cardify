package com.example.cardify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.app.FragmentManager;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QRScannerFragment extends Fragment {

    private DecoratedBarcodeView barcodeView;
    private boolean scanned = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_scanner, container, false);
        barcodeView = view.findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);
        return view;
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (!scanned) {
                scanned = true;

                // Получаем cardId из ссылки, например: https://cardify.page.link/add?cardId=cardID123
                String scannedText = result.getText();
                String cardId = extractCardIdFromUrl(scannedText);

                if (cardId != null) {
                    // Переход к фрагменту подтверждения
                    ConfirmAddCardFragment.newInstance(cardId)
                            .show(requireActivity().getSupportFragmentManager(), "confirm_dialog");
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new SaveCardFragment()) // или другой нужный фрагмент
                            .commit();

                } else {
                    scanned = false;
                }
            }
        }
    };

    private String extractCardIdFromUrl(String url) {
        if (url != null && url.contains("cardId=")) {
            return url.substring(url.indexOf("cardId=") + 7);
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}
