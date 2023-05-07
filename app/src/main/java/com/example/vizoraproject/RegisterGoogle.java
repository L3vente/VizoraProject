package com.example.vizoraproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
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

public class RegisterGoogle extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    @BindView(R.id.firstnameET)
    EditText firstNameET;
    @BindView(R.id.lastnameET)
    EditText lastNameET;
    @BindView(R.id.reg)
     TextView textView;
    @BindView(R.id.viewLoading)
    ScrollView scrollView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_google);
        ButterKnife.bind(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
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
        String firstname = firstNameET.getText().toString();
        String lastname = lastNameET.getText().toString();
        new Thread(() -> {
            User user = new User(getIntent().getStringExtra("email"), firstname, lastname);
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        finish();
                        Intent intent = new Intent(this, SignedInReports.class);
                        ActivityOptions options = ActivityOptions
                                .makeSceneTransitionAnimation(this, textView, "asd");
                        startActivity(intent, options.toBundle());
                        scrollView.setBackgroundColor(Color.TRANSPARENT);
                        progressBar.setVisibility(View.INVISIBLE);
                    })
                    .addOnFailureListener(e -> {
                        scrollView.setBackgroundColor(Color.TRANSPARENT);
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterGoogle.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }).start();
    }

    private boolean validateForm() {
        boolean valid = true;

        String firstName = firstNameET.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            firstNameET.setError("A vezetéknév mező kitöltése kötelező.");
            valid = false;
        } else {
            firstNameET.setError(null);
        }
        String lastName = lastNameET.getText().toString();
        if (TextUtils.isEmpty(lastName)) {
            lastNameET.setError("A keresztnév mező kitöltése kötelező.");
            valid = false;
        } else {
            lastNameET.setError(null);
        }

        return valid;
    }
}