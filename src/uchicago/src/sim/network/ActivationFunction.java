/*
 * ActivationFunction.java
 *
 * Created on October 25, 2003, 6:26 PM
 */

package uchicago.src.sim.network;

/**
 *
 * @author  Alson Kemp
 */
public abstract class ActivationFunction {
    protected double MinimumInput;
    protected double MaximumInput;
    protected double MinimumOutput;
    protected double MaximumOutput;
    protected double Noise;
    
    public ActivationFunction(){
        MinimumInput  = 0.0;
        MaximumInput  = 1.0;
        MinimumOutput = 0.0;
        MaximumOutput = 1.0;
        Noise = 0.0;
        setupActivationFunction();
    }
    
    public ActivationFunction(double a, double b, double c, double d, double e){
        MinimumInput  = a;
        MaximumInput  = b;
        MinimumOutput = c;
        MaximumOutput = d;
        Noise = e;
        setupActivationFunction();
    }
    
    public abstract void setupActivationFunction();
    public abstract double calculateActivationFunction(double in);
    
    /* access methods
     * setters
     */
    public void setMinimumInput(double i) {
        MinimumInput = i;
        setupActivationFunction();
    }
    
    public void setMaximumInput(double i) {
        MaximumInput = i;
        setupActivationFunction();
    }
    
    public void setMinimumOutput(double i) {
        MinimumOutput = i;
        setupActivationFunction();
    }
    
    public void setMaximumOutput(double i) {
        MaximumOutput = i;
        setupActivationFunction();
    }
    
    /* getters
     */
    public double getMinimumInput() {
        return MinimumInput;
    }
    
    public double getMaximumInput() {
        return MaximumInput;
    }
    
    public double getMinimumOutput() {
        return MinimumOutput;
    }
    
    public double getMaximumOutput() {
        return MaximumOutput;
    }
    
}
