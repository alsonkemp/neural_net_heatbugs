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


public class RuleList_Square extends RuleList {
    protected int SquareSize = 4;
    public RuleList_Square(MatrixElementFactory MEF, RuleFactory RF) {
        // call the superclass's constructor
        super(MEF, RF);
        SquareSize = 4;
    }
    
    public int GenerateRules(java.util.BitSet Genotype, int initialgene) {
        boolean Continue;
        int TempRuleType, TempValue2;
        int loop1, loop2;
        int BSLocation = initialgene;
        int BSLength = Genotype.length() - 1;  //ignore mandatory true bit at end
        MatrixElement TempMatrixElement;
        
        clear();
        
        for (BSLocation = 0;  BSLocation < (BSLength-RuleElementLength);  BSLocation++) {
            Rule TempRule = RF.get();
            
            if (Genotype.get(BSLocation)) {
                TempRule.MatrixType = Rule.MatrixType_Adjustment;
            } else{
                TempRule.MatrixType = Rule.MatrixType_Expansion;
            } 
 
            Continue = ((BSLocation + RuleElementLength) < BSLength);
            for ( ; Continue; ) {
                
                //Decoding modifcation matrix elements
                TempMatrixElement = MEF.get();
                
                TempRuleType = (Genotype.get(BSLocation+1) ? 1 : 0)
                            + (Genotype.get(BSLocation+2) ? 2 : 0);
                TempValue2   = (Genotype.get(BSLocation+3) ? 1 : 0)
                            + (Genotype.get(BSLocation+4) ? 2 : 0)
                            + (Genotype.get(BSLocation+5) ? 4 : 0)
                            + (Genotype.get(BSLocation+6) ? 8 : 0)
                            + (Genotype.get(BSLocation+7) ? 16 : 0);
                
                BSLocation += RuleElementLength;
                Continue = ((BSLocation + RuleElementLength) < BSLength);
                
                switch (TempRuleType) {
                    case Rule.CellType_RuleTarget:
                    case Rule.CellType_RuleTarget2:
                        RulesCreated++;
                        RulesRuleTarget++;
                        TempMatrixElement.RuleTargetOffset = TempValue2 - 15;
                        TempRule.add(TempMatrixElement);
                        break;
                    case Rule.CellType_LastCell:
                        //treat like a weight (increase complexity?)
                    case Rule.CellType_Weight:
                    case Rule.CellType_Weight2:
                        RulesCreated++;
                        RulesWeight++;
                        TempMatrixElement.Weight = TempValue2 - 15;
                        TempRule.add(TempMatrixElement);
                        break; 
                    case Rule.CellType_Both:
                        RulesCreated++;
                        RulesBoth++;
                        TempMatrixElement.RuleTargetOffset = TempValue2 / 4;
                        TempMatrixElement.Weight = (TempValue2 % 3) - 1;
                        TempRule.add(TempMatrixElement);
                        break;
                    case Rule.CellType_Blank:
                        RulesCreated++;
                        RulesBoth++;
                        TempRule.add(TempMatrixElement);
                        break;
                }
                
                if (TempRule.size() == SquareSize)
                    Continue = false;
            }
            if (TempRule.size() == SquareSize)
                add(TempRule);
        }
        FilterRules();
        this.trimToSize();
        return size();
    }
}