package org.example.utils;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class ImageUtils {

    public static float[][] toFloatArray(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        float[][] array = new float[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                array[y][x] = ip.getPixelValue(x, y) / 255f;
            }
        }

        return array;
    }

    public static ImageProcessor toImageProcessor(float[][] array) {
        int height = array.length;
        int width = array[0].length;
        FloatProcessor fp = new FloatProcessor(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float val = Math.max(0, Math.min(1, array[y][x]));
                fp.setf(x, y, val * 255);
            }
        }

        return fp;
    }

    public static void normalize(float[][] array) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        for (float[] row : array) {
            for (float val : row) {
                if (val < min) min = val;
                if (val > max) max = val;
            }
        }

        float range = max - min;
        if (range == 0) return;

        for (int y = 0; y < array.length; y++) {
            for (int x = 0; x < array[0].length; x++) {
                array[y][x] = (array[y][x] - min) / range;
            }
        }
    }
}
