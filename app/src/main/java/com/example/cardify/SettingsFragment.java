package com.example.cardify;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private TextView textProfileName;
    private LinearLayout layoutAbout;
    private LinearLayout layoutTheme;
    private ImageView imageThemeIcon;
    private LinearLayout logoutLayout;
    private ImageView imageProfile;
    View profileSection;

    private DatabaseReference databaseReference;
    private boolean isDarkTheme;
    private FirebaseAuth mAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    // Замените на ваш собственный API ключ ImgBB
    private static final String IMGBB_API_KEY = "8c60ccf329b617800e1928f60d7c0382";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: fragment created");

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        textProfileName = view.findViewById(R.id.textProfileName);
        layoutAbout = view.findViewById(R.id.layoutAbout);
        layoutTheme = view.findViewById(R.id.layoutTheme);
        imageThemeIcon = view.findViewById(R.id.imageThemeIcon);
        logoutLayout = view.findViewById(R.id.layoutLogout);
        imageProfile = view.findViewById(R.id.imageProfile);
        profileSection = view.findViewById(R.id.profileHeader); // верхняя часть с аватаркой и именем

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        loadUserProfile();
        setupAboutClickListener();
        setupThemeClickListener();
        setupLogoutClickListener();

        //Вызов функции открытия файла
//        imageProfile.setOnClickListener(v -> {
//            Log.d(TAG, "Profile image clicked");
//            openFileChooser();
//        });

        LinearLayout layoutPrivacy = view.findViewById(R.id.layoutPrivacy);
        layoutPrivacy.setOnClickListener(v -> {
            // Получи текущее значение из Firebase или кэша
            getVisibilityStatusAndShowDialog();
        });

        profileSection.setOnClickListener(v -> {
            InfoDialogFragment dialog = new InfoDialogFragment();
            dialog.setOnDialogCloseListener(() -> {
                // Это будет вызвано, когда диалог закроется
                Log.d("SettingsFragment", "InfoDialogFragment был закрыт");
                // Здесь можешь обновить UI, например, заново загрузить имя/аватар/визитки
                loadUserProfile();
            });
            dialog.show(getParentFragmentManager(), "UserInfoDialog");
        });


        updateThemeIcon();

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "loadUserProfile: currentUser is null");
            return;
        }

        String userId = currentUser.getUid();
        databaseReference.child(userId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null) {
                            textProfileName.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "loadUserProfile: onCancelled - " + error.getMessage());
                    }
                });

        databaseReference.child(userId).child("avatar")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String avatarUrl = snapshot.getValue(String.class);
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Picasso.get()
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .into(imageProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "loadUserProfile: onCancelled - " + error.getMessage());
                    }
                });
    }

    private void setupAboutClickListener() {
        layoutAbout.setOnClickListener(v -> showAboutDialog());
    }

    private void setupThemeClickListener() {
        layoutTheme.setOnClickListener(v -> {
            isDarkTheme = !isDarkTheme;
            saveThemePreference(isDarkTheme);
            applyTheme();
        });
    }

    private void setupLogoutClickListener() {
        mAuth = FirebaseAuth.getInstance();
        logoutLayout.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.DeleteCardDialog)
                    .setTitle("Выйти из аккаунта?")
                    .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                    .setPositiveButton("Да", (dialogInterface, which) -> {
                        mAuth.signOut();
                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    })
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
    }

    private void applyTheme() {
        if (isDarkTheme) {
            ThemeManager.saveTheme(requireContext(), true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            ThemeManager.saveTheme(requireContext(), false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("openSettings", true);
        startActivity(intent);
        //requireActivity().finish();
        //requireActivity().overridePendingTransition(0, 0);
    }

    private void updateThemeIcon() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("settings", getContext().MODE_PRIVATE);
        isDarkTheme = prefs.getBoolean("dark_theme", false);

        if (isDarkTheme) {
            imageThemeIcon.setImageResource(R.drawable.ic_sun);
        } else {
            imageThemeIcon.setImageResource(R.drawable.ic_moon);
        }
    }

    private void saveThemePreference(boolean darkTheme) {
        SharedPreferences.Editor editor = requireActivity()
                .getSharedPreferences("settings", getContext().MODE_PRIVATE)
                .edit();
        editor.putBoolean("dark_theme", darkTheme);
        editor.apply();
    }

    private void showAboutDialog() {
        String versionName = "1.0";
        try {
            versionName = requireContext()
                    .getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0)
                    .versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        String message = "Название: Cardify\n"
                + "Версия: " + versionName + "\n"
                + "Версия CardifyUI: 1.02.1\n"
                + "PashaCO 2016–2025\n"
                + "Все права защищены.";

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.FilterDialog)
                .setTitle("О приложении")
                .setMessage(message)
                .setPositiveButton("Ок", null).create();

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
                    .into(imageProfile);
        });
    }

    private void getVisibilityStatusAndShowDialog() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId).child("isVisible");

        ref.get().addOnSuccessListener(dataSnapshot -> {
            boolean currentVisibility = dataSnapshot.getValue(Boolean.class) != null && dataSnapshot.getValue(Boolean.class);
            PrivacyDialogFragment dialog = new PrivacyDialogFragment(currentVisibility);
            dialog.show(getParentFragmentManager(), "PrivacyDialog");
        });
    }
}
