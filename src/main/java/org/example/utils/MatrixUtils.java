package org.example.utils;

public class MatrixUtils {

    public static float[][] deepCopy(float[][] input) {
        int height = input.length;
        int width = input[0].length;
        float[][] copy = new float[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(input[i], 0, copy[i], 0, width);
        }
        return copy;
    }

    public static float[][] subtract(float[][] a, float[][] b) {
        int height = a.length;
        int width = a[0].length;
        float[][] result = new float[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result[i][j] = a[i][j] - b[i][j];
            }
        }
        return result;
    }

    public static float[][] convolve(float[][] image, float[][] kernel) {
        int ih = image.length, iw = image[0].length;
        int kh = kernel.length, kw = kernel[0].length;
        int padH = kh / 2, padW = kw / 2;
        float[][] result = new float[ih][iw];

        for (int i = 0; i < ih; i++) {
            for (int j = 0; j < iw; j++) {
                float sum = 0;
                for (int ki = 0; ki < kh; ki++) {
                    for (int kj = 0; kj < kw; kj++) {
                        int ii = i + ki - padH;
                        int jj = j + kj - padW;
                        if (ii >= 0 && ii < ih && jj >= 0 && jj < iw) {
                            sum += image[ii][jj] * kernel[ki][kj];
                        }
                    }
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    public static float[][] convolveTranspose(float[][] image, float[][] kernel) {
        int kh = kernel.length;
        int kw = kernel[0].length;
        float[][] flipped = new float[kh][kw];

        for (int i = 0; i < kh; i++) {
            for (int j = 0; j < kw; j++) {
                flipped[i][j] = kernel[kh - 1 - i][kw - 1 - j];
            }
        }

        return convolve(image, flipped);
    }

    public static void normalize(float[][] matrix) {
        float sum = 0;
        for (float[] row : matrix) {
            for (float val : row) {
                sum += val;
            }
        }
        if (sum == 0) return;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] /= sum;
            }
        }
    }
}
