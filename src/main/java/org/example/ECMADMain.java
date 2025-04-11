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
            // Convert image to float array
            float[][] inputImage = ImageUtils.toFloatArray(ip);
            int width = ip.getWidth();
            int height = ip.getHeight();

            float[][] initialPSF = PSFUtils.createGaussianPSF(width, height, 2.0f);

            WienerDeconvolver wiener = new WienerDeconvolver();
            TVRegularizer tv = new TVRegularizer();
            BlindDeconvolver blind = new BlindDeconvolver();
            EntropyController entropyCtrl = new EntropyController();

            ECMADProcessor processor = new ECMADProcessor(
                    wiener, tv, blind, entropyCtrl,
                    20, // max iterations
                    7.0f // entropy threshold
            );

            float[][] result = processor.process(inputImage, initialPSF);

            ImageUtils.normalize(result);
            ImageProcessor output = ImageUtils.toImageProcessor(result);
            new ImagePlus("ECMAD Result", output).show();
        } catch (Exception e) {
            IJ.log("🚨 Exception in ECMAD Plugin:");
            IJ.log(e.toString());
            e.printStackTrace();
            IJ.showMessage("Error", "An error occurred:\n" + e.getMessage());
        }
    }
}