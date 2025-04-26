package org.lmh.deconvolution.algorithms;

import ij.process.ImageProcessor;

public abstract class DeconvolutionAlgorithm {
    ImageProcessor ip;
    int psfSize;
    int iterations;
    public abstract void run();
}
