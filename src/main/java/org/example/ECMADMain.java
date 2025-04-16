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

            // Convert image to float array
            float[][] inputImage = ImageUtils.toFloatArray(ip);
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

            IJ.log("ECMAD: Beginning deconvolution process...");
            float[][] result = processor.process(inputImage, initialPSF);

            IJ.log("ECMAD: Normalizing and displaying result...");
            ImageUtils.normalize(result);
            ImageProcessor output = ImageUtils.toImageProcessor(result);
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
        }
    }
}