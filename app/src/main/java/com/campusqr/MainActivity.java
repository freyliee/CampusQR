package com.campusqr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

public class MainActivity extends AppCompatActivity {

    private static final String CURRENT_STUDENT_ID = "1001"; // Demo user
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int CAMERA_REQUEST = 200;

    private BarcodeScanner scanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация ML Kit сканера
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        scanner = BarcodeScanning.getClient(options);

        ImageView qrImage = findViewById(R.id.qrImage);
        TextView name = findViewById(R.id.name);
        TextView group = findViewById(R.id.group);
        Button scanBtn = findViewById(R.id.scanButton);

        // Show current user's info
        StudentRepository.Student me = StudentRepository.findById(CURRENT_STUDENT_ID);
        if (me != null) {
            name.setText(me.name);
            group.setText("Группа: " + me.group);
            // Encode QR as "student:<id>"
            Bitmap bmp = generateQRCode("student:" + me.id);
            if (bmp != null) qrImage.setImageBitmap(bmp);
        }

        scanBtn.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                startCameraActivity();
            } else {
                requestCameraPermission();
            }
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraActivity();
            } else {
                Toast.makeText(this, "Разрешение на камеру необходимо для сканирования",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null) {
            String scannedData = data.getStringExtra("scanned_data");
            if (scannedData != null) {
                // Expecting format "student:<id>"
                String id = scannedData.startsWith("student:") ?
                        scannedData.substring("student:".length()) : scannedData;
                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra("student_id", id);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Не удалось считать QR код", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanner != null) {
            scanner.close();
        }
    }
}