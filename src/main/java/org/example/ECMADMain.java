package org.example;
import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import org.example.core.*;
import org.example.utils.*;

public class ECMADMain implements PlugInFilter {

    private ImagePlus imp;

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        try {
            IJ.showStatus("ECMAD: Starting deconvolution...");
            IJ.log("ECMAD: Preparing data...");

            boolean isColor = (ip instanceof ColorProcessor);
            ImageProcessor[] channels = new ImageProcessor[isColor ? 3 : 1];

            if (isColor) {
                // For color images, manually extract RGB channels
                ColorProcessor cp = (ColorProcessor) ip.duplicate();
                byte[] R = new byte[cp.getWidth() * cp.getHeight()];
                byte[] G = new byte[cp.getWidth() * cp.getHeight()];
                byte[] B = new byte[cp.getWidth() * cp.getHeight()];

                cp.getRGB(R, G, B);

                channels[0] = new ByteProcessor(cp.getWidth(), cp.getHeight(), R, null);
                channels[1] = new ByteProcessor(cp.getWidth(), cp.getHeight(), G, null);
                channels[2] = new ByteProcessor(cp.getWidth(), cp.getHeight(), B, null);

                IJ.log("ECMAD: RGB channels extracted");
            } else {
                // For grayscale, just use the original
                channels[0] = ip.duplicate();
            }

            int width = ip.getWidth();
            int height = ip.getHeight();

            IJ.log("ECMAD: Creating initial PSF...");
            float[][] initialPSF = PSFUtils.createGaussianPSF(width, height, 2.0f);

            WienerDeconvolver wiener = new WienerDeconvolver();
            TVRegularizer tv = new TVRegularizer();
            BlindDeconvolver blind = new BlindDeconvolver();
            EntropyController entropyCtrl = new EntropyController();

            IJ.log("ECMAD: Setting up processor...");
            ECMADProcessor processor = new ECMADProcessor(
                    wiener, tv, blind, entropyCtrl,
                    20, // max iterations
                    7.0f // entropy threshold
            );

            // Add progress listener
            processor.setProgressListener(new DeconvolutionProgressListener() {
                @Override
                public void onIterationStart(int iteration) {
                    IJ.showStatus("ECMAD: Iteration " + iteration);
                    IJ.log("ECMAD: Starting iteration " + iteration);
                }

                @Override
                public void onIterationComplete(int iteration, float error) {
                    IJ.log("ECMAD: Completed iteration " + iteration + " (error: " + error + ")");
                }
            });

            ImageProcessor[] resultChannels = new ImageProcessor[channels.length];

            // Process each channel
            for (int c = 0; c < channels.length; c++) {
                IJ.log("ECMAD: Processing channel " + (c+1) + " of " + channels.length);

                // Convert channel to float array
                float[][] inputChannel = ImageUtils.toFloatArray(channels[c]);

                // Process the channel
                IJ.log("ECMAD: Beginning deconvolution process for channel " + (c+1) + "...");
                float[][] resultChannel = processor.process(inputChannel, initialPSF);

                // Normalize and convert back to ImageProcessor
                ImageUtils.normalize(resultChannel);
                resultChannels[c] = ImageUtils.toImageProcessor(resultChannel);
            }

            ImageProcessor output;

            if (isColor) {
                // For color images, merge the RGB channels
                IJ.log("ECMAD: Merging RGB channels...");
                byte[] R = (byte[])resultChannels[0].convertToByte(false).getPixels();
                byte[] G = (byte[])resultChannels[1].convertToByte(false).getPixels();
                byte[] B = (byte[])resultChannels[2].convertToByte(false).getPixels();

                ColorProcessor colorOutput = new ColorProcessor(width, height);
                colorOutput.setRGB(R, G, B);
                output = colorOutput;
            } else {
                // For grayscale, just use the processed channel
                output = resultChannels[0];
            }

            // Display the result
            new ImagePlus("ECMAD Result", output).show();

            IJ.log("ECMAD: Process completed successfully.");
            IJ.showStatus("ECMAD: Done");

        } catch (Exception e) {
            IJ.log("🚨 Exception in ECMAD Plugin:");
            IJ.log(e.toString());

            // Log the stack trace to ImageJ log window
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                IJ.log("    at " + element.toString());
            }

            IJ.showMessage("Error", "An error occurred:\n" + e.getMessage());
        } finally {
            // Ensure the original image is unlocked if it was locked
            if (imp != null && imp.isLocked()) {
                IJ.log("ECMAD: Unlocking the original image");
                imp.unlock();
            }
        }
    }
}