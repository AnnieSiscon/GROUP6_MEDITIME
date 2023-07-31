package com.example.meditime;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;
import android.provider.MediaStore;
import android.net.Uri;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.meditime.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Upload extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private SharedPreferences sharedPreferences;
    private static final String PREF_IMAGE_URL = "pref_image_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_upload);

        imageView = findViewById(R.id.imageview);
        Button uploadButton = findViewById(R.id.uploadbutton);
        Button captureButton = findViewById(R.id.capturebutton);

        // Initialize Firebase database and storage
        databaseReference = FirebaseDatabase.getInstance().getReference("images");
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                } else {
                    // Camera not available
                    Toast.makeText(Upload.this, "Camera not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load the image from Firebase Storage if available
        loadImageFromFirebaseStorage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                // Image selected from gallery
                Uri imageUri = data.getData();
                imageView.setImageURI(imageUri);

                // Save the image file to Firebase Storage
                saveImageToFirebaseStorage(imageUri);
            } else if (data != null && data.getExtras() != null) {
                // Image captured from camera
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bitmap);

                // Convert the bitmap to Uri
                Uri imageUri = getImageUri(bitmap);

                // Save the image file to Firebase Storage
                saveImageToFirebaseStorage(imageUri);
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void saveImageToFirebaseStorage(Uri imageUri) {
        StorageReference fileReference = storageReference.child("images/" + System.currentTimeMillis() + ".jpg");
        UploadTask uploadTask = fileReference.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get the image URL from Firebase Storage
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                // Save the image URL to Firebase Database
                String imageUrl = uri.toString();
                databaseReference.child("image").setValue(imageUrl);

                // Save the image URL to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_IMAGE_URL, imageUrl);
                editor.apply();
            });
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            // Handle failure
        });
    }

    private void loadImageFromFirebaseStorage() {
        databaseReference.child("image").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String imageUrl = dataSnapshot.getValue(String.class);
                if (imageUrl != null) {
                    // Load the image using Glide or any other image loading library
                    Glide.with(Upload.this)
                            .load(imageUrl)
                            .into(imageView);

                    // Save the image URL to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PREF_IMAGE_URL, imageUrl);
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                // Handle failure
            }
        });
    }
}
