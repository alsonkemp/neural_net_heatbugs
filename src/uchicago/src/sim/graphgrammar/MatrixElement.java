/*
 * MatrixElement.java
 *
 * Created on December 11, 2002, 7:37 PM
 */

package uchicago.src.sim.graphgrammar;

/**
 *
 * @author  Administrator
 */
public class MatrixElement {
    
    int RuleTargetOffset;
    int Weight;
    int LastModified;
    
    /** Creates a new instance of MatrixElement */
    public MatrixElement() {
        RuleTargetOffset = Integer.MIN_VALUE;
        Weight = Integer.MIN_VALUE;
        LastModified = -1;
    }
    
    public java.lang.String toString() {
        java.lang.StringBuffer rs = new java.lang.StringBuffer();
        
        rs.append("[");
        if (RuleTargetOffset == Integer.MIN_VALUE)
            rs.append("- ,");
        else
            rs.append(RuleTargetOffset+",");
        if (Weight != Integer.MIN_VALUE)
            rs.append(Weight+","+LastModified+"]\t");
        else
            rs.append("- ,"+LastModified+"]\t");

        return rs.toString();
    }
    
    public void setRuleTargetOffset(int o){
        RuleTargetOffset = o;
    }

    public void setWeight(int o){
        Weight = o;
    }
    
    public int getWeight() {
        return Weight;
    }
    
    public void setLastModified(int o){
        LastModified = o;
    }
    
    public boolean isEmpty() {
        if ((Weight == Integer.MIN_VALUE) && (RuleTargetOffset == Integer.MIN_VALUE)) 
            return true;
        else
            return false;
    }
    
    public void initialize() {
        RuleTargetOffset = Integer.MIN_VALUE;
        Weight = Integer.MIN_VALUE;
        LastModified = -1;
    }
}
