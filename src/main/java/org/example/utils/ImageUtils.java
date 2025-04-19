package org.example.utils;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class ImageUtils {

    /**
     * Converts an ImageProcessor to a 2D float array
     */
    public static float[][] toFloatArray(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();

        float[][] result = new float[height][width];
        FloatProcessor fp;

        // Convert to FloatProcessor if it's not already
        if (!(ip instanceof FloatProcessor)) {
            fp = (FloatProcessor) ip.convertToFloat();
        } else {
            fp = (FloatProcessor) ip;
        }

        // Copy data to the array
        float[] pixels = (float[]) fp.getPixels();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result[y][x] = pixels[y * width + x];
            }
        }

        return result;
    }

    /**
     * Converts a 2D float array to an ImageProcessor
     */
    public static ImageProcessor toImageProcessor(float[][] array) {
        int height = array.length;
        int width = array[0].length;

        FloatProcessor fp = new FloatProcessor(width, height);
        float[] pixels = (float[]) fp.getPixels();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = array[y][x];
            }
        }

        return fp;
    }

    /**
     * Normalizes a float array to the range [0,1]
     */
    public static void normalize(float[][] array) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        // Find min and max
        for (float[] row : array) {
            for (float val : row) {
                min = Math.min(min, val);
                max = Math.max(max, val);
            }
        }

        float range = max - min;
        if (range < 0.00001f) {
            // Avoid division by near-zero
            return;
        }

        // Normalize to [0,1]
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                array[i][j] = (array[i][j] - min) / range;
            }
        }
    }
}