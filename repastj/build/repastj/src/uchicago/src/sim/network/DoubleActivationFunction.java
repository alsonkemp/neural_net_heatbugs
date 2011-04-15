/*
 * DoubleActivationFunction.java
 *
 * Created on October 26, 2003, 3:58 PM
 */

package uchicago.src.sim.network;

/**
 *
 * @author  Alson Kemp
 */
public abstract class DoubleActivationFunction extends ActivationFunction {
    protected double MinimumInput2;
    protected double MaximumInput2;
    
    /** Creates a new instance of DoubleActivationFunction */
    public DoubleActivationFunction() {
        super();
        MinimumInput2 = 0;
        MaximumInput2 = 1;
        setupActivationFunction();
    }

    public DoubleActivationFunction(double a, double b, double c, double d, double e, double f, double g) {
        super(a,b,e,f,g);
        MinimumInput2 = c;
        MaximumInput2 = d;
        setupActivationFunction();
    }

    public abstract double calculateActivationFunction(double in, double inhibit);
}
