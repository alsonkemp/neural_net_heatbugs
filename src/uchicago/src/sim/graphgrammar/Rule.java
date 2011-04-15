/*
 * Rule.java
 *
 * Created on January 11, 2002, 4:38 PM
 */

/**
 *
 * @author  Alson Kemp
 * @version 
 */
package uchicago.src.sim.graphgrammar;

public class Rule extends java.util.ArrayList {
/*
 * Rule format in binary, in order of segmentation
 * [RuleType] 1 bit (if no segment, default is 0)
 * [NodeType] 2 bits, required only if (RuleType=RuleType_NewNode)
 * [ConnectionRedirect] 1 bit
 * [ConnectionDepth] 1 bit
 * [NodeSrcName] variable length; required to be >0 bits
 * [NodeDestName] variable length; required to be >0 bits
 * [NodeNewSrcName]
 */
    /*
     * MinRuleLength is the minimum possible length (in bits) of a valid rule.
     * Includes segment "stop" bits
     * =   1 MatrixType bit			= 1
     *   + 2 CellType bits    			= 2
     *						= 3
     */
    public static final int MinRuleLength = 3;
    
    public static final int MatrixType_Expansion  = 0;
    public static final int MatrixType_Adjustment = 1;
    
    public static final int CellType_LastCell    = 0;
    public static final int CellType_RuleTarget  = 1;
    public static final int CellType_RuleTarget2 = -3;
    public static final int CellType_Weight      = 2;
    public static final int CellType_Weight2     = 3;
    public static final int CellType_Blank       = -1;
    public static final int CellType_Both        = -2; 
    
    int MatrixType;
   
    public Rule(){
    }
    
    public void initialize() {
        this.clear();
    }

    public boolean equals(Rule R) {
        if (!this.equals(R)) return false;
	return true;
    }
}
