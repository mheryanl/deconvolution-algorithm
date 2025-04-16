package org.example;

public interface DeconvolutionProgressListener {
    void onIterationStart(int iteration);
    void onIterationComplete(int iteration, float error);
}