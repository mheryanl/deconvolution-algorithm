package org.lmh.deconvolution;

import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;

public class DeconvolutionBlindPlugin implements PlugInFilter {

    private ImagePlus imp;

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        int psfSize = 5;
        int numIter = 10;
        String[] algorithms = {
                "Blind Deconvolution 1",
                "Blind Deconvolution 2",
                "Blind Deconvolution 3",
        };

        GenericDialog gd = new GenericDialog("Plugin Parameters");
        gd.addRadioButtonGroup("Deconvolution Algorithm", algorithms, 1, 3, algorithms[0]);
        gd.addNumericField("PSF Size", psfSize, 0);
        gd.addNumericField("Iterations", numIter, 0);
        gd.showDialog();
    }
}