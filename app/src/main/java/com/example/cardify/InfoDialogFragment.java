package com.example.cardify;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;

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
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InfoDialogFragment extends DialogFragment {

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

    private TextView userNameTextView, emailTextView, createdCountTextView, savedCountTextView;
    private ImageView avatarImageView, editIcon;
    private Button deleteAccountButton;
    private DatabaseReference databaseReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    // Замените на ваш собственный API ключ ImgBB
    private static final String IMGBB_API_KEY = "8c60ccf329b617800e1928f60d7c0382";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_info_dialog, null);

        userNameTextView = view.findViewById(R.id.userNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        createdCountTextView = view.findViewById(R.id.createdCountTextView);
        savedCountTextView = view.findViewById(R.id.savedCountTextView);
        avatarImageView = view.findViewById(R.id.avatarImageView);
        editIcon = view.findViewById(R.id.editIcon);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userNameTextView.setText(user.getDisplayName());
            emailTextView.setText(user.getEmail());

            loadUserProfile();

            // Получаем данные о визитках
            FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .get().addOnSuccessListener(snapshot -> {
                        long createdCount = snapshot.child("createdVizitcards").getChildrenCount();
                        long savedCount = snapshot.child("savedVizitcards").getChildrenCount();
                        createdCountTextView.setText("Создано визиток: " + createdCount);
                        savedCountTextView.setText("Сохранено визиток: " + savedCount);
                    });
        }

        editIcon.setOnClickListener(v -> {
            //new EditDialogFragment().show(getParentFragmentManager(), "edit_dialog");
            EditDialogFragment dialog = new EditDialogFragment();
            dialog.setOnDialogCloseListener(() -> {
                // Это будет вызвано, когда диалог закроется
                Log.d("InfoDialogFragment", "EditDialogFragment был закрыт");
                // Здесь можешь обновить UI, например, заново загрузить имя/аватар/визитки
                loadUserProfile();
            });
            dialog.show(getParentFragmentManager(), "UserInfoDialog");
        });

        deleteAccountButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Удалить аккаунт")
                    .setMessage("Вы уверены, что хотите удалить аккаунт и все визитки?")
                    .setPositiveButton("Да", (dialog, which) -> deleteAccountAndData())
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        avatarImageView.setOnClickListener(v -> {
            openFileChooser();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        return builder.create();
    }

    private void deleteAccountAndData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        //TODO: Добавить удаление всех созданных визиток
        String uid = user.getUid();

        // Удаляем данные из базы
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.removeValue();

        // Удаляем аккаунт
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
        if (currentUser == null) {
            Log.e(TAG, "loadUserProfile: currentUser is null");
            return;
        }

        String userId = currentUser.getUid();
        FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null) {
                            userNameTextView.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "loadUserProfile: onCancelled - " + error.getMessage());
                    }
                });

        FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("avatar")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String avatarUrl = snapshot.getValue(String.class);
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Picasso.get()
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .into(avatarImageView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "loadUserProfile: onCancelled - " + error.getMessage());
                    }
                });
    }

    private void openFileChooser() {
        Log.d(TAG, "openFileChooser: opening file chooser");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            Log.d(TAG, "onActivityResult: imageUri = " + imageUri);
            uploadImageToImgBB(imageUri);
        } else {
            Log.e(TAG, "onActivityResult: Invalid result or data is null");
        }
    }

    private Bitmap resizeBitmap(Bitmap original) {
        return Bitmap.createScaledBitmap(original, 500, 500, true);
    }

    private void uploadImageToImgBB(Uri imageUri) {
        Log.d(TAG, "uploadImageToImgBB: uploading image " + imageUri);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            Bitmap resized = resizeBitmap(bitmap);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imageBytes = stream.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("key", IMGBB_API_KEY)
                    .add("image", base64Image)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build();

            Log.d(TAG, "uploadImageToImgBB: sending request to ImgBB");

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "ImgBB upload failed: " + e.getMessage(), e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String resp = response.body().string();
                    Log.d(TAG, "ImgBB upload response: " + resp);

                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(resp);
                            String imageUrl = jsonObject.getJSONObject("data").getString("url");
                            Log.d(TAG, "Image uploaded to ImgBB: " + imageUrl);
                            saveImageUrlToDatabase(imageUrl);
                            updateProfileImage(imageUrl);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
                        }
                    } else {
                        Log.e(TAG, "ImgBB upload failed: code = " + response.code());
                    }
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "uploadImageToImgBB: Error getting image from URI", e);
        }
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "saveImageUrlToDatabase: currentUser is null");
            return;
        }

        String userId = currentUser.getUid();
        databaseReference.child(userId).child("avatar").setValue(imageUrl)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Image URL saved to database"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save image URL to database", e));
    }

    private void updateProfileImage(String imageUrl) {
        requireActivity().runOnUiThread(() -> {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(avatarImageView);
        });
    }
}
