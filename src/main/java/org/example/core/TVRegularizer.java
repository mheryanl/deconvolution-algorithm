package org.example.core;

public class TVRegularizer {

    private float lambda = 0.125f;
    private float stepSize = 0.1f;
    private int iterations = 10;

    public void setLambda(float lambda) {
        this.lambda = lambda;
    }

    public void setStepSize(float stepSize) {
        this.stepSize = stepSize;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public float[][] denoise(float[][] image) {
        int height = image.length;
        int width = image[0].length;
        float[][] result = deepCopy(image);

        for (int iter = 0; iter < iterations; iter++) {
            float[][] gradX = new float[height][width];
            float[][] gradY = new float[height][width];
            float[][] div = new float[height][width];

            // Compute gradients
            for (int i = 0; i < height - 1; i++) {
                for (int j = 0; j < width - 1; j++) {
                    gradX[i][j] = result[i][j + 1] - result[i][j];
                    gradY[i][j] = result[i + 1][j] - result[i][j];
                }
            }

            // Compute divergence
            for (int i = 1; i < height - 1; i++) {
                for (int j = 1; j < width - 1; j++) {
                    float dx = gradX[i][j] - gradX[i][j - 1];
                    float dy = gradY[i][j] - gradY[i - 1][j];
                    div[i][j] = dx + dy;
                }
            }

            // Update image
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    result[i][j] = result[i][j] + stepSize * lambda * div[i][j];
                }
            }
        }

        return result;
    }

    private float[][] deepCopy(float[][] input) {
        int height = input.length;
        int width = input[0].length;
        float[][] copy = new float[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(input[i], 0, copy[i], 0, width);
        }
        return copy;
    }
}
