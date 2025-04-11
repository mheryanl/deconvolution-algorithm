package org.example.utils;

public class PSFUtils {

    public static float[][] createGaussianPSF(int width, int height, float sigma) {
        float[][] psf = new float[height][width];
        int cx = width / 2;
        int cy = height / 2;
        float sum = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float val = (float) Math.exp(-(dx * dx + dy * dy) / (2 * sigma * sigma));
                psf[y][x] = val;
                sum += val;
            }
        }

        // Normalize
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                psf[y][x] /= sum;
            }
        }

        return psf;
    }

    public static float[][] createDeltaPSF(int width, int height) {
        float[][] psf = new float[height][width];
        psf[height / 2][width / 2] = 1.0f;
        return psf;
    }

    public static void normalize(float[][] psf) {
        float sum = 0f;
        for (int y = 0; y < psf.length; y++) {
            for (int x = 0; x < psf[0].length; x++) {
                sum += psf[y][x];
            }
        }
        if (sum == 0) return;
        for (int y = 0; y < psf.length; y++) {
            for (int x = 0; x < psf[0].length; x++) {
                psf[y][x] /= sum;
            }
        }
    }
}
