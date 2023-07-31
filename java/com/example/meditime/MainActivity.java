package com.example.meditime;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meditime.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int HOME_ID = R.id.home;
    private static final int MEDICATION_ID = R.id.medication;
    private static final int UPLOAD_ID = R.id.upload;
    private static final int LOGOUT_ID = R.id.logout;
    private DatabaseReference databaseReference;

    private RecyclerView recyclerView;
    private YourAdapter adapter;
    private List<DataModel> dataList;

    TextView textView;

    ActivityMainBinding binding;
    private Bundle savedInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new Home());

        recyclerView = findViewById(R.id.recyclerView); // Find the RecyclerView in the layout

        // Initialize the data list and adapter
        dataList = new ArrayList<>();
        adapter = new YourAdapter(dataList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == HOME_ID) {
                replaceFragment(new Home());
            } else if (itemId == MEDICATION_ID) {
                showMedicationDialog();
            } else if (itemId == UPLOAD_ID) {
                replaceFragment(new UploadFragment());
            } else if (itemId == LOGOUT_ID) {
                logout(); // Call the logout method here
            }
            return true;
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("reminders");

        // Call the method to retrieve data from Firebase and update the UI
        retrieveDataFromFirebase();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        LinearLayout addLayout = dialog.findViewById(R.id.addReminder);
        LinearLayout shortsLayout = dialog.findViewById(R.id.deleteReminder);
        LinearLayout liveLayout = dialog.findViewById(R.id.layoutLive);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        addLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showMedicationDialog();
            }
        });




        liveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Go live is Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    //medicine
    private void showMedicationDialog() {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.custom_dialog);

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.ic_background);

        TextView genericTextView = dialog.findViewById(R.id.txtGeneric);
        genericTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showGenericTypeDialog();
            }
        });

        TextView brandedTextView = dialog.findViewById(R.id.txtBranded);
        brandedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showBrandedTypeDialog();
            }
        });

        dialog.show();
    }

    private void showGenericTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Generic Medication")
                .setItems(new CharSequence[]{"Cough", "Fever", "Diarrhea", "Dysmenorrhea", "Headache", "Sorethroat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        String selectedCategory = "";
                        if (position == 0) {
                            selectedCategory = "genericCough";
                            navigateToGenericCough();
                        } else if (position == 1) {
                            selectedCategory = "genericFever";
                            navigateToGenericFever();
                        } else if (position == 2) {
                            selectedCategory = "genericDiarrhea";
                            navigateToGenericDiarrhea();
                        } else if (position == 3) {
                            selectedCategory = "genericDysmenorrhea";
                            navigateToGenericDysmenorrhea();
                        } else if (position == 4) {
                            selectedCategory = "genericHeadache";
                            navigateToGenericHeadache();
                        } else if (position == 5) {
                            selectedCategory = "genericSorethroat";
                            navigateToGenericSoreThroat();
                        }
                        saveDataToFirebase(selectedCategory, getCurrentDateTime());
                        Toast.makeText(MainActivity.this, "Selected Medication Category: " + selectedCategory, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }


    // Add this method to get the current date and time
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void navigateToGenericCough() {
        Intent intent = new Intent(MainActivity.this, GenericCough.class);
        startActivity(intent);
    }

    private void navigateToGenericFever() {
        Intent intent = new Intent(MainActivity.this, GenericFever.class);
        startActivity(intent);
    }

    private void navigateToGenericDiarrhea() {
        Intent intent = new Intent(MainActivity.this, GenericDiarrhea.class);
        startActivity(intent);
    }

    private void navigateToGenericDysmenorrhea() {
        Intent intent = new Intent(MainActivity.this, GenericDysmenorrhea.class);
        startActivity(intent);
    }

    private void navigateToGenericHeadache() {
        Intent intent = new Intent(MainActivity.this, GenericHeadache.class);
        startActivity(intent);
    }

    private void navigateToGenericSoreThroat() {
        Intent intent = new Intent(MainActivity.this, GenericSoreThroat.class);
        startActivity(intent);
    }

    private void showBrandedTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Branded Medication")
                .setItems(new CharSequence[]{"Cough", "Fever", "Diarrhea", "Dysmenorrhea", "Headache", "Sorethroat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        String selectedCategory = "";
                        if (position == 0) {
                            selectedCategory = "brandedCough";
                            navigateToBrandedCough();
                        } else if (position == 1) {
                            selectedCategory = "brandedFever";
                            navigateToBrandedFever();
                        } else if (position == 2) {
                            selectedCategory = "brandedDiarrhea";
                            navigateToBrandedDiarrhea();
                        } else if (position == 3) {
                            selectedCategory = "brandedDysmenorrhea";
                            navigateToBrandedDysmenorrhea();
                        } else if (position == 4) {
                            selectedCategory = "brandedHeadache";
                            navigateToBrandedHeadache();
                        } else if (position == 5) {
                            selectedCategory = "brandedSorethroat";
                            navigateToBrandedSoreThroat();
                        }
                        saveDataToFirebase(selectedCategory, getCurrentDateTime());
                        Toast.makeText(MainActivity.this, "Selected Medication Category: " + selectedCategory, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }


    private void navigateToBrandedCough() {
        Intent intent = new Intent(MainActivity.this, BrandedCough.class);
        startActivity(intent);
    }

    private void navigateToBrandedFever() {
        Intent intent = new Intent(MainActivity.this, BrandedFever.class);
        startActivity(intent);
    }

    private void navigateToBrandedDiarrhea() {
        Intent intent = new Intent(MainActivity.this, BrandedDiarrhea.class);
        startActivity(intent);
    }

    private void navigateToBrandedDysmenorrhea() {
        Intent intent = new Intent(MainActivity.this, BrandedDysmenorrhea.class);
        startActivity(intent);
    }

    private void navigateToBrandedHeadache() {
        Intent intent = new Intent(MainActivity.this, BrandedHeadache.class);
        startActivity(intent);
    }

    private void navigateToBrandedSoreThroat() {
        Intent intent = new Intent(MainActivity.this, BrandedSoreThroat.class);
        startActivity(intent);
    }


    // Add this method to save data to Firebase Realtime Database
    private void saveDataToFirebase(String medicine, String dateTimeValue) {
        String category;
        switch (medicine) {
            case "Cough":
                category = "genericCough";
                break;
            case "Fever":
                category = "genericFever";
                break;
            case "Diarrhea":
                category = "genericDiarrhea";
                break;
            case "Dysmenorrhea":
                category = "genericDysmenorrhea";
                break;
            case "Headache":
                category = "genericHeadache";
                break;
            case "Sorethroat":
                category = "genericSorethroat";
                break;
            case "Branded Diarrhea":
                category = "brandedDiarrhea";
                break;
            case "Branded Headache":
                category = "brandedHeadache";
                break;
            case "Branded Sorethroat":
                category = "brandedSoreThroat";
                break;
            case "Branded Cough":
                category = "brandedCough";
                break;
            case "Branded Dysmenorrhea":
                category = "brandedDysmenorrhea";
                break;
            case "Branded Fever":
                category = "brandedFever";
                break;
            default:
                // Default category in case the selected medication is not found
                category = "genericMedication";
                break;
        }

        String key = databaseReference.child(category).push().getKey();
        if (key != null) {
            DatabaseReference medicationRef = databaseReference.child(category).child(key);
            medicationRef.child("medicine").setValue(medicine);
            medicationRef.child("dateTime").setValue(dateTimeValue)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Data saved to Firebase", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to save data to Firebase", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    // Add this method to retrieve data from the Firebase Realtime Database for all medication categories
    private void retrieveDataFromFirebase() {
        // Clear the dataList before adding new data
        dataList.clear();

        String[] medicationCategories = {
                "brandedDiarrhea", "brandedHeadache", "brandedSorethroat",
                "brandedCough", "brandedDysmenorrhea", "brandedFever",
                "genericDiarrhea", "genericHeadache", "genericSoreThroat",
                "genericCough", "genericDysmenorrhea", "genericFever"
        };

        for (String category : medicationCategories) {
            databaseReference.child(category).get().addOnCompleteListener(dataTask -> {
                if (dataTask.isSuccessful()) {
                    if (dataTask.getResult().hasChildren()) {
                        for (DataSnapshot dataSnapshot : dataTask.getResult().getChildren()) {
                            String medicine = dataSnapshot.child("medicine").getValue(String.class);
                            String dateTimeValue = dataSnapshot.child("dateTime").getValue(String.class);

                            if (dateTimeValue != null && !dateTimeValue.isEmpty()) {
                                // Split the dateTimeValue into date and time components
                                String[] parts = dateTimeValue.split(" ");
                                String date = "";
                                String time = "";
                                if (parts.length >= 2) {
                                    date = parts[0];
                                    time = parts[1];
                                }

                                // Add the retrieved data to the dataList (List<DataModel>)
                                dataList.add(new DataModel(medicine, date, time));
                            }
                        }

                        // Notify the adapter that the data has changed
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle the case when there is no data in the category node
                        Toast.makeText(MainActivity.this, "No data found for " + category, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case when the data retrieval fails
                    Toast.makeText(MainActivity.this, "Failed to fetch data for " + category, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Add this method to handle logout
    private void logout() {
        // Perform any cleanup or additional tasks before logging out if needed.

        // Start the Login activity and clear the back stack.
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}