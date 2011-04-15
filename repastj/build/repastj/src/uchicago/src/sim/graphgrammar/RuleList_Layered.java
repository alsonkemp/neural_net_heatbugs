/*
 * Rules.java
 *
 * Created on January 6, 2002, 9:30 PM
 */

/**
 *
 * @author  Alson Kemp
 * @version
 */
package uchicago.src.sim.graphgrammar;


public class RuleList_Layered extends RuleList {
    protected int SquareSize = 4;
    /** Creates new Rules */
    public RuleList_Layered(MatrixElementFactory MEF, RuleFactory RF) {
        // call the superclass's constructor
        super(MEF,RF);
    }
     
    public int GenerateRules(java.util.BitSet Genotype, int initialgene) {
        boolean Continue;
        int TempRuleType, TempValue2;
        int loop1, loop2;
        int BSLocation = initialgene;
        int BSLength = Genotype.length() - 1;  //ignore mandatory true bit at end
        MatrixElement TempMatrixElement;
    
        if (RulesCreated > 1000){
            RulesCreated = 0;
            RulesNone = 0;
            RulesWeight = 0;
            RulesRuleTarget = 0;
            RulesBoth = 0;
        }
        
        clear(); 
        
        for (BSLocation = 0;  BSLocation < (BSLength-RuleElementLength);) {
            Rule TempRule = RF.get();
            TempRule.MatrixType = Rule.MatrixType_Expansion;
            
            Continue = ((BSLocation + RuleElementLength) < BSLength);
            for ( ; Continue; ) {
                
                //Decoding modifcation matrix elements
                TempMatrixElement = MEF.get();
                TempValue2   = (Genotype.get(BSLocation) ? 1 : 0)
                             + (Genotype.get(BSLocation+1) ? 2 : 0)
                             + (Genotype.get(BSLocation+2) ? 4 : 0)
                             + (Genotype.get(BSLocation+3) ? 8 : 0);
                if ((BSLocation / 4 / 4) < 36) 
                    TempRuleType = Rule.CellType_RuleTarget;
                else
                    TempRuleType = Rule.CellType_Weight;
                
                BSLocation += RuleElementLength;
                Continue = ((BSLocation + RuleElementLength) <= BSLength);

                switch (TempRuleType) {
                    case Rule.CellType_RuleTarget:
                    case Rule.CellType_RuleTarget2:
                        RulesCreated++;
                        RulesRuleTarget++;
                        TempMatrixElement.RuleTargetOffset = TempValue2;
                        TempRule.add(TempMatrixElement);
                        break;
                    case Rule.CellType_Weight:
                    case Rule.CellType_Weight2:
                        RulesCreated++;
                        RulesWeight++;
                        TempMatrixElement.Weight = 7*( (TempValue2) -1);
                        if ( (TempMatrixElement.Weight > 7) || (TempMatrixElement.Weight ==0) )
                            TempMatrixElement.Weight = Integer.MIN_VALUE;
                        TempRule.add(TempMatrixElement);
                        break;
                }
                
                if (TempRule.size() == SquareSize)
                    Continue = false;
            }
            add(TempRule);
        }
    FilterRules();
    this.trimToSize();
    return size();
    }

    void FilterRules(){
        int loop, layer;
        boolean allempty;
        java.util.Iterator iter;
        MatrixElement TempME;
        
        //set RuleTargetOffsets relative to the present rule
        for (loop = 0; loop < size(); loop++) {
            Rule TempRule = (Rule)get(loop);
            for (int loop2 = 0; loop2 < TempRule.size(); loop2++) {
                TempME = (MatrixElement)TempRule.get(loop2);
                if (loop < 4)
                    layer = 4;
                else if (loop < 20)
                    layer = 20;
                else
                    layer = 36;
                if ( (TempME.RuleTargetOffset != Integer.MIN_VALUE)){
                    TempME.RuleTargetOffset += layer;
                } 
            }
        }
    }

}