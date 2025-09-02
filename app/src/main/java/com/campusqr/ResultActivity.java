package com.campusqr;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView resultName = findViewById(R.id.resultName);
        TextView resultGroup = findViewById(R.id.resultGroup);
        TextView resultStatus = findViewById(R.id.resultStatus);
        Button scanAgain = findViewById(R.id.scanAgain);

        String id = getIntent().getStringExtra("student_id");
        StudentRepository.Student s = StudentRepository.findById(id);
        if (s == null) {
            Toast.makeText(this, "Студент не найден: " + id, Toast.LENGTH_LONG).show();
            resultName.setText("ФИО: —");
            resultGroup.setText("Группа: —");
            resultStatus.setText("Статус: —");
        } else {
            resultName.setText("ФИО: " + s.name);
            resultGroup.setText("Группа: " + s.group);
            resultStatus.setText("Статус: " + s.status);
        }

        scanAgain.setOnClickListener(v -> {
            finish();
        });
    }
}