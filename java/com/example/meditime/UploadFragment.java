package com.example.meditime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class UploadFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private SharedPreferences sharedPreferences;
    private static final String PREF_IMAGE_URL = "pref_image_url";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);



        imageView = view.findViewById(R.id.imageview);
        Button uploadButton = view.findViewById(R.id.uploadbutton);
        Button captureButton = view.findViewById(R.id.capturebutton);

        // Initialize Firebase database and storage
        databaseReference = FirebaseDatabase.getInstance().getReference("images");
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

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
                if (requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                } else {
                    // Camera not available
                    Toast.makeText(requireContext(), "Camera not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load the image from Firebase Storage if available
        loadImageFromFirebaseStorage();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK) {
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
        String path = MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), bitmap, "Title", null);
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
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageUrl = dataSnapshot.getValue(String.class);
                if (imageUrl != null) {
                    // Load the image using Glide or any other image loading library
                    Glide.with(requireContext())
                            .load(imageUrl)
                            .into(imageView);

                    // Save the image URL to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PREF_IMAGE_URL, imageUrl);
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                // Handle failure
            }
        });
    }
}
