package org.example.core;

public class ECMADProcessor {

    private final WienerDeconvolver wiener;
    private final TVRegularizer tv;
    private final BlindDeconvolver blind;
    private final EntropyController entropyCtrl;

    private final int maxIterations;
    private final float entropyThreshold;

    public ECMADProcessor(WienerDeconvolver wiener,
                          TVRegularizer tv,
                          BlindDeconvolver blind,
                          EntropyController entropyCtrl,
                          int maxIterations,
                          float entropyThreshold) {
        this.wiener = wiener;
        this.tv = tv;
        this.blind = blind;
        this.entropyCtrl = entropyCtrl;
        this.maxIterations = maxIterations;
        this.entropyThreshold = entropyThreshold;
    }

    public float[][] process(float[][] image, float[][] psf) {
        float[][] result = image;
        float[][] currentPSF = psf;

        for (int iter = 0; iter < maxIterations; iter++) {
            System.out.println("Iteration: " + iter);

            // Wiener Deconvolution
            result = wiener.deconvolve(result, currentPSF);

            // TV Regularization
            result = tv.denoise(result);

            // Blind PSF update
            currentPSF = blind.updatePSF(result, currentPSF);

            // Entropy-based adaptive control
            float entropy = entropyCtrl.computeEntropy(result);
            System.out.println("Entropy: " + entropy);

            if (entropy > entropyThreshold) {
                System.out.println("High entropy detected. Adjusting weights to suppress noise.");
                entropyCtrl.decreaseWienerWeight();
                entropyCtrl.increaseTVWeight();
            } else {
                System.out.println("Entropy acceptable. Emphasizing sharpness.");
                entropyCtrl.increaseWienerWeight();
                entropyCtrl.resetTVWeight();
            }

            // Optional: break early if entropy stabilizes
            if (entropyCtrl.hasConverged()) {
                System.out.println("Converged early at iteration " + iter);
                break;
            }
        }

        return result;
    }
}
