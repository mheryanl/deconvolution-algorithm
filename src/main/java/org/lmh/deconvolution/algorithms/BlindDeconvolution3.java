package org.lmh.deconvolution.algorithms;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class BlindDeconvolution3 extends DeconvolutionAlgorithm {

    public BlindDeconvolution3(ImageProcessor ip, int psfSize, int iterations) {
        this.ip = ip;
        this.psfSize = psfSize;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        int width = ip.getWidth();
        int height = ip.getHeight();
        byte[] R = new byte[width * height];
        byte[] G = new byte[width * height];
        byte[] B = new byte[width * height];
        ((ColorProcessor) ip).getRGB(R, G, B);

        float[] gray = new float[width * height];
        for (int i = 0; i < gray.length; i++) {
            gray[i] = ((R[i] & 0xff) + (G[i] & 0xff) + (B[i] & 0xff)) / 3f / 255f;
        }

        float[] psf1D = new float[psfSize];
        for (int i = 0; i < psfSize; i++) psf1D[i] = 1f / psfSize;

        float[] latent = gray.clone();
        for (int it = 0; it < iterations; it++) {
            float[] conv = convolveSeparable(latent, psf1D, width, height);
            float[] ratio = new float[gray.length];
            for (int i = 0; i < gray.length; i++) ratio[i] = gray[i] / (conv[i] + 1e-6f);
            float[] update = convolveSeparable(ratio, psf1D, width, height);
            for (int i = 0; i < latent.length; i++) {
                latent[i] *= update[i];
                latent[i] = Math.min(1f, Math.max(0f, latent[i]));
            }
        }

        byte[] result = new byte[latent.length];
        for (int i = 0; i < result.length; i++) result[i] = (byte) (latent[i] * 255);

        ColorProcessor cp = new ColorProcessor(width, height);
        cp.setPixels(result);
        new ImagePlus("Deconvolved Separable", cp).show();
    }

    private float[] convolveSeparable(float[] image, float[] kernel1D, int width, int height) {
        int k = kernel1D.length / 2;
        float[] temp = new float[image.length];
        float[] result = new float[image.length];

        // Horizontal convolution
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float sum = 0;
                for (int i = -k; i <= k; i++) {
                    int xi = Math.min(width - 1, Math.max(0, x + i));
                    sum += image[y * width + xi] * kernel1D[i + k];
                }
                temp[y * width + x] = sum;
            }
        }

        // Vertical convolution
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float sum = 0;
                for (int j = -k; j <= k; j++) {
                    int yj = Math.min(height - 1, Math.max(0, y + j));
                    sum += temp[yj * width + x] * kernel1D[j + k];
                }
                result[y * width + x] = sum;
            }
        }

        return result;
    }
}
