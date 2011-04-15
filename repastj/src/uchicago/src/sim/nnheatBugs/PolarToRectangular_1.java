/*
 * PolarToRectangular.java
 *
 * Created on December 31, 2003, 2:45 PM
 */

package uchicago.src.sim.nnheatBugs;

/**
 *
 * @author  Alson Kemp
 */
public class PolarToRectangular_1 {
    //EF012
    //D7013
    //C6-24
    //B5435
    //A9876
    
    private static final int[][] ldx =
     {{ 0, 1, 1, 1, 0,-1,-1,-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      { 0, 1, 2, 2, 2, 2, 2, 1, 0,-1,-2,-2,-2,-2,-2,-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      { 0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 2, 1, 0,-1,-2,-3,-3,-3,-3,-3,-3,-3,-2,-1, 0, 0, 0, 0, 0, 0, 0, 0},
      { 0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 2, 1, 0,-1,-2,-3,-4,-4,-4,-4,-4,-4,-4,-4,-4,-3,-2,-1}};
     
    private static final int[][] ldy =
     {{-1,-1, 0, 1, 1, 1, 0,-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {-2,-2,-2,-1, 0, 1, 2, 2, 2, 2, 2, 1, 0,-1,-2,-2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {-3,-3,-3,-3,-2,-1, 0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 2, 1, 0,-1,-2,-3,-3,-3, 0, 0, 0, 0, 0, 0, 0, 0},
      {-4,-4,-4,-4,-4,-3,-2,-1, 0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 2, 1, 0,-1,-2,-3,-4,-4,-4,-4}};
    private static final int[] lm =
      {8,
       16,
       24,
       32};
      /** Creates a new instance of PolarToRectangular */
       public PolarToRectangular_1() {
      }
      
      public static final int dx(int distance, int rotation) {
        int d = distance -1;
        return ldx[d][(rotation+lm[d]) % lm[d]];
      }

      public static final int dy(int distance, int rotation) {
        int d = distance -1;
        return ldy[d][(rotation+lm[d]) % lm[d]];
      }
      
}
