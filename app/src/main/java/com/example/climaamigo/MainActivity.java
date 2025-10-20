package com.example.climaamigo;

import android.Manifest;
<<<<<<< HEAD
import android.content.Intent;
import android.content.pm.PackageManager;
=======
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
>>>>>>> 88514d0 (Modificacion Funcional)
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView textClima;
    private ExecutorService cameraExecutor;
    private ClimateAnalyzer analyzer;
<<<<<<< HEAD
=======
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isPointingToSky = false;
    private static final float PITCH_THRESHOLD = 45f;
    private MediaPlayer mediaPlayer;
    private String currentWeather = "";
>>>>>>> 88514d0 (Modificacion Funcional)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        textClima = findViewById(R.id.textClima);
        cameraExecutor = Executors.newSingleThreadExecutor();
<<<<<<< HEAD

        analyzer = new ClimateAnalyzer(getApplicationContext());

=======
        analyzer = new ClimateAnalyzer(getApplicationContext());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

>>>>>>> 88514d0 (Modificacion Funcional)
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) startCamera();
                    else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

<<<<<<< HEAD
=======
    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float pitch = (float) Math.toDegrees(Math.atan2(-y, Math.sqrt(x * x + z * z)));
                if (pitch < 0) pitch = -pitch;

                isPointingToSky = (pitch > PITCH_THRESHOLD);

                if (!isPointingToSky) {
                    runOnUiThread(() -> textClima.setText("Apunta la cámara al cielo"));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

>>>>>>> 88514d0 (Modificacion Funcional)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, (ImageProxy image) -> {
<<<<<<< HEAD
            analyzer.analyzeImage(image, clima -> {
                runOnUiThread(() -> {
                    textClima.setText("Clima detectado: " + clima);
                    Intent intent = new Intent("com.example.climaamigo.UPDATE_CLIMA");
                    intent.putExtra("clima", clima);
                    sendBroadcast(intent);
                });
                image.close();
            });
=======
            if (isPointingToSky) {
                analyzer.analyzeImage(image, clima -> {
                    runOnUiThread(() -> {
                        if (currentWeather.equals(clima)) {
                            image.close();
                            return;
                        }
                        currentWeather = clima;
                        textClima.setText("Clima detectado: " + clima);

                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }

                        int audioRes = R.raw.indefinido;
                        if (clima.contains("Soleado")) {
                            audioRes = R.raw.soleado;
                        } else if (clima.contains("Nublado")) {
                            audioRes = R.raw.nublado;
                        } else if (clima.contains("Lluvioso")) {
                            audioRes = R.raw.lluvia;
                        }

                        mediaPlayer = MediaPlayer.create(MainActivity.this, audioRes);
                        if (mediaPlayer != null) {
                            mediaPlayer.start();
                        }

                        Intent intent = new Intent("com.example.climaamigo.UPDATE_CLIMA");
                        intent.putExtra("clima", clima);
                        sendBroadcast(intent);
                    });
                    image.close();
                });
            } else {
                image.close();
            }
>>>>>>> 88514d0 (Modificacion Funcional)
        });

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    @Override
<<<<<<< HEAD
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
=======
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
>>>>>>> 88514d0 (Modificacion Funcional)
