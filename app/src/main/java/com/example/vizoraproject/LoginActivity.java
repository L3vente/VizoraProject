package com.example.vizoraproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {
    private static final String LOG_TAG = LoginActivity.class.getName();
    private static final String PREF_KEY = LoginActivity.class.getPackage().toString();
    private static final int RC_SIGN_IN = 1269;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;

    @BindView(R.id.scrollViewLogin)
    ScrollView scrollView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.emailET)
    EditText emailET;
    @BindView(R.id.pswET)
    EditText passwordET;

    @OnClick(R.id.loginBTN)
    void onOnclick() {
        if (validateForm()) {
            loginWithemailPsw();
        }
    }

    @BindView(R.id.logintext)
    TextView textView;


    @OnClick(R.id.noaccount)
    void start_register() {
        Intent intent = new Intent(this, RegisterActivity.class);
        ActivityOptions options = ActivityOptions
                .makeSceneTransitionAnimation(this, textView, "asd");
        startActivity(intent, options.toBundle());

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        ButterKnife.bind(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        db = FirebaseFirestore.getInstance();

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(LOG_TAG, "firebaseAuthGoogle: " + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(LOG_TAG, "Google sing in failed!");
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        scrollView.setBackgroundColor(Color.argb(50, 73, 65, 65));
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {

                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();
                        db.collection("users").document(userId).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot document = task1.getResult();
                                if (document.exists()) {
                                    startReports();
                                } else {
                                    String google_email = mAuth.getCurrentUser().getEmail();
                                    startReportsGoogle(google_email);
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                            scrollView.setBackgroundColor(Color.TRANSPARENT);
                            progressBar.setVisibility(View.INVISIBLE);
                        });
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    scrollView.setBackgroundColor(Color.TRANSPARENT);
                    progressBar.setVisibility(View.INVISIBLE);
                }

            });
        }).start();
    }

    private void startReportsGoogle(String email) {
        Intent intent = new Intent(this, RegisterGoogle.class);
        ActivityOptions options = ActivityOptions
                .makeSceneTransitionAnimation(this, textView, "asd");
        intent.putExtra("email", email);
        startActivity(intent, options.toBundle());


    }

    @OnClick(R.id.google_button)
    public void loginWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);


    }

    @OnClick(R.id.guest_button)
    public void loginGuest(View view) {
        scrollView.setBackgroundColor(Color.argb(50, 73, 65, 65));
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                startReportsGuest();
                scrollView.setBackgroundColor(Color.TRANSPARENT);
                progressBar.setVisibility(View.INVISIBLE);
            } else {
                scrollView.setBackgroundColor(Color.TRANSPARENT);
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(LoginActivity.this, "User login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }

    private void startReportsGuest() {
        Intent intent = new Intent(this, AddReportActivity.class);
        ActivityOptions options = ActivityOptions
                .makeSceneTransitionAnimation(this, textView, "asd");
        startActivity(intent, options.toBundle());

    }

    public void loginWithemailPsw() {
        ScrollView sc2 = scrollView;
        scrollView.setBackgroundColor(Color.argb(50, 73, 65, 65));
        progressBar.setVisibility(View.VISIBLE);
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                scrollView.setBackgroundColor(Color.TRANSPARENT);
                progressBar.setVisibility(View.INVISIBLE);
                startReports();
            } else {
                Toast.makeText(LoginActivity.this, "User login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                scrollView.setBackgroundColor(Color.TRANSPARENT);
                progressBar.setVisibility(View.INVISIBLE);
            }

        });
    }

    private void startReports() {
        Intent intent = new Intent(this, SignedInReports.class);
        ActivityOptions options = ActivityOptions
                .makeSceneTransitionAnimation(this, textView, "asd");
        startActivity(intent, options.toBundle());

    }

    private boolean validateForm() {
        boolean valid = true;
        // Ellenőrizzük, hogy a email mező kitöltött-e
        String username = emailET.getText().toString();
        if (TextUtils.isEmpty(username)) {
            emailET.setError("Az e-mail mező kitöltése kötelező.");
            valid = false;
        } else {
            emailET.setError(null);
        }

        // Ellenőrizzük, hogy a jelszó mező kitöltött-e
        String password = passwordET.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordET.setError("A jelszó mező kitöltése kötelező.");
            valid = false;
        } else {
            passwordET.setError(null);
        }

        return valid;
    }

}