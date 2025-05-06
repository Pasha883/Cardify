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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import android.view.ViewGroup;

public class UserInfoDialogFragment extends DialogFragment {
    ImageView avatarImageView;
    TextView userNameTextView;
    TextView emailTextView;
    TextView createdCountTextView;
    TextView savedCountTextView;

    private static final String ARG_USER_ID = "userId";

    public static UserInfoDialogFragment newInstance(String userId) {
        UserInfoDialogFragment fragment = new UserInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info_dialog, container, false);

        String userId = getArguments().getString(ARG_USER_ID);

        avatarImageView = view.findViewById(R.id.avatarImageView);
        userNameTextView = view.findViewById(R.id.userNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        createdCountTextView = view.findViewById(R.id.createdCountTextView);
        savedCountTextView = view.findViewById(R.id.savedCountTextView);

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId);

        loadUserProfile();


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
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
