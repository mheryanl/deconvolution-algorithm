package org.example.core;

import org.example.utils.MatrixUtils;

public class BlindDeconvolver {

    private float learningRate = 0.01f;
    private int iterations = 5;

    public void setLearningRate(float lr) {
        this.learningRate = lr;
    }

    public void setIterations(int iters) {
        this.iterations = iters;
    }

    public float[][] updatePSF(float[][] image, float[][] psf) {
        int height = psf.length;
        int width = psf[0].length;
        float[][] updatedPSF = MatrixUtils.deepCopy(psf);

        for (int iter = 0; iter < iterations; iter++) {
            float[][] estimatedBlurred = MatrixUtils.convolve(image, updatedPSF);
            float[][] error = MatrixUtils.subtract(estimatedBlurred, image);
            float[][] gradient = MatrixUtils.convolveTranspose(image, error);

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    updatedPSF[i][j] -= learningRate * gradient[i][j];
                    updatedPSF[i][j] = Math.max(updatedPSF[i][j], 0); // keep non-negative
                }
            }

            MatrixUtils.normalize(updatedPSF); // ensure PSF sums to 1
        }

        return updatedPSF;
    }
}
