package com.campusqr;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class ResultActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView resultName = findViewById(R.id.resultName);
        TextView resultGroup = findViewById(R.id.resultGroup);
        TextView resultStatus = findViewById(R.id.resultStatus);
        Button scanAgain = findViewById(R.id.scanAgain);

        db = FirebaseFirestore.getInstance();

        // Получаем ID студента из Intent
        String id = getIntent().getStringExtra("student_id");
        if (id == null) {
            Toast.makeText(this, "Нет данных студента", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Запрашиваем данные из Firestore
        db.collection("users").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Student s = documentSnapshot.toObject(Student.class);
                        if (s != null) {
                            s.id = id;
                            resultName.setText("ФИО: " + s.name);
                            resultGroup.setText("Группа: " + s.group);
                            resultStatus.setText("Статус: " + s.status);
                        } else {
                            showEmptyData(resultName, resultGroup, resultStatus);
                        }
                    } else {
                        showEmptyData(resultName, resultGroup, resultStatus);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при загрузке данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    showEmptyData(resultName, resultGroup, resultStatus);
                });

        scanAgain.setOnClickListener(v -> finish());
    }

    private void showEmptyData(TextView name, TextView group, TextView status) {
        name.setText("ФИО: —");
        group.setText("Группа: —");
        status.setText("Статус: —");
    }
}
