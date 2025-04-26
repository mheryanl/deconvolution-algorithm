package org.example.algorithms;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import org.jtransforms.fft.FloatFFT_2D;

public class BlindDeconvolution2 implements PlugInFilter {
    private int psfSize = 5;
    private int numIter = 10;

    public int setup(String arg, ImagePlus imp) {
        return DOES_RGB;
    }

    public void run(ImageProcessor ip) {
        if (!showDialog()) return;

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

        float[] psf = new float[psfSize * psfSize];
        for (int i = 0; i < psf.length; i++) psf[i] = 1f / psf.length;

        float[] latent = gray.clone();
        for (int it = 0; it < numIter; it++) {
            float[] conv = convolveFFT(latent, psf, width, height, psfSize);
            float[] ratio = new float[gray.length];
            for (int i = 0; i < gray.length; i++) ratio[i] = gray[i] / (conv[i] + 1e-6f);
            float[] update = convolveFFT(ratio, psf, width, height, psfSize);
            for (int i = 0; i < latent.length; i++) {
                latent[i] *= update[i];
                latent[i] = Math.min(1f, Math.max(0f, latent[i]));
            }
        }

        byte[] result = new byte[latent.length];
        for (int i = 0; i < result.length; i++) result[i] = (byte) (latent[i] * 255);

        ColorProcessor cp = new ColorProcessor(width, height);
        cp.setPixels(result);
        new ImagePlus("Deconvolved FFT", cp).show();
    }

    private boolean showDialog() {
        GenericDialog gd = new GenericDialog("Blind Deconvolution - FFT");
        gd.addNumericField("PSF Size", psfSize, 0);
        gd.addNumericField("Iterations", numIter, 0);
        gd.showDialog();
        if (gd.wasCanceled()) return false;
        psfSize = (int) gd.getNextNumber();
        numIter = (int) gd.getNextNumber();
        return true;
    }

    private float[] convolveFFT(float[] image, float[] kernel, int width, int height, int kernelSize) {
        float[][] image2D = new float[height][2 * width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image2D[y][2 * x] = image[y * width + x];
                image2D[y][2 * x + 1] = 0;
            }
        }

        float[][] kernel2D = new float[height][2 * width];
        int k = kernelSize / 2;
        for (int j = 0; j < kernelSize; j++) {
            for (int i = 0; i < kernelSize; i++) {
                int x = (i - k + width) % width;
                int y = (j - k + height) % height;
                kernel2D[y][2 * x] = kernel[j * kernelSize + i];
                kernel2D[y][2 * x + 1] = 0;
            }
        }

        FloatFFT_2D fft = new FloatFFT_2D(height, width);
        fft.complexForward(image2D);
        fft.complexForward(kernel2D);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int re = 2 * x;
                int im = 2 * x + 1;
                float a = image2D[y][re];
                float b = image2D[y][im];
                float c = kernel2D[y][re];
                float d = kernel2D[y][im];
                image2D[y][re] = a * c - b * d;
                image2D[y][im] = a * d + b * c;
            }
        }

        fft.complexInverse(image2D, true);

        float[] result = new float[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result[y * width + x] = image2D[y][2 * x];
            }
        }

        return result;
    }
}
