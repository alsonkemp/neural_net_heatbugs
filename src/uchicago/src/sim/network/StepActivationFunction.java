package uchicago.src.sim.network;

import uchicago.src.sim.util.Random;
import uchicago.src.sim.network.*;


/**
 *
 * @author  Alson Kemp
 */
public class StepActivationFunction extends ActivationFunction {
    /** Creates a new instance of StepActivationFunction */
    public StepActivationFunction(double a, double b, double c, double d, double e) {
        super(a,b,c,d,e);
        setupActivationFunction();
    }
    
    public StepActivationFunction() {
        super();
        setupActivationFunction();
    }
    
    public double calculateActivationFunction(double in) {
        in += Random.uniform.nextDoubleFromTo(-Noise, Noise);

        if (in < MinimumInput) 
            return MinimumOutput;
        else
            return MaximumOutput;
    }

    public void setupActivationFunction() { }
}
