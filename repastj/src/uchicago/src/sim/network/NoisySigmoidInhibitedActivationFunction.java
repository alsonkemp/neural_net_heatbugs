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
public class NoisySigmoidInhibitedActivationFunction extends DoubleActivationFunction {
    private double Slope;
    private double MidPoint;
    /** Creates a new instance of NoisyLinearInhibitedActivationFunction */
    public NoisySigmoidInhibitedActivationFunction(double a, double b, double c, double d, double e, double f, double g) {
        super(a,b,c,d,e,f,g);
        setupActivationFunction();
    }
    
    public NoisySigmoidInhibitedActivationFunction() {
        super();
        setupActivationFunction();
    }
    
    public double calculateActivationFunction(double in, double inhibit) {
        in += Random.uniform.nextDoubleFromTo(-Noise, Noise);

        if (inhibit > MinimumInput2)
            return 0.0;
        else
            return MinimumOutput + (MaximumOutput - MinimumOutput)/(1.0 + Math.exp(-(in-MidPoint)*Slope));
    }

    public double calculateActivationFunction(double in) {
        System.err.println("NoisySigmoidInhibitedActivationFunction:calculateActivationFunction called with only one input.  Assuming second input is 999.");
        return calculateActivationFunction(in, 0.0);
    }
    
    public void setupActivationFunction() {
        Slope = 1.0;
        MidPoint = (MaximumInput + MinimumInput)/2.0;
    }
}