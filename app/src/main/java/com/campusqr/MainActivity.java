package com.campusqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int CAMERA_REQUEST = 200;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BarcodeScanner scanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Инициализация ML Kit сканера (для QR-кодов)
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE)
                .build();
        scanner = BarcodeScanning.getClient(options);

        ImageView qrImage = findViewById(R.id.qrImage);
        TextView name = findViewById(R.id.name);
        TextView group = findViewById(R.id.group);
        Button scanBtn = findViewById(R.id.scanButton);
        Button logoutBtn = findViewById(R.id.logoutButton);

        // Загружаем данные текущего пользователя и генерируем QR-код
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Student s = documentSnapshot.toObject(Student.class);
                        if (s != null) {
                            s.id = uid;
                            name.setText(s.name);
                            group.setText("Группа: " + s.group);
                            Bitmap bmp = generateQRCode("student:" + s.id);
                            if (bmp != null) qrImage.setImageBitmap(bmp);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(MainActivity.this, "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }

        // Кнопка сканирования
        scanBtn.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                startCameraActivity();
            } else {
                requestCameraPermission();
            }
        });

        // Кнопка выхода
        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
    }

    private void startCameraActivity() {
        Intent intent = new Intent(this, CameraScanActivity.class);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private Bitmap generateQRCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int size = 512;
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Получение результата из CameraScanActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null) {
            String scannedData = data.getStringExtra("scanned_data");
            if (scannedData != null && scannedData.startsWith("student:")) {
                String studentId = scannedData.substring("student:".length());

                // Открываем ResultActivity с переданным student_id
                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra("student_id", studentId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Неверный QR-код", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startCameraActivity();
            } else {
                Toast.makeText(this, "Разрешение на камеру необходимо для сканирования", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanner != null) scanner.close();
    }
}
