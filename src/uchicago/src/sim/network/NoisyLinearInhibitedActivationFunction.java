/*
 * NoisyLinearInhibitedActivationFunction.java
 *
 * Created on October 26, 2003, 4:04 PM
 */

package uchicago.src.sim.network;

import uchicago.src.sim.util.Random;
import uchicago.src.sim.network.*;


/**
 *
 * @author  Alson Kemp
 */
public class NoisyLinearInhibitedActivationFunction extends DoubleActivationFunction {
    private double Slope;
    
    /** Creates a new instance of NoisyLinearInhibitedActivationFunction */
    public NoisyLinearInhibitedActivationFunction(double a, double b, double c, double d, double e, double f, double g) {
        super(a,b,c,d,e,f,g);
        setupActivationFunction();
    }
    
    public NoisyLinearInhibitedActivationFunction() {
        super();
        setupActivationFunction();
    }
    
    public double calculateActivationFunction(double in, double inhibit) {
        in += Random.uniform.nextDoubleFromTo(-Noise, Noise);

        if (inhibit > MinimumInput2)
            return 0.0;
        else if (in < MinimumInput) 
            return MinimumOutput;
        else if (in > MaximumInput)
            return MaximumOutput;
        else
            return ((in - MinimumInput) * Slope);
    }

    public double calculateActivationFunction(double in) {
        System.err.println("NoisyLinearInhibitedActivationFunction:calculateActivationFunction called with only one input.  Assuming second input is 999.");
        return calculateActivationFunction(in, 999.0);
    }

    
    public void setupActivationFunction() {
        Slope = (MaximumOutput - MinimumOutput)/(MaximumInput - MinimumInput);
    }
    
   
}