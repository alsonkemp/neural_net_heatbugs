/*
 * NeuralNetwork.java
 *
 * Created on October 25, 2003, 5:24 PM
 */

package uchicago.src.sim.network;
import java.util.*;
/**
 *
 * @author  Alson Kemp
 */
public abstract class  NeuralNetworkNode extends DefaultNodeWithInhibitors {
    protected double ExternalInputValue;
    protected double SumOfInputs, SumOfInhibitoryInputs;
    protected double Output, InhibitoryOutput;
    protected double learning;
    protected double bias;
    protected static boolean WeightLearning = true;
    
    /** Creates a new instance of NeuralNetwork */
    public NeuralNetworkNode() {
        super();
        ExternalInputValue = 0.0;
        learning = 1.0;
        bias = 0.0;
    }
    
    public double getLearning(){
        return learning;
    }
    
    public void collectInputs() {
        SumOfInputs = ExternalInputValue + bias;
        SumOfInhibitoryInputs = 0.0;
        
        //collect non-inhibiting inputs
        ArrayList a = getInEdges();
        if (a != null) {
            for (int i = 0; i < a.size(); i++) {
                DefaultEdge e = (DefaultEdge)a.get(i);
                NeuralNetworkNode n = (NeuralNetworkNode)e.from;
                SumOfInputs += e.getStrength() * n.getOutput();
            }
        }
        
        //collect inhibiting inputs
        a = getInInhibitoryEdges();
        if (a != null) {
            for (int i = 0; i < a.size(); i++) {
                DefaultEdge e = (DefaultEdge)a.get(i);
                NeuralNetworkNode n = (NeuralNetworkNode)e.from;
                SumOfInhibitoryInputs += e.getStrength() * n.getInhibitoryOutput();
            }
        }
    }
    
    public abstract void calculateOutputs();
    
    public abstract double getOutput();
    
    public abstract double getInhibitoryOutput();
    
    public double getExternalInput() {
        return ExternalInputValue;
    }
    
    public void setBias(double val) {
        bias = val;
    }
    
    public double getBias() {
        return bias;
    }
    
    public static void setWeightLearning (boolean b) {
        WeightLearning = b;
    }
    
    public static boolean getWeightLearning(){
        return WeightLearning;
    }
    
    public void setExternalInput(double val) {
        ExternalInputValue = val;
    }
}
