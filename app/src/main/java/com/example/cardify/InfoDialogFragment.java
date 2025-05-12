package com.example.cardify;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.*;

public class InfoDialogFragment extends DialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView userNameTextView, emailTextView, createdCountTextView, savedCountTextView;
    private ImageView avatarImageView, editIcon;
    private Button deleteAccountButton;
    private DatabaseReference vizitRef;
    private Uri imageUri;
    LayoutInflater inflater;

    private static final String IMGBB_API_KEY = "8c60ccf329b617800e1928f60d7c0382";

    public interface OnDialogCloseListener {
        void onUserInfoDialogClosed();
    }

    private OnDialogCloseListener listener;

    public void setOnDialogCloseListener(OnDialogCloseListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onUserInfoDialogClosed();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.UserInfoDialog);
        inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_info_dialog, null);
        builder.setView(view);

        userNameTextView = view.findViewById(R.id.userNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        createdCountTextView = view.findViewById(R.id.createdCountTextView);
        savedCountTextView = view.findViewById(R.id.savedCountTextView);
        avatarImageView = view.findViewById(R.id.avatarImageView);
        editIcon = view.findViewById(R.id.editIcon);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        vizitRef = database.getReference("vizitcards");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userNameTextView.setText(user.getDisplayName());
            emailTextView.setText(user.getEmail());
            loadUserProfile();
        }

        avatarImageView.setOnClickListener(v -> openFileChooser());

        editIcon.setOnClickListener(v -> {
            EditDialogFragment dialog = new EditDialogFragment();
            dialog.setOnDialogCloseListener(this::loadUserProfile);
            dialog.show(getParentFragmentManager(), "UserInfoDialog");
        });

        deleteAccountButton.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.DeleteCardDialog)
                    .setTitle("Удалить аккаунт")
                    .setMessage("Вы уверены, что хотите удалить аккаунт и все визитки?")
                    .setPositiveButton("Да", (dialogInterface, which) -> deleteAccountAndData())
                    .setNegativeButton("Отмена", null)
                    .create();

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

            dialog.show();
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
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri sourceUri = data.getData();
            if (sourceUri != null) {
                Uri destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), "cropped_avatar.jpg"));
                UCrop.of(sourceUri, destinationUri)
                        .withAspectRatio(1, 1)
                        .withMaxResultSize(500, 500)
                        .start(requireContext(), this);
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri croppedUri = UCrop.getOutput(data);
            if (croppedUri != null) {
                uploadImageToImgBB(croppedUri);
            }
        } else if (requestCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Toast.makeText(getContext(), "Ошибка кадрирования: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToImgBB(Uri imageUri) {
        Log.d(TAG, "uploadImageToImgBB started with imageUri: " + imageUri);
        try {
            Log.d(TAG, "Attempting to get Bitmap from URI.");
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            Log.d(TAG, "Bitmap loaded successfully.");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Log.d(TAG, "Compressing bitmap to PNG.");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imageBytes = stream.toByteArray();
            Log.d(TAG, "Bitmap compressed, size: " + imageBytes.length + " bytes.");

            Log.d(TAG, "Encoding image to Base64.");
            String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            Log.d(TAG, "Image encoded to Base64, length: " + base64Image.length());

            OkHttpClient client = new OkHttpClient();
            Log.d(TAG, "Creating request body.");
            RequestBody requestBody = new FormBody.Builder()
                    .add("key", IMGBB_API_KEY)
                    .add("image", base64Image)
                    .build();
            Log.d(TAG, "Request body created.");

            Log.d(TAG, "Creating request.");
            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build();
            Log.d(TAG, "Request created.");

            Log.d(TAG, "Sending request to ImgBB.");
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Request to ImgBB failed.", e);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.d(TAG, "Received response from ImgBB.");
                    String resp = response.body().string();
                    Log.d(TAG, "Response body: " + resp);

                    try {
                        Log.d(TAG, "Parsing JSON response.");
                        JSONObject json = new JSONObject(resp);
                        String url = json.getJSONObject("data").getString("url");
                        Log.d(TAG, "Image URL extracted: " + url);

                        Log.d(TAG, "Getting current Firebase user.");
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            Log.d(TAG, "Saving avatar URL to Firebase for user: " + currentUser.getUid());
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(currentUser.getUid())
                                    .child("avatar")
                                    .setValue(url);
                            Log.d(TAG, "Avatar URL saved to Firebase.");
                        } else {
                            Log.w(TAG, "CurrentUser is null. Cannot save avatar URL.");
                        }

                        requireActivity().runOnUiThread(() -> {
                            Log.d(TAG, "Loading image into avatarImageView.");
                            Picasso.get().load(url).into(avatarImageView);
                            Log.d(TAG, "Image loaded into avatarImageView.");
                            Toast.makeText(getContext(), "Аватар обновлён", Toast.LENGTH_SHORT).show();
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "Error processing JSON response.", e);
                    }
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Error processing image.", e);
        }
        Log.d(TAG, "uploadImageToImgBB finished.");
    }

    private void deleteAccountAndData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        DatabaseReference savedRef = FirebaseDatabase.getInstance().getReference("users")
                .child(uid).child("createdVizitcards");

        savedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot cardSnap : snapshot.getChildren()) {
                    Log.d("Delete", "Deleting card: " + cardSnap.getKey());
                    String cardId = cardSnap.getKey();
                    if (cardId == null) continue;

                    vizitRef.child(cardId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot cardSnapshot) {
                            if (cardSnapshot.exists()) {
                                Vizitka card = cardSnapshot.getValue(Vizitka.class);
                                vizitRef.child(cardId).removeValue();
                                Log.d("Delete", "Deleted card: " + card.getCompanyName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.removeValue();



        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Аккаунт удалён", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), LoginActivity.class));
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), "Ошибка удаления аккаунта", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        ref.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (name != null) userNameTextView.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        ref.child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String avatarUrl = snapshot.getValue(String.class);
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Picasso.get().load(avatarUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(avatarImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        ref.get().addOnSuccessListener(snapshot -> {
            long createdCount = snapshot.child("createdVizitcards").getChildrenCount();
            long savedCount = snapshot.child("savedVizitcards").getChildrenCount();
            createdCountTextView.setText("Создано визиток: " + createdCount);
            savedCountTextView.setText("Сохранено визиток: " + savedCount);
        });
    }
}
