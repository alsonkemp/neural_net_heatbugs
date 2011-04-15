/*
 * RuleList_LargeSquare.java
 *
 * Created on December 23, 2003, 11:07 AM
 */

package uchicago.src.sim.graphgrammar;

/**
 *
 * @author  Alson Kemp
 */
public class RuleList_LargeSquare extends RuleList_Square {
    
    /** Creates a new instance of RuleList_LargeSquare */
    public RuleList_LargeSquare(MatrixElementFactory MEF, RuleFactory RF) {
        super(MEF, RF);
        SquareSize = 16;
    }
    
}
