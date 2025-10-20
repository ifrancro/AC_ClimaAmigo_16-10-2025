package com.example.climaamigo;

import android.content.Context;
import android.media.Image;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClimateAnalyzer {

    public interface ClimateCallback {
        void onResult(String clima);
    }

    private final ImageLabeler labeler;
    private final ExecutorService executor;

    private static final String TAG = "ClimateAnalyzer";
    private static final float CLIMA_CONFIDENCE_THRESHOLD = 0.5f;

    public ClimateAnalyzer(Context context) {
        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(CLIMA_CONFIDENCE_THRESHOLD)
                .build();
        labeler = ImageLabeling.getClient(options);
        executor = Executors.newSingleThreadExecutor();
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public void analyzeImage(ImageProxy imageProxy, ClimateCallback callback) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.getImageInfo().getRotationDegrees()
        );

        labeler.process(image)
                .addOnSuccessListener(executor, labels -> {
                    String clima = interpretLabels(labels);
                    callback.onResult(clima);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error en etiquetado: " + e.getMessage()))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private String interpretLabels(List<ImageLabel> labels) {
        StringBuilder logLabels = new StringBuilder("Etiquetas detectadas:\n");
        boolean hasSky = false;
        float skyConfidence = 0f;
        boolean hasWater = false;
        float waterConfidence = 0f;
        boolean hasCloud = false;

        for (ImageLabel label : labels) {
            String text = label.getText().toLowerCase();
            float confidence = label.getConfidence();
            logLabels.append("Etiqueta: ").append(text)
                    .append(", Confianza: ").append(confidence).append("\n");

            if (text.contains("sky") || text.contains("sun") || text.contains("sunset") || text.contains("daytime")) {
                hasSky = true;
                skyConfidence = Math.max(skyConfidence, confidence);
            }
            if (text.contains("cloud")) {
                hasCloud = true;
            }
            if (text.contains("water")) {
                hasWater = true;
                waterConfidence = confidence;
            }
            if (text.contains("snow") || text.contains("ice")) {
                return "Frío ❄️";
            }
        }

        Log.d(TAG, logLabels.toString());

        if (!hasSky && !hasCloud) {
            return "Apunta al cielo para detectar el clima ☁️";
        }

        Log.d(TAG, "Cielo detectado con confianza: " + skyConfidence + " | Agua: " + hasWater + " (" + waterConfidence + ")");

        if (hasWater && hasSky && skyConfidence < 0.75f) {
            return "Lluvioso 🌧️";
        }
        if (hasCloud || (hasSky && skyConfidence < 0.8f)) {
            return "Nublado ☁️";
        }
        if (hasSky && skyConfidence >= 0.8f) {
            return "Soleado 🌞";
        }

        return "Indefinido 🌈";
    }
}
