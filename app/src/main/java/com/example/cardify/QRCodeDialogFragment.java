package com.example.cardify;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.io.File;
import java.io.FileOutputStream;

public class QRCodeDialogFragment extends DialogFragment {

    private String cardId;
    private String companyName;

    public static QRCodeDialogFragment newInstance(String cardId, String companyName) {
        QRCodeDialogFragment fragment = new QRCodeDialogFragment();
        Bundle args = new Bundle();
        args.putString("cardId", cardId);
        args.putString("companyName", companyName);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_qr_code, null);
        cardId = getArguments().getString("cardId");
        companyName = getArguments().getString("companyName");

        TextView companyTitle = view.findViewById(R.id.text_company_title);
        ImageView qrImage = view.findViewById(R.id.image_qr);
        Button btnSave = view.findViewById(R.id.btn_save_qr);

        companyTitle.setText(companyName);

        // Генерация QR-кода
        try {
            String url = "https://cardify.page.link/add?cardId=" + cardId;
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap qrBitmap = encoder.encodeBitmap(url, BarcodeFormat.QR_CODE, 512, 512);
            qrImage.setImageBitmap(qrBitmap);

            btnSave.setOnClickListener(v -> {
                saveImage(qrBitmap, companyName.replaceAll("[^a-zA-Z0-9_-]", "_"));
            });

        } catch (WriterException e) {
            e.printStackTrace();
        }

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private void saveImage(Bitmap bitmap, String baseName) {
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) dir.mkdirs();

            // Генерируем уникальное имя файла
            String fileName = baseName + ".png";
            File file = new File(dir, fileName);
            int count = 1;
            while (file.exists()) {
                fileName = baseName + "(" + count + ").png";
                file = new File(dir, fileName);
                count++;
            }

            // Сохраняем файл
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();


            Toast.makeText(getContext(), "Сохранено: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }

}
