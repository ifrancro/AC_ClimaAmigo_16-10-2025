package com.example.climaamigo;

import android.content.Context;
import android.media.Image;

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

    public ClimateAnalyzer(Context context) {
        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
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
                .addOnFailureListener(Throwable::printStackTrace)
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private String interpretLabels(List<ImageLabel> labels) {
        for (ImageLabel label : labels) {
            String text = label.getText().toLowerCase();
            if (text.contains("sky") || text.contains("sun")) return "Soleado 🌞";
            if (text.contains("cloud")) return "Nublado ☁️";
            if (text.contains("snow") || text.contains("ice")) return "Frío ❄️";
        }
        return "Indefinido 🌈";
    }
}
