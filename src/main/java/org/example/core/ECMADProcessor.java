package org.example.core;

import ij.IJ;
import org.example.DeconvolutionProgressListener;

public class ECMADProcessor {
    private WienerDeconvolver wienerDeconvolver;
    private TVRegularizer tvRegularizer;
    private BlindDeconvolver blindDeconvolver;
    private EntropyController entropyController;
    private int maxIterations;
    private float entropyThreshold;
    private DeconvolutionProgressListener progressListener;

    public ECMADProcessor(
            WienerDeconvolver wienerDeconvolver,
            TVRegularizer tvRegularizer,
            BlindDeconvolver blindDeconvolver,
            EntropyController entropyController,
            int maxIterations,
            float entropyThreshold) {
        this.wienerDeconvolver = wienerDeconvolver;
        this.tvRegularizer = tvRegularizer;
        this.blindDeconvolver = blindDeconvolver;
        this.entropyController = entropyController;
        this.maxIterations = maxIterations;
        this.entropyThreshold = entropyThreshold;
    }

    public void setProgressListener(DeconvolutionProgressListener listener) {
        this.progressListener = listener;
    }

    public float[][] process(float[][] image, float[][] initialPSF) {
        // Make a defensive copy of the input image
        float[][] currentEstimate = copyArray(image);
        float[][] currentPSF = copyArray(initialPSF);
        float[][] previousEstimate = null;

        IJ.log("ECMAD: Image dimensions: " + image.length + "x" + image[0].length);
        IJ.log("ECMAD: PSF dimensions: " + initialPSF.length + "x" + initialPSF[0].length);

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            try {
                if (progressListener != null) {
                    progressListener.onIterationStart(iteration);
                } else {
                    IJ.log("Iteration: " + iteration);
                }

                // Store previous estimate for convergence check
                previousEstimate = copyArray(currentEstimate);

                // Step 1: Wiener Deconvolution
                IJ.log("ECMAD: Performing Wiener deconvolution...");
                currentEstimate = wienerDeconvolver.deconvolve(image, currentPSF);

                // Step 2: Total Variation Regularization - Apply TV using whatever method TVRegularizer provides
                IJ.log("ECMAD: Applying TV regularization...");
                // Use applyRegularization instead of regularize
                currentEstimate = applyTVRegularization(currentEstimate);

                // Step 3: Update PSF with Blind Deconvolution
                IJ.log("ECMAD: Updating PSF...");
                // Call updatePSF with just image and estimate, since your class doesn't accept currentPSF
                currentPSF = blindDeconvolver.updatePSF(image, currentEstimate);

                // Step 4: Check entropy convergence
                float entropy = entropyController.computeEntropy(currentEstimate); // Use computeEntropy instead of calculateEntropy
                entropyController.updateWeights(entropy); // Update weights based on entropy

                float error = calculateError(previousEstimate, currentEstimate);

                IJ.log("ECMAD: Current entropy: " + entropy + ", Error: " + error);

                if (progressListener != null) {
                    progressListener.onIterationComplete(iteration, error);
                }

                // Check convergence
                if (entropy < entropyThreshold || error < 0.001 || entropyController.hasConverged()) {
                    IJ.log("ECMAD: Convergence reached at iteration " + iteration);
                    break;
                }

                // Adjust weights based on results
                adjustWeights(entropy, iteration);

            } catch (Exception e) {
                IJ.log("🚨 Error in ECMAD iteration " + iteration + ": " + e.getMessage());
                e.printStackTrace();
                // Continue with next iteration instead of aborting
            }
        }

        return currentEstimate;
    }

    private float[][] applyTVRegularization(float[][] image) {
        // Implement TV regularization using your TVRegularizer class
        // Since we don't know the exact method signature, we'll have to adapt

        // This is a placeholder - you'll need to replace with actual calls to your TVRegularizer
        // For example, if TVRegularizer has a method like "applyTV" or similar:
        // return tvRegularizer.applyTV(image, entropyController.getTVWeight());

        // Simplified approach: just return the image if we don't know how to call TVRegularizer
        IJ.log("Warning: TV regularization not applied - method not found");
        return image;
    }

    private void adjustWeights(float entropy, int iteration) {
        // Automatically adjust weights based on entropy trends
        // This is just an example - you may want to implement your own logic
        if (iteration > 0) {
            if (entropy > entropyThreshold * 1.2) {
                entropyController.increaseTVWeight();
                entropyController.decreaseWienerWeight();
                IJ.log("ECMAD: Increasing TV weight to " + entropyController.getTVWeight());
            } else if (entropy < entropyThreshold * 0.8) {
                entropyController.resetTVWeight();
                entropyController.increaseWienerWeight();
                IJ.log("ECMAD: Resetting TV weight and increasing Wiener weight");
            }
        }
    }

    private float[][] copyArray(float[][] source) {
        if (source == null) return null;
        float[][] copy = new float[source.length][source[0].length];
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, copy[i], 0, source[i].length);
        }
        return copy;
    }

    private float calculateError(float[][] previous, float[][] current) {
        if (previous == null) return Float.MAX_VALUE;

        float sumSquaredDiff = 0;
        float sumSquared = 0;

        for (int i = 0; i < previous.length; i++) {
            for (int j = 0; j < previous[0].length; j++) {
                float diff = previous[i][j] - current[i][j];
                sumSquaredDiff += diff * diff;
                sumSquared += previous[i][j] * previous[i][j];
            }
        }

        // Prevent division by zero
        if (sumSquared < 0.000001f) {
            return sumSquaredDiff;
        }

        return (float) Math.sqrt(sumSquaredDiff / sumSquared);
    }
}