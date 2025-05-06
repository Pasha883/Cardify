// RegisterActivity.java
package com.example.cardify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputConfirmPassword;
    private Button btnRegister;
    private TextView linkToLogin;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.editTextEmail);
        inputPassword = findViewById(R.id.editTextPassword);
        inputConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        linkToLogin = findViewById(R.id.txtLogin);
        progressBar = findViewById(R.id.progressBar);

        linkToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Добавляем эту строку
        });

        btnRegister.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            String confirm = inputConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
                Toast.makeText(getApplicationContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(getApplicationContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            String uid = auth.getCurrentUser().getUid();

                            // Создание данных пользователя
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("id", uid);
                            userData.put("e-mail", email);
                            userData.put("paswd", password); // Лучше не хранить так, а использовать только Firebase Auth
                            String randomName = generateRandomName();
                            userData.put("name", randomName);
                            userData.put("isVisible", true);
                            userData.put("createdVizitcards", new HashMap<>());
                            userData.put("savedVizitcards", new HashMap<>());

                            usersRef.child(uid).setValue(userData)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Ошибка при сохранении данных", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Ошибка регистрации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private String generateRandomName() {
        String[] adjectives = {
                "Sunny", "Mighty", "Happy", "Clever", "Bright", "Lucky", "Brave", "Quiet", "Fast", "Cool",
                "Gentle", "Fierce", "Bold", "Silent", "Swift", "Loyal", "Witty", "Shiny", "Jolly", "Nimble",
                "Silly", "Glorious", "Sneaky", "Charming", "Feisty", "Daring", "Graceful", "Wild", "Kind", "Curious"
        };

        String[] nouns = {
                "Fox", "Tiger", "Pine", "River", "Wolf", "Eagle", "Bear", "Falcon", "Hawk", "Cloud",
                "Star", "Storm", "Shadow", "Leaf", "Blossom", "Rock", "Flame", "Rain", "Wind", "Stone",
                "Moon", "Sun", "Hill", "Snow", "Sky", "Tree", "Lion", "Otter", "Panther", "Whale"
        };

        int adjIndex = (int) (Math.random() * adjectives.length);
        int nounIndex = (int) (Math.random() * nouns.length);

        return adjectives[adjIndex] + nouns[nounIndex] + (int)(Math.random() * 1000);

    }

}
