/*
 * CHHNeuralNetworkNode.java
 *
 * Created on October 26, 2003, 4:30 PM
 */

package uchicago.src.sim.network;

import uchicago.src.sim.network.*;
import java.util.*;

/**
 *
 * @author  Alson Kemp
 */
public class CHHNeuralNetworkNode extends NeuralNetworkNode {
    private NoisyLinearInhibitedActivationFunction OutputActivationFunction;
    private StepActivationFunction InhibitoryActivationFunction;
    private static final double ActivationThreshold = 0.5;
    /** Creates a new instance of CHHNeuralNetworkNode 
     * Patterned after the Cliff, Harvey, Husbands neural network nodes
     */
    public CHHNeuralNetworkNode() {
        super();
        
        // Input =(0,2) Input2 = (0,0) Output = (0,1) Noise = 0.1
        OutputActivationFunction = 
            new NoisyLinearInhibitedActivationFunction(0.0, 2.0, 0.1, 0.1, 0.0, 1.0, 0.1);
        // Input = (0.1,0.1) Output = (0,1) Noise = 0
        InhibitoryActivationFunction = 
            new StepActivationFunction(0.75, 0.75, 0.0, 1.0, 0.0);
    }

    public void collectInputs() {
        double temp;
        super.collectInputs();

        learning *= 0.999;
        
        if ( (OutputActivationFunction.calculateActivationFunction(SumOfInputs, SumOfInhibitoryInputs) > ActivationThreshold) || 
            (InhibitoryActivationFunction.calculateActivationFunction(SumOfInputs)> 0.5) ) {
            if (OutputActivationFunction.calculateActivationFunction(SumOfInputs, SumOfInhibitoryInputs) > ActivationThreshold)
                learning *= 1.05;
            if (InhibitoryActivationFunction.calculateActivationFunction(SumOfInputs) > 0.5)
                learning /= 1.05;
            
            java.util.ArrayList a = getInEdges();
            if (a != null) {
                Iterator i = a.iterator();
                while (i.hasNext()) {
                    DefaultEdge e = (DefaultEdge)i.next();
                    NeuralNetworkNode n = (NeuralNetworkNode)e.from;
                    if (NeuralNetworkNode.getWeightLearning())
                        e.setStrength(e.strength * learning * (1.0 + e.strength * n.getOutput() * 0.01));
                }
            }
            learning = 1.0;
        }
    }

    public void calculateOutputs() {
        Output = OutputActivationFunction.calculateActivationFunction(SumOfInputs, SumOfInhibitoryInputs);
        InhibitoryOutput = InhibitoryActivationFunction.calculateActivationFunction(SumOfInputs);
    }
    
    public double getInhibitoryOutput() {
        return InhibitoryOutput;
    }
    
    public double getOutput() {
        return Output;
    }
    
    public static double getActivationThreshold(){
        return ActivationThreshold;
    }
    
}
