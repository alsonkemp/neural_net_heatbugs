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
public class MatrixElementFactory extends java.util.ArrayList {
    
    private static final int increment = 100;
    private int LastUsed;
    
    /** Creates a new instance of MatrixElementStore */
    public MatrixElementFactory() {
        LastUsed = 0;
    }

    public synchronized MatrixElement get() {
        if (this.size() < LastUsed + 2){
            for (int i = 0; i < increment; i++)
                add(new MatrixElement());
            System.out.println("MatrixElementFactory: added up to "+this.size());
        }
        LastUsed++;
        MatrixElement me;
        me = (MatrixElement)get(LastUsed);
        me.initialize();
        return me;
    }

    public synchronized MatrixElement get(int i, int j) {
        MatrixElement me;
        me = (MatrixElement)get();
        me.setRuleTargetOffset(i);
        me.setWeight(j);
        return me;
    }

    public synchronized MatrixElement get(int i, int j, int k) {
        MatrixElement me;
        me = (MatrixElement)get();
        me.setRuleTargetOffset(i);
        me.setWeight(j);
        me.setLastModified(k);
        return me;
    }
    
    public synchronized void initialize() {
        LastUsed = 0;
        for (java.util.Iterator i = this.iterator(); i.hasNext(); ) {
            ((MatrixElement)i.next()).initialize();
        }

    }
}