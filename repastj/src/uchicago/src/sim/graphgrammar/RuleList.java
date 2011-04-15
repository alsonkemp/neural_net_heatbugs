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


public class RuleList extends java.util.ArrayList {
    protected static final int MaxNodes = 100;
    protected static final boolean RuleTargetsClip = false;
    public  static final int RuleElementLength = 8;
    //public  static final int RuleElementLength = 4;
    protected static int RulesCreated;
    protected static int RulesNone;
    protected static int RulesWeight;
    protected static int RulesRuleTarget;
    protected static int RulesBoth;
    
    protected static MatrixElementFactory MEF;
    protected static RuleFactory RF;

    protected int CurrentRule;
    protected int RulesApplied;

    /** Creates new Rules */
    public RuleList(MatrixElementFactory MEF, RuleFactory RF) {
        // call the superclass's constructor
        super();
        
        this.MEF = MEF;
        this.RF = RF;        
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
                        TempMatrixElement.RuleTargetOffset = TempValue2;
                        TempRule.add(TempMatrixElement);
                        break;
                    case Rule.CellType_LastCell:
                        Continue = false;
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
            }
            add(TempRule);
        }
    FilterRules();
    this.trimToSize();
    return size();
    }

    void FilterRules(){
        int loop;
        boolean allempty;
        java.util.Iterator iter;
        MatrixElement TempME;
        
// Don't clean out rules - maintain as much structure as possible
/*        for (loop = 0; loop < size(); loop++) {
            Rule TempRule = (Rule)get(loop);
            if (TempRule.NumMatrixElements == 0) {
                remove(loop);
                loop--;
            }
/*            else {
                allempty = true;
                iter = TempRule.getMatrixElements().iterator();

                for (; iter.hasNext() && allempty; ){
                    TempME = (MatrixElement)iter.next();
                    if (!TempME.isEmpty())
                        allempty = false;
                }
                
                if (allempty){
                    remove(loop);
                    loop--;
                }
            }
 
        }
*/ /*
        //set RuleTargetOffsets relative to the present rule
        for (loop = 0; loop < size(); loop++) {
            Rule TempRule = (Rule)get(loop);
            for (int loop2 = 0; loop2 < TempRule.size(); loop2++) {
                TempME = (MatrixElement)TempRule.get(loop2);

                if (TempME.RuleTargetOffset != Integer.MIN_VALUE) {
                    if (loop < 3)
                        TempME.RuleTargetOffset += 3;
                    else
                        TempME.RuleTargetOffset += loop + 1;

                    if (TempME.RuleTargetOffset > (size()-1) ){
                        if (RuleTargetsClip)
                            TempME.RuleTargetOffset = size()-1;
                         else
                            TempME.RuleTargetOffset = Integer.MIN_VALUE;
                    }
                }
            }
        } */
        //set RuleTargetOffsets relative to the present rule
        for (loop = 0; loop < size(); loop++) {
            Rule TempRule = (Rule)get(loop);
            for (int loop2 = 0; loop2 < TempRule.size(); loop2++) {
                TempME = (MatrixElement)TempRule.get(loop2);
                if ( (TempME.RuleTargetOffset != Integer.MIN_VALUE) && 
                        (TempME.RuleTargetOffset != 0) ){
                    TempME.RuleTargetOffset = (loop + TempME.RuleTargetOffset + size()) % size();
                } else {
                    TempME.RuleTargetOffset = Integer.MIN_VALUE;
                }
            }
        }
    }

    public int GetNumRules() {
        return size();
    } 
    
    public java.lang.String toString() {
        StringBuffer RuleString = new StringBuffer();
        java.lang.Integer TempInteger;
        int loop1, loop2;
        
        for (int loop = 0; loop < size(); loop++) {
            Rule TempRule = (Rule)get(loop);
            RuleString.append(loop);
            if (TempRule.MatrixType == Rule.MatrixType_Adjustment) {
                RuleString.append("\tAdjustment:\n");
            } else {
                RuleString.append("\tExpansion:\n");
            }
                
            for (int elementloop = 0; elementloop < TempRule.size(); elementloop++) {
                RuleString.append("\t"+ ((MatrixElement)TempRule.get(elementloop)).toString());
                if (((elementloop +1) % (new Double(Math.ceil(Math.sqrt(TempRule.size()))).intValue())) == 0) {
                    RuleString.append("\n");
                }
            }
            RuleString.append("\n");
        }
        return RuleString.toString();
    }   

    public static int  getRulesCreated(){return RulesCreated;}
    public static void setRulesCreated(int i){RulesCreated = i;}
    public static int  getRulesNone(){return RulesNone;}
    public static void setRulesNone(int i){RulesNone = i;}
    public static int  getRulesRuleTarget(){return RulesRuleTarget;}
    public static void setRulesRuleTarget(int i){RulesRuleTarget = i;}
    public static int  getRulesWeight(){return RulesWeight;}
    public static void setRulesWeight(int i){RulesWeight = i;}
    public static int  getRulesBoth(){return RulesBoth;}
    public static void setRulesBoth(int i){RulesBoth = i;}

}