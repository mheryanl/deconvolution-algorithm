package org.example.core;

import org.example.utils.FFTUtils.*;

import static org.example.utils.FFTUtils.fft2D;
import static org.example.utils.FFTUtils.ifft2D;

public class WienerDeconvolver {
    private float noiseToSignalRatio = 0.01f;

    public void setNoiseToSignalRatio(float ratio) {
        this.noiseToSignalRatio = ratio;
    }

    public float[][] deconvolve(float[][] image, float[][] psf) {
        int width = image[0].length;
        int height = image.length;

        // Forward FFT
        Complex[][] imageFFT = fft2D(image);
        Complex[][] psfFFT = fft2D(psf);

        Complex[][] resultFFT = new Complex[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Complex H = psfFFT[i][j];
                Complex H_conj = H.conjugate();
                Complex numerator = imageFFT[i][j].multiply(H_conj);
                double denom = H.magnitudeSquared() + noiseToSignalRatio;
                resultFFT[i][j] = numerator.divide(denom);
            }
        }

        // Inverse FFT
        return ifft2D(resultFFT);
    }
}
