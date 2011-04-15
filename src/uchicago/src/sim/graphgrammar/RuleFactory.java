/*
 * MatrixElementStore.java
 *
 * Created on December 30, 2003, 6:17 PM
 */

package uchicago.src.sim.graphgrammar;

/**
 *
 * @author  Alson Kemp
 */
public class RuleFactory extends java.util.ArrayList {
    
    private static final int increment = 100;
    private int LastUsed;
    
    /** Creates a new instance of MatrixElementStore */
    public RuleFactory() {
        LastUsed = 0;
    }

    public synchronized Rule get() {
        if (this.size() < LastUsed + 2){
            for (int i = 0; i < increment; i++)
                add(new Rule());
            System.out.println("RuleFactory: added up to "+this.size());

        }
        LastUsed++;
        return (Rule)get(LastUsed);
    }
    
    public synchronized void initialize() {
        LastUsed = 0;
        for (java.util.Iterator i = this.iterator(); i.hasNext(); ) {
            ((Rule)i.next()).initialize();
        }
    }
}