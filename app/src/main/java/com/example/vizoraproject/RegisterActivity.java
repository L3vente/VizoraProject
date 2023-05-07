package com.example.vizoraproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = RegisterActivity.class.getPackage().toString();

    private FirebaseAuth mAuth;
    private NotificationHandler mNotificationHandler;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mNotificationHandler = new NotificationHandler(this);
    }

    @BindView(R.id.emailET)
    EditText emailET;
    @BindView(R.id.pswET)
    EditText pswET;
    @BindView(R.id.pswaET)
    EditText pswagainET;
    @BindView(R.id.firstnameET)
    EditText firstNameET;
    @BindView(R.id.lastnameET)
    EditText lastNameET;
    @BindView(R.id.viewLoading)
    ScrollView scrollView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @OnClick(R.id.cancelBTN)
    void cancel() {
        finishAfterTransition();
    }

    @OnClick(R.id.regBTN)
    void registerBTN() {
        if (validateForm()) {
            register();
        }
    }

    public void register() {
        scrollView.setBackgroundColor(Color.argb(50, 73, 65, 65));
        progressBar.setVisibility(View.VISIBLE);
        String email = emailET.getText().toString();
        String password = pswET.getText().toString();
        String firstname = firstNameET.getText().toString();
        String lastname = lastNameET.getText().toString();
        new Thread(() -> mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = new User(email, firstname, lastname);
                        db.collection("users").document(mAuth.getCurrentUser().getUid())
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    mNotificationHandler.send("A regiszráció sikeres! Mostantól be tudsz jelentkezni");
                                    finishAfterTransition();
                                })
                                .addOnFailureListener(e -> {Toast.makeText(RegisterActivity.this, "Error adding user to Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show(); scrollView.setBackgroundColor(Color.TRANSPARENT);
                        progressBar.setVisibility(View.INVISIBLE);});
                    } else {
                        scrollView.setBackgroundColor(Color.TRANSPARENT);
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterActivity.this, "User wasn't created successfully: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                })).start();
    }

    private boolean validateForm() {
        boolean valid = true;

        String username = emailET.getText().toString();
        if (TextUtils.isEmpty(username)) {
            emailET.setError("Az e-mail mező kitöltése kötelező.");
            valid = false;
        } else {
            emailET.setError(null);
        }

        String password = pswET.getText().toString();
        if (TextUtils.isEmpty(password)) {
            pswET.setError("A jelszó mező kitöltése kötelező.");
            valid = false;
        } else {
            pswET.setError(null);
        }

        String confirmPassword = pswagainET.getText().toString();
        if (!TextUtils.equals(password, confirmPassword)) {
            pswagainET.setError("A két jelszó mezőnek egyeznie kell.");
            valid = false;
        } else {
            pswagainET.setError(null);
        }

        return valid;
    }
}