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
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import android.view.ViewGroup;

public class UserInfoDialogFragment extends DialogFragment {
    ImageView avatarImageView;
    TextView userNameTextView;
    TextView emailTextView;
    TextView createdCountTextView;
    TextView savedCountTextView;
    LayoutInflater inflater;

    private static final String ARG_USER_ID = "userId";
    private String userId;

    public static UserInfoDialogFragment newInstance(String userId) {
        UserInfoDialogFragment fragment = new UserInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getArguments().getString(ARG_USER_ID);
        setStyle(STYLE_NO_TITLE, R.style.UserInfoDialog);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.UserInfoDialog);
        inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_user_info_dialog, null);
        builder.setView(view);

        String userId = getArguments().getString(ARG_USER_ID);

        avatarImageView = view.findViewById(R.id.avatarImageView);
        userNameTextView = view.findViewById(R.id.userNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        createdCountTextView = view.findViewById(R.id.createdCountTextView);
        savedCountTextView = view.findViewById(R.id.savedCountTextView);

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId);

        loadUserProfile();


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

    private void loadUserProfile() {


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);

        ref.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (name != null) userNameTextView.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        ref.child("e-mail").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = snapshot.getValue(String.class);
                if (email != null) emailTextView.setText(email);
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
