package org.lmh.deconvolution;

import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import org.lmh.deconvolution.algorithms.BlindDeconvolution1;
import org.lmh.deconvolution.algorithms.BlindDeconvolution2;
import org.lmh.deconvolution.algorithms.BlindDeconvolution3;
import org.lmh.deconvolution.algorithms.DeconvolutionAlgorithm;

public class DeconvolutionBlindPlugin implements PlugInFilter {

    static final String algorithm1 = "Blind Deconvolution 1";
    static final String algorithm2 = "Blind Deconvolution 2";
    static final String algorithm3 = "Blind Deconvolution 3";

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_RGB;
    }

    @Override
    public void run(ImageProcessor ip) {
        DeconvolutionAlgorithm algorithm;
        int psfSize = 5;
        int numIter = 10;
        final String[] algorithms = {
                algorithm1, algorithm2, algorithm3
        };

        GenericDialog gd = new GenericDialog("Plugin Parameters");
        gd.addRadioButtonGroup("Deconvolution Algorithm", algorithms, 1, 3, algorithms[0]);
        gd.addNumericField("PSF Size", psfSize, 0);
        gd.addNumericField("Iterations", numIter, 0);
        gd.showDialog();
        if (gd.wasCanceled()) return;
        psfSize = (int) gd.getNextNumber();
        numIter = (int) gd.getNextNumber();
        algorithm = switch (gd.getNextRadioButton()) {
            case algorithm2 -> new BlindDeconvolution2(ip, psfSize, numIter);
            case algorithm3 -> new BlindDeconvolution3(ip, psfSize, numIter);
            default -> new BlindDeconvolution1(ip, psfSize, numIter);
        };
        algorithm.run();
    }
}