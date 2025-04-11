package org.example.utils;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

public class FFTUtils {

    public static Complex[][] fft2D(float[][] input) {
        int height = input.length;
        int width = input[0].length;
        FloatFFT_2D fft = new FloatFFT_2D(height, width);

        float[][] data = new float[height][2 * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                data[i][2 * j] = input[i][j];
                data[i][2 * j + 1] = 0;
            }
        }

        fft.complexForward(data);

        Complex[][] output = new Complex[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                float real = data[i][2 * j];
                float imag = data[i][2 * j + 1];
                output[i][j] = new Complex(real, imag);
            }
        }

        return output;
    }

    public static float[][] ifft2D(Complex[][] input) {
        int height = input.length;
        int width = input[0].length;
        FloatFFT_2D fft = new FloatFFT_2D(height, width);

        float[][] data = new float[height][2 * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                data[i][2 * j] = input[i][j].real;
                data[i][2 * j + 1] = input[i][j].imag;
            }
        }

        fft.complexInverse(data, true);

        float[][] output = new float[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                output[i][j] = data[i][2 * j];
            }
        }

        return output;
    }

    public static class Complex {
        public float real;
        public float imag;

        public Complex(float real, float imag) {
            this.real = real;
            this.imag = imag;
        }

        public Complex conjugate() {
            return new Complex(real, -imag);
        }

        public Complex multiply(Complex other) {
            return new Complex(
                    real * other.real - imag * other.imag,
                    real * other.imag + imag * other.real
            );
        }

        public Complex divide(double scalar) {
            return new Complex((float)(real / scalar), (float)(imag / scalar));
        }

        public double magnitudeSquared() {
            return real * real + imag * imag;
        }
    }
}

