package org.example.core;

import java.util.HashMap;
import java.util.Map;

public class EntropyController {

    private float lastEntropy = -1;
    private float convergenceThreshold = 0.001f;
    private float wienerWeight = 1.0f;
    private float tvWeight = 1.0f;

    public float computeEntropy(float[][] image) {
        Map<Integer, Integer> histogram = new HashMap<>();
        int totalPixels = image.length * image[0].length;

        for (float[] row : image) {
            for (float pixel : row) {
                int val = (int) (pixel * 255);
                histogram.put(val, histogram.getOrDefault(val, 0) + 1);
            }
        }

        float entropy = 0;
        for (int count : histogram.values()) {
            float p = (float) count / totalPixels;
            if (p > 0) {
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }

        return entropy;
    }

    public boolean hasConverged() {
        return lastEntropy > 0 && Math.abs(lastEntropy - getCurrentEntropy()) < convergenceThreshold;
    }

    public void updateWeights(float entropy) {
        this.lastEntropy = entropy;
    }

    public void decreaseWienerWeight() {
        wienerWeight = Math.max(0.5f, wienerWeight - 0.1f);
    }

    public void increaseWienerWeight() {
        wienerWeight = Math.min(2.0f, wienerWeight + 0.1f);
    }

    public void increaseTVWeight() {
        tvWeight = Math.min(2.0f, tvWeight + 0.1f);
    }

    public void resetTVWeight() {
        tvWeight = 1.0f;
    }

    public float getWienerWeight() {
        return wienerWeight;
    }

    public float getTVWeight() {
        return tvWeight;
    }

    private float getCurrentEntropy() {
        return lastEntropy;
    }
}
