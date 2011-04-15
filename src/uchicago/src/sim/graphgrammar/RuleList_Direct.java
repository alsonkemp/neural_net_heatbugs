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


public class RuleList_Direct extends RuleList {
    public RuleList_Direct(MatrixElementFactory MEF, RuleFactory RF) {
        // call the superclass's constructor
        super(MEF, RF);
    }
    
    public int GenerateRules(java.util.BitSet Genotype, int initialgene) {
        boolean Continue;
        int width,height;
        int TempRuleType, TempValue2;
        int loop1, loop2;
        int BSLocation = initialgene;
        int BSLength = Genotype.length() - 1;  //ignore mandatory true bit at end
        MatrixElement TempMatrixElement;
        Rule TempRule;
        
        clear();
        width = (Genotype.length()/4)/RuleElementLength;
        width *= RuleElementLength;
        for (loop1 = 0; loop1 < 4; loop1++) {
            TempRule = RF.get();
            TempRule.MatrixType = Rule.MatrixType_Expansion;

            for (BSLocation = width * loop1; BSLocation < width * (loop1+1); BSLocation += RuleElementLength) {
                //Decoding modifcation matrix elements
                TempMatrixElement = MEF.get();

                TempRuleType = (Genotype.get(BSLocation+1) ? 1 : 0)
                             + (Genotype.get(BSLocation+2) ? 2 : 0);
                TempValue2   = (Genotype.get(BSLocation+3) ? 1 : 0)
                             + (Genotype.get(BSLocation+4) ? 2 : 0)
                             + (Genotype.get(BSLocation+5) ? 4 : 0)
                             + (Genotype.get(BSLocation+6) ? 8 : 0)
                             + (Genotype.get(BSLocation+7) ? 16 : 0);

                switch (TempRuleType) {
                    case Rule.CellType_Weight:
                        RulesCreated++;
                        RulesWeight++;
                        TempMatrixElement.Weight = TempValue2 - 15;
                        TempRule.add(TempMatrixElement);
                        break;
                    default:
                        RulesCreated++;
                        RulesNone++;
                        TempRule.add(TempMatrixElement);
                        break;
                }
            }
            add(TempRule);
        }
        return size();
    }
}