package com.example.meditime;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BrandedCough extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> medicineList;

    private String selectedMedicine;
    private Calendar calendar;
    private int hourOfDay, minute;

    // Change the reference to "brandedCoughData"
    private DatabaseReference saveDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branded_cough);

        listView = findViewById(R.id.listView);
        medicineList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, medicineList);
        listView.setAdapter(adapter);





        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click event to go back to MainActivity
                onBackPressed();
            }
        });
        // Add sample medicines for testing
        medicineList.add("Solmux 11.35");
        medicineList.add("Tuseran Forte 10.00");
        medicineList.add("Neozep Forte 6.50");
        medicineList.add("Bioflu 9.00");
        medicineList.add("Kremil S 9.75");
        adapter.notifyDataSetChanged();

        calendar = Calendar.getInstance();

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        saveDatabaseReference = database.getReference("reminders").child("brandedCough");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedMedicine = medicineList.get(position);

                DatePickerDialog datePickerDialog = new DatePickerDialog(BrandedCough.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(BrandedCough.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                BrandedCough.this.hourOfDay = hourOfDay;
                                BrandedCough.this.minute = minute;

                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                String dateTime = dateFormat.format(calendar.getTime());

                                // Save the selected medicine, date, and time to Firebase Realtime Database
                                saveDataToFirebase(selectedMedicine, dateTime);
                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

                        timePickerDialog.show();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.show();
            }
        });
    }

    // Add this method to save data to Firebase Realtime Database
    private void saveDataToFirebase(String medicine, String dateTimeValue) {
        String key = saveDatabaseReference.push().getKey();
        if (key != null) {
            DatabaseReference medicationRef = saveDatabaseReference.child(key);
            medicationRef.child("medicine").setValue(medicine);
            medicationRef.child("dateTime").setValue(dateTimeValue)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(BrandedCough.this, "Data saved to Firebase", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BrandedCough.this, "Failed to save data to Firebase", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }






    private void showDeleteMedicineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Medicine");

        // Set up the input field
        final EditText etMedicineName = new EditText(this);
        etMedicineName.setInputType(InputType.TYPE_CLASS_TEXT);
        etMedicineName.setHint("Enter Medicine Name");

        // Add the input field to the dialog
        builder.setView(etMedicineName);

        // Set up the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String medicineName = etMedicineName.getText().toString().trim();
                if (!TextUtils.isEmpty(medicineName)) {
                    // Call the new delete function to delete the data from Firebase
                    deleteDataFromFirebase(medicineName);
                } else {
                    Toast.makeText(BrandedCough.this, "Please enter a medicine name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        builder.show();
    }



    private void deleteDataFromFirebase(String medicineName) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        String key = saveDatabaseReference.push().getKey();
        Query query = databaseRef.child(key).orderByChild("medicine").equalTo(medicineName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Delete the data from Firebase
                        snapshot.getRef().removeValue();
                    }
                    Toast.makeText(BrandedCough.this, "Data deleted from Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BrandedCough.this, "Medicine not found in Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(BrandedCough.this, "Failed to delete data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
