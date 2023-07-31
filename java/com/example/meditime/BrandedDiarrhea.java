package com.example.meditime;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BrandedDiarrhea extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> medicineList;

    private String selectedMedicine;
    private Calendar calendar;
    private int hourOfDay, minute;

    // Change the reference to "brandedDiarrheaData"
    private DatabaseReference saveDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branded_diarrhea);

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
        medicineList.add("Loperamide 8.00");
        medicineList.add("Imodium 10.00");
        medicineList.add("Diatabs 5.50");
        medicineList.add("Kremil S 9.75");
        medicineList.add("Bisacodyl 6.50");
        adapter.notifyDataSetChanged();

        calendar = Calendar.getInstance();

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        saveDatabaseReference = database.getReference("reminders").child("brandedDiarrhea");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedMedicine = medicineList.get(position);

                DatePickerDialog datePickerDialog = new DatePickerDialog(BrandedDiarrhea.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(BrandedDiarrhea.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                BrandedDiarrhea.this.hourOfDay = hourOfDay;
                                BrandedDiarrhea.this.minute = minute;

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
                            Toast.makeText(BrandedDiarrhea.this, "Data saved to Firebase", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BrandedDiarrhea.this, "Failed to save data to Firebase", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
