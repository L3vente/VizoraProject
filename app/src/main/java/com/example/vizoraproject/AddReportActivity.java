package com.example.vizoraproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddReportActivity extends AppCompatActivity {
    private static final String PHOTO_PATH_KEY = "photoPath";
    private static final String IMAGE_URI_KEY = "image_uri";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String LOG_TAG = AddReportActivity.class.getName();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private NotificationHandler mNotificationHandler;
    private String userId;
    @BindView(R.id.addressET)
    EditText addressET;
    @BindView(R.id.factoryNumET)
    EditText factoryNumET;
    @BindView(R.id.dateET)
    EditText dateET;
    private Uri image;
    private boolean anonym;
    DatePickerDialog datePickerDialog;
    private String mCurrentPhotoPath = null;
    @BindView(R.id.positionET)
    EditText positionET;
    @BindView(R.id.viewLoading)
    ScrollView scrollView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @OnClick(R.id.cancelBTN)
    void cancel() {
        if (mCurrentPhotoPath != null) {
            File imageFile = new File(mCurrentPhotoPath);
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }

        finishAfterTransition();
    }

    @BindView(R.id.uploadImage)
    Button uploadBTN;

    @OnClick(R.id.submitBTN)
    void registerBTN() {
        if (validateForm()) {
            if (image != null) {
                uploadBTN.setError(null);
                uploadImageToFirestorage(image);
            } else {
                uploadBTN.setError("Kép feltöltése kötelező");
            }

        }
    }

    @OnClick(R.id.dateSelect)
    void date() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(AddReportActivity.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    dateET.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    @OnClick(R.id.uploadImage)
    void saveImage() {
        if (checkUserPermission()) {
            if (mCurrentPhotoPath == null) {
                takePhoto();
            } else {
                File imageFile = new File(mCurrentPhotoPath);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
                takePhoto();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);
        ButterKnife.bind(this);
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-M-dd", Locale.getDefault());
        dateET.setText(df.format(c));
        storageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> anonym = !documentSnapshot.exists());
        mNotificationHandler = new NotificationHandler(this);
    }

    boolean checkUserPermission() {
        if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_CAPTURE);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    public void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Hiba történt a fájl létrehozása közben: " + ex, Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra("output", photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmms").format(new Date());
        String imageFileName = userId + "_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private boolean validateForm() {
        boolean valid = true;

        String address = addressET.getText().toString();
        if (TextUtils.isEmpty(address)) {
            addressET.setError("A cím mező kitöltése kötelező.");
            valid = false;
        } else {
            addressET.setError(null);
        }

        String factoryNum = factoryNumET.getText().toString();
        if (TextUtils.isEmpty(factoryNum)) {
            factoryNumET.setError("A gyári szám mező kitöltése kötelező.");
            valid = false;
        } else {
            factoryNumET.setError(null);
        }

        String position = positionET.getText().toString();
        if (TextUtils.isEmpty(position)) {
            positionET.setError("Az aktuális mérőállás mező kitöltése kötelező.");
            valid = false;
        } else {
            positionET.setError(null);
        }

        return valid;
    }


    private void uploadImageToFirestorage(Uri photoURI) {
        scrollView.setBackgroundColor(Color.argb(50, 73, 65, 65));
        progressBar.setVisibility(View.VISIBLE);
        String address = addressET.getText().toString();
        String factoryNum = factoryNumET.getText().toString();
        String date = dateET.getText().toString();
        String position = positionET.getText().toString();
        StorageReference imageRef;
        String fileName;

        if (anonym) {
            fileName = factoryNum + "_" + photoURI.getLastPathSegment().split("_")[1] + "_" + photoURI.getLastPathSegment().split("_")[2];
        } else {
            fileName = photoURI.getLastPathSegment();
        }
        imageRef = storageRef.child("images/" + fileName);
        UploadTask uploadTask = imageRef.putFile(photoURI);

        uploadTask.addOnFailureListener(exception -> Toast.makeText(AddReportActivity.this, "Image upload failed: " + uploadTask.getException().getMessage(), Toast.LENGTH_LONG).show()).addOnSuccessListener(taskSnapshot -> {
            ReportItem item = new ReportItem(address, factoryNum, date, position, fileName);
            item.setUserId(userId);
            if (anonym) {
                db.collection("guestReports").document()
                        .set(item)
                        .addOnSuccessListener(aVoid -> {
                            mNotificationHandler.send("A vízóra lejelentve");
                            File imageFile = new File(mCurrentPhotoPath);
                            if (imageFile.exists()) {
                                imageFile.delete();
                            }
                            finishAfterTransition();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AddReportActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            scrollView.setBackgroundColor(Color.TRANSPARENT);
                            progressBar.setVisibility(View.INVISIBLE);
                        });
            } else {
                db.collection("userReports").add(item)
                        .addOnSuccessListener(aVoid -> {
                            mNotificationHandler.send("A vízóra lejelentve");
                            File imageFile = new File(mCurrentPhotoPath);
                            if (imageFile.exists()) {
                                imageFile.delete();
                            }
                            finishAfterTransition();
                        })
                        .addOnFailureListener(e -> {
                            scrollView.setBackgroundColor(Color.TRANSPARENT);
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(AddReportActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                image = Uri.fromFile(new File(mCurrentPhotoPath));
            }
        }

    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentPhotoPath != null) {
            outState.putString(PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
        if (image != null) {
            outState.putString(IMAGE_URI_KEY, image.toString());
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPhotoPath = savedInstanceState.getString(PHOTO_PATH_KEY);
        }
        if (savedInstanceState != null) {
            String uriString = savedInstanceState.getString(IMAGE_URI_KEY);
            if (uriString != null) {
                image = Uri.parse(uriString);
            }
        }
    }

}