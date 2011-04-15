package uchicago.src.sim.nnheatBugs;

import java.awt.Point;
import java.util.Vector;
import java.util.Random;

import uchicago.src.sim.space.*;
import cern.jet.random.Uniform;

/**
 * The environment that the heat bugs inhabit. This uses a Diffuse2D to
 * diffuse the heat absorbed from the bugs.
 *
 * @author Swarm Project and Nick Collier
 * @version $Revision: 1.2 $ $Date: 2001/12/18 20:39:09 $
 */
public class HeatSpace extends Diffuse2D {

  public static final int HOT = 0;
  public static final int COLD = 1;
  private long maxHeat = Diffuse2D.MAX;
  private int[] xpoints = new int[9];
  private int[] ypoints = new int[9];

  public HeatSpace(double diffusionConstant, double evaporationRate,
          int xSize, int ySize) {
    super(diffusionConstant, evaporationRate, xSize, ySize);
  }

  public void addHeat(int x, int y, int heat) {
    long heatHere = (long) this.getValueAt(x, y);

    if (heatHere + heat >= maxHeat) {
      heatHere = maxHeat;
    } else if (heatHere + heat <= 0) {
      heatHere = 0;
    } else {
      heatHere += heat;
    }

    this.putValueAt(x, y, heatHere);
  }

  public void setHeat(int x, int y, int heat) {
    if (heat <= maxHeat) {
      this.putValueAt(x, y, heat);
    } else {
      this.putValueAt(x, y, maxHeat);
    }
  }

  /**
   * Find the extreme hot or cold within this 9 cell neighborhood
   *
   * @return the extreme point
   */
  public Point findExtreme(int type, int x, int y) {

    long bestHeat = (long) this.getValueAt(x, y);

    // iterate through the space to find the extreme
    //Vector heatList = new Vector();
    int count = 0;

    for (int py = y - 1; py <= y + 1; py++) {
      for (int px = x - 1; px <= x + 1; px++) {

        boolean hereIsBetter, hereIsEqual;

        long heatHere = (long) this.getValueAt(px, py);
        hereIsBetter = (type == COLD) ? (heatHere < bestHeat)
                : (heatHere > bestHeat);
        hereIsEqual = (heatHere == bestHeat);

        if (hereIsBetter) {
          //Point p = new Point(px, py);
          xpoints[0] = px;
          ypoints[0] = py;
          //heatList.clear();
          //heatList.add(p);
          count = 1;
          bestHeat = heatHere;
        }

        if (hereIsEqual) {
          xpoints[count] = px;
          ypoints[count] = py;
          count++;
          //Point p = new Point(px, py);
          //heatList.add(p);
        }
      }
    }

    // choose a random index from within the list
    int index = Uniform.staticNextIntFromTo(0, count - 1);
    //Point bestPoint = (Point)heatList.elementAt(index);
    //bestPoint.x = xnorm(bestPoint.x);
    //bestPoint.y = ynorm(bestPoint.y);
    return new Point(xnorm(xpoints[index]), ynorm(ypoints[index]));
  }
}
