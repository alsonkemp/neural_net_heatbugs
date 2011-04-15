package uchicago.src.sim.nnheatBugs;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Color;
import java.util.Hashtable;
import javax.swing.JComponent;

import uchicago.src.sim.space.*;
import uchicago.src.sim.network.*;
import uchicago.src.sim.gui.*;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.graphgrammar.*;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.reflector.DescriptorContainer;
import uchicago.src.reflector.BooleanPropertyDescriptor;
import uchicago.src.sim.gui.Network2DDisplay;
import cern.jet.random.Uniform;

/**
 * The agent for the Heat Bugs simulation. This pretty much follows the
 * Swarm code.
 *
 * @author Swarm Project and Nick Collier
 * @version $Revision: 1.3 $ $Date: 2001/12/18 20:39:09 $
 * @see HeatBugsModel
 */
public class HeatBug implements Drawable, DescriptorContainer {

  protected static final int COLD = 0;
  protected static final int HOT = 1;
  protected static final int MAXCOLORS = 256;
  protected static final int ACTION_NONE = 0x0;
  protected static final int ACTION_FIGHT = 0x1;
  protected static final int ACTION_BREED = 0x2;
  protected static Color[][] HeatColors;
  protected double unhappiness = 0.0;
  protected int LastAction, Action;
  protected int x, y;
  protected int idealTemp, minOutputHeat, maxOutputHeat;
  protected float randomMoveProb;
  protected boolean nndisplayed = false;
  protected boolean isMoving = false;
  protected HeatSpace space;
  protected Object2DTorus world;
  protected Dimension worldSize;
  protected HeatBugsModel model;
  protected int xSize;
  protected int ySize;
  protected boolean example = false;
  protected Hashtable descriptors = new Hashtable();
  protected SigmoidNeuralNetworkNode[] nodes;
  protected String[] names;
  protected java.util.BitSet Genotype;
  protected int GenotypeCardinality;
  protected double Age = 0.0;
  protected double totalUnhappiness;
  protected int matings;
  protected int RuleType;
  double SquashingFactor = 100.0;
  protected int heatOutput;
  protected int numOutputs = 5;
  protected int firstInput = 5;
  protected int numInputs = 9;
  protected static boolean BiasOnIdentity = true;
  protected static boolean IOClean = true;

  public HeatBug(HeatBugsModel model, HeatSpace space, Object2DTorus world, int x,
          int y, int idealTemp, int minOutputHeat, int maxOutputHeat, java.util.BitSet Genotype, int RuleType) {
    this.x = x;
    this.y = y;
    this.idealTemp = idealTemp;
    this.minOutputHeat = minOutputHeat;
    this.maxOutputHeat = maxOutputHeat;
    this.randomMoveProb = randomMoveProb;
    this.space = space;
    this.world = world;
    this.model = model;
    this.Genotype = Genotype;
    this.GenotypeCardinality = Genotype.cardinality();
    this.RuleType = RuleType;
    worldSize = world.getSize();
    xSize = worldSize.width;
    ySize = worldSize.height;
    Age = 0.0;
    LastAction = ACTION_NONE;
    Action = ACTION_NONE;
    totalUnhappiness = 0.0;
    hasMovedXp = false;
    hasMovedXn = false;
    hasMovedYp = false;
    hasMovedYn = false;
    distanceTraveled = 0;
    hasHeated = false;
    matings = -1;

    numOutputs = 4;
    heatOutput = 4;
    firstInput = numOutputs;
    numInputs = 9;
    UsedNodes = -1;
    names = new String[firstInput + numInputs];

    BooleanPropertyDescriptor bd = new BooleanPropertyDescriptor("BDExample", false);
    descriptors.put("BDExample", bd);
    if (HeatColors == null) {
      buildHeatColors();
    }
  }

  public void setXY(int x, int y) {
    this.x = x;
    this.y = y;
    world.putObjectAt(x, y, this);
  }
  protected int deltaX = 0;
  protected int deltaY = 0;
  protected int distanceTraveled = 0;
  protected boolean hasMovedXp;
  protected boolean hasMovedYp;
  protected boolean hasMovedXn;
  protected boolean hasMovedYn;
  protected boolean hasHeated;

  public Object[] step() {
    int i;
    int n;

    unhappiness = 0.0;
    isMoving = false;

    // get the neighbors
    int prevX = SimUtilities.norm(x - 1, xSize);
    int nextX = SimUtilities.norm(x + 1, xSize);
    int prevY = SimUtilities.norm(y - 1, ySize);
    int nextY = SimUtilities.norm(y + 1, ySize);


    Age += 1.0;

    deltaX = 0;
    deltaY = 0;

    collectExternalInputs();
    for (i = 0; i < nodes.length; i++) {
      nodes[i].collectInputs();
    }
    for (i = 0; i < nodes.length; i++) {
      nodes[i].calculateOutputs();
    }
    collectOutputs();

    if (((deltaX != 0) || (deltaY != 0))) {
      int newX = SimUtilities.norm(x + deltaX, xSize);
      int newY = SimUtilities.norm(y + deltaY, ySize);
      Object temp = world.getObjectAt(newX, newY);
      if (temp == null) {  //check to see if the new square is empty
        world.putObjectAt(x, y, null);  //pop off the old square ...
        x = newX;
        y = newY;
        world.putObjectAt(x, y, this);  //and land on the new square.
        distanceTraveled++;
        isMoving = true;
        if (deltaX > 0) {
          hasMovedXp = true;
        }
        if (deltaX < 0) {
          hasMovedXn = true;
        }
        if (deltaY > 0) {
          hasMovedYp = true;
        }
        if (deltaY < 0) {
          hasMovedYn = true;
        }
      } else { //square is occupied
        unhappiness += 1.0;
      }
    }
    unhappiness += Math.abs((double) (idealTemp - space.getValueAt(x, y))) / idealTemp;
    //penalty for having only enough or less nodes than I/O requires
    //if (getNumInputsUsed() < 9) {
    //  unhappiness += (9.0 - getNumInputsUsed());
    //}
    //if (getNumOutputsUsed() < 5) {
    //  unhappiness += (5.0 - getNumOutputsUsed());
    //}
    //if (getNumberOfNodes() < 30) {
    //  unhappiness += (30.0 - getNumberOfNodes());
    //}

    //totalUnhappiness is forgetful...
    totalUnhappiness *= 0.90;
    totalUnhappiness += unhappiness;
    if (unhappiness < totalUnhappiness)
      totalUnhappiness = unhappiness;
    
    // heat our space
    //if (nodes.length > 4) {
    //  space.addHeat(x, y, (int) Math.round(minOutputHeat + (maxOutputHeat - minOutputHeat) * nodes[4].getOutput()));
    //} else {
      space.addHeat(x, y, minOutputHeat );
    //}

    return null;
  }

  public void collectExternalInputs() {
    int n;
    //if a square is occupied signal by delivering max heat (disincentive)
    for (n = 0; (n < numInputs) && (nodes.length > (firstInput + n)); n++) {
      if (n == 0) {
        nodes[firstInput + n].setExternalInput(getProcessedInput(x, y, SquashingFactor));
      } else {
        if (getObjectAtR(1, n - 2) == null) {
          nodes[firstInput + n].setExternalInput(getProcessedInputR(1, n - 2, SquashingFactor));
        } else {
          nodes[firstInput + n].setExternalInput(2.0);
        }
      }
    }
  }

  public void collectOutputs() {
    //retrieve outputs and translate them
    // 0: x-1, 1: x+1, 2: y-1, 3: y+1
    if (nodes.length > 0) {
      deltaX -= ((nodes[0].getOutput() > SigmoidNeuralNetworkNode.getActivationThreshold()) ? 1 : 0);
    }
    if (nodes.length > 1) {
      deltaX += ((nodes[1].getOutput() > SigmoidNeuralNetworkNode.getActivationThreshold()) ? 1 : 0);
    }
    if (nodes.length > 2) {
      deltaY -= ((nodes[2].getOutput() > SigmoidNeuralNetworkNode.getActivationThreshold()) ? 1 : 0);
    }
    if (nodes.length > 3) {
      deltaY += ((nodes[3].getOutput() > SigmoidNeuralNetworkNode.getActivationThreshold()) ? 1 : 0);
    }
  }

  public void buildNetwork(int useInhibitory) {
    int i, j;
    int source, destination;
    int _strength;
    DefaultEdge de;
    java.util.ArrayList ConnectionList, templist;
    Matrix ConnectionMatrix = new Matrix(RuleType);
    int Denominator = 16;

    ConnectionMatrix.GenerateMatrix(Genotype);
    ConnectionList = ConnectionMatrix.getConnectionList();

    nodes = new SigmoidNeuralNetworkNode[ConnectionMatrix.getNumberOfNodes()];

    for (i = 0; i < nodes.length; i++) {
      nodes[i] = new SigmoidNeuralNetworkNode();
    }


    for (i = 0; i < ConnectionList.size(); i++) {
      templist = (java.util.ArrayList) ConnectionList.get(i);
      source = ((Integer) templist.get(0)).intValue();
      destination = ((Integer) templist.get(1)).intValue();
      _strength = (((Integer) templist.get(2)).intValue());
      if (((destination == source) && (BiasOnIdentity))
              || ((destination == 0) && (!BiasOnIdentity))) {
        nodes[source].setBias(((double) (_strength)) / Denominator);
        continue;
      }

      if (_strength == 0) {
        continue;
      }

      if (IOClean && ((source < numOutputs)
              || ((destination >= firstInput) && (destination < firstInput + numInputs)))) {
        continue;
      }

      if (!BiasOnIdentity) {
        destination--;
      }
      de = new DefaultEdge(nodes[source], nodes[destination], "", 0.0f);
      if ((useInhibitory == 0) || Math.signum(_strength) >= 0) {
        nodes[destination].addInEdge(de);
        de.setStrength(Math.signum(_strength) * Math.exp(2.014828 * Math.abs(_strength / 7.0)) / 7.5);
        nodes[source].addOutEdge(de);
      } else {
        nodes[destination].addInInhibitoryEdge(de);
        de.setStrength( Math.exp(2.014828 * Math.abs(_strength / 7.0)) / 7.5);
        nodes[source].addOutInhibitoryEdge(de);
      }
    }

    //Normalize first and then clean
    //This allows non-useful edges to attenuate the normalized strength edges
    //Also prevents disruption in the network during evolution.
    //e.g. if a formerly "cleaned out" node becomes useful, it doesn't disturb the strengths.
    //normalizeNetwork();
    cleanNetwork();
    ConnectionMatrix = null;
  }

  //normalize the network outputs so that only x units of output are shared
  //amongst the destination neurons
  public void normalizeNetwork() {
    java.util.ArrayList ConnectionList, templist;
    DefaultEdge tempedge;
    int i;
    double sum;
    final double Normal = 2.0;

    for (i = 0; i < nodes.length; i++) {
      templist = (java.util.ArrayList) nodes[i].getOutEdges();

      java.util.Iterator tempiter = templist.iterator();
      sum = 0.0;
      for (; tempiter.hasNext();) {
        tempedge = (DefaultEdge) tempiter.next();
        sum += Math.abs(tempedge.getStrength());
      }
      // guard against zero
      sum += 0.01;
      
      tempiter = templist.iterator();
      for (; tempiter.hasNext();) {
        tempedge = (DefaultEdge) tempiter.next();
        tempedge.setStrength(Normal * tempedge.getStrength() / sum);
      }
    }

  }

  public void cleanNetwork() {
    boolean changed;
    int i, j;
    DefaultNodeWithInhibitors node;
    java.util.Iterator iter;

    // remove links to nodes which have no output
    do {
      changed = false;
      for (i = firstInput; i < nodes.length; i++) {
        if ((nodes[i] != null) && ((nodes[i].getNumOutEdges() + nodes[i].getNumOutInhibitoryEdges()) == 0)) {
          iter = nodes[i].getInNodes().iterator();
          for (; iter.hasNext();) {
            node = (DefaultNodeWithInhibitors) iter.next();
            if (node.hasEdgeTo(nodes[i])) {
              node.removeEdgesTo(nodes[i]);
              nodes[i].removeEdgesFrom(node);
              changed = true;
            }
          }
          iter = nodes[i].getInInhibitoryNodes().iterator();
          for (; iter.hasNext();) {
            node = (DefaultNodeWithInhibitors) iter.next();
            if (node.hasEdgeToInhibitory(nodes[i])) {
              node.removeEdgesToInhibitory(nodes[i]);
              nodes[i].removeEdgesFromInhibitory(node);
              changed = true;
            }
          }
        }
      }
    } while (changed == true);

    // remove links from nodes which have no input
    do {
      changed = false;
      for (i = firstInput + numInputs; i < nodes.length; i++) {
        if ((nodes[i] != null) && (nodes[i].getNumInEdges() == 0)) {
          iter = nodes[i].getOutNodes().iterator();
          for (; iter.hasNext();) {
            node = (DefaultNodeWithInhibitors) iter.next();
            if (node.hasEdgeFrom(nodes[i])) {
              node.removeEdgesFrom(nodes[i]);
              nodes[i].removeEdgesTo(node);
              changed = true;
            }
          }
          iter = nodes[i].getOutInhibitoryNodes().iterator();
          for (; iter.hasNext();) {
            node = (DefaultNodeWithInhibitors) iter.next();
            if (node.hasEdgeFromInhibitory(nodes[i])) {
              node.removeEdgesFromInhibitory(nodes[i]);
              nodes[i].removeEdgesToInhibitory(node);
              changed = true;
            }
          }
        }
      }
    } while (changed == true);

    /*        //delete the empty nodes
    java.util.ArrayList al = new java.util.ArrayList();
    for (i = 0; i < nodes.length; i++)
    if (i<14)
    al.add(nodes[i]);
    else if ((nodes[i] != null) && ((nodes[i].getNumOutEdges() + nodes[i].getNumOutInhibitoryEdges()) != 0))
    al.add(nodes[i]);
    al.trimToSize();
    nodes =  new SigmoidNeuralNetworkNode[al.size()];
    al.toArray(nodes); */
  }

  public double getUnhappiness() {
    return unhappiness;
  }

  public boolean canBreed(double BreedAtAge) {
    if (Age < BreedAtAge) {
      return false;
    }
    if (!getHasMoved()) {
      return false;
    }
    if (isMoving) {
      return false;
    }
    if (!isWellFormed()) {
      return false;
    }
    if (this.Age < (matings + 10.0)) {
      return false;
    }
    return true;
  }

  public boolean isWellFormed() {
    if (getPercentOfOutputsUsed() < 0.95) {
      return false;
    }
    if (getPercentOfInputsUsed() < 0.95) {
      return false;
    }
    return true;
  }

  public boolean getHasMoved() {
    if ( (((hasMovedXp ? 1 : 0) + (hasMovedXn ? 1 : 0) + (hasMovedYp ? 1 : 0) + (hasMovedYn ? 1 : 0)) >= 3) && (distanceTraveled > 10) ) {
      return true;
    }
    return false;
    //return (hasMovedXp && hasMovedXn) && (hasMovedYp && hasMovedYn);
    //return (hasMovedXp || hasMovedXn) || (hasMovedYp || hasMovedYn);
  }

  protected double getProcessedInput(int x, int y, double sf) {
    return getProcessedInput((double) space.getValueAt(x, y), sf);
  }

  protected double getProcessedInputR(int distance, int rot, double sf) {
    int targetX = SimUtilities.norm(x + PolarToRectangular.dx(distance, rot), xSize);
    int targetY = SimUtilities.norm(y + PolarToRectangular.dy(distance, rot), ySize);
    return getProcessedInput((double) space.getValueAt(targetX, targetY), sf);
  }

  protected Object getObjectAtR(int distance, int rot) {
    int targetX = SimUtilities.norm(x + PolarToRectangular.dx(distance, rot), xSize);
    int targetY = SimUtilities.norm(y + PolarToRectangular.dy(distance, rot), ySize);
    return world.getObjectAt(targetX, targetY);
  }

  protected double getProcessedInput(double val, double sf) {
    //return (val - idealTemp) / IdealTemp / SquashingFactor;
    //return 0.5 + Math.atan( (val - idealTemp)/SquashingFactor)/Math.PI;
    return -1.0 + 2.0 / (1.0 + Math.exp(-1.0 * ((val - idealTemp) / sf)));
    //return Math.abs(-1.0 + 2.0/(1.0 + Math.exp(-1.0* ((val - idealTemp)/sf))));
    //return 1.0-Math.abs(-1.0 + 2.0/(1.0 + Math.exp(-1.0* ((val - idealTemp)/sf))));
  }

  public double getTemperature() {
    return (double) space.getValueAt(x, y);
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public void setMatings() {
    matings = (int) this.Age;
  }

  public int getRuleType() {
    return this.RuleType;
  }

  public int getIdealTemp() {
    return idealTemp;
  }

  public void setSquashingFactor(double val) {
    SquashingFactor = val;
  }

  public void setIdealTemp(int idealTemp) {
    this.idealTemp = idealTemp;
  }

  public int getMinOutputHeat() {
    return minOutputHeat;
  }

  public void setMinOutputHeat(int minOutputHeat) {
    this.minOutputHeat = minOutputHeat;
  }

  public int getMaxOutputHeat() {
    return maxOutputHeat;
  }

  public void setMaxOutputHeat(int maxOutputHeat) {
    this.maxOutputHeat = maxOutputHeat;
  }

  public float getRandomMoveProb() {
    return randomMoveProb;
  }

  public void setRandomMoveProb(float f) {
    randomMoveProb = f;
  }

  public static void setBiasOnIdentity(boolean b) {
    BiasOnIdentity = b;
  }

  public static boolean getBiasOnIdentity() {
    return BiasOnIdentity;
  }

  public static void setIOClean(boolean b) {
    IOClean = b;

  }

  public static boolean getIOClean() {
    return IOClean;
  }

  public void setBDExample(boolean val) {
    example = val;
  }

  public boolean getBDExample() {
    return example;
  }

  public int getAction() {
    return Action;
  }

  public double getAge() {
    return Age;
  }

  public double getTotalUnhappiness() {
    return totalUnhappiness;
  }

  public int getStructure() {
    //return 10000*nodes.length + GenotypeCardinality;
    //return nodes.length;
    //return 1000*nodes.length + getNumConnections();
    //return 1000000*nodes.length + 1000*getNumInhibitoryConnections() + getNumConnections();
    return 1000000 * getRawNumberOfNodes() + 1000 * getNumberOfNodes() + getNumConnections();
  }

  public java.util.BitSet getGenotype() {
    return Genotype;
  }

  public double getConnectionsRatio() {
    if (getNumberOfNodes() == 0) {
      return 0;
    }
    return getNumConnections() / ((double) getNumberOfNodes());
  }

  public double getBiasRatio() {
    if (getNumberOfNodes() == 0) {
      return 0;
    }
    return getNumBiases() / ((double) getNumberOfNodes());
  }

  public double getInhibitoryConnectionsRatio() {
    if (getNumberOfNodes() == 0) {
      return 0;
    }
    return getNumInhibitoryConnections() / ((double) getNumberOfNodes());


  }
  protected int UsedNodes;

  public int getNumberOfNodes() {
    if (UsedNodes == -1) {
      UsedNodes++; //up from -1
      for (int i = 0; i < nodes.length; i++) {
        if (((nodes[i].getOutInhibitoryDegree() + nodes[i].getOutDegree()) != 0)
                || ((nodes[i].getInInhibitoryDegree() + nodes[i].getInDegree()) != 0)) {
          UsedNodes++;
        }
      }
    }
    return UsedNodes;
  }

  public int getRawNumberOfNodes() {
    return nodes.length;
  }
  private int NC = -1;

  public int getNumConnections() {
    int num = 0;
    if (NC != -1) {
      return NC;
    }
    for (int i = 0; i < nodes.length; i++) {
      num += nodes[i].getOutEdges().size();
    }
    NC = num;
    return NC;
  }
  private int NB = -1;

  public int getNumBiases() {
    int num = 0;
    if (NB != -1) {
      return NB;
    }
    for (int i = 0; i < nodes.length; i++) {
      if (Math.abs(nodes[i].getBias()) > 0.001) {
        num++;
      }
    }
    NB = num;
    return NB;
  }

  public double getPercentOfOutputsUsed() {
    return ((double) getNumOutputsUsed()) / numOutputs;
  }
  private int NOU = -1;

  public int getNumOutputsUsed() {
    int num = 0;

    if (NOU != -1) {
      return NOU;
    }
    for (int i = 0; i < (nodes.length < numOutputs ? nodes.length : numOutputs); i++) {
      if (nodes[i].getNumInEdges() > 0) {
        ++num;
      }
    }
    NOU = num;
    return NOU;
  }

  public double getPercentOfInputsUsed() {
    return ((double) getNumInputsUsed()) / numInputs;
  }
  private int NIU = -1;

  public int getNumInputsUsed() {
    int num = 0;
    if (NIU != -1) {
      return NIU;
    }

    for (int i = (nodes.length < firstInput ? nodes.length : firstInput);
            i < (nodes.length < (firstInput + numInputs) ? nodes.length : (firstInput + numInputs));
            i++) {
      if (nodes[i].getNumOutEdges() > 0) {
        ++num;
      }
    }
    NIU = num;
    return NIU;
  }
  private int NIC = -1;

  public int getNumInhibitoryConnections() {
    int num = 0;

    if (NIC != -1) {
      return NIC;
    }
    for (int i = 0; i < nodes.length; i++) {
      num += nodes[i].getOutInhibitoryEdges().size();
    }
    NIC = num;
    return NIC;
  }

  // DescriptorContainer interface
  public Hashtable getParameterDescriptors() {
    return descriptors;
  }

  public void draw(SimGraphics g) {
    int uval, hval;
    double temp;

    //if (getAction() == ACTION_BREED) {
    //  g.drawFastRoundRect(HeatColors[1][ACTION_BREED]);
    //} else if (getAction() == ACTION_FIGHT) {
    //  g.drawFastRoundRect(HeatColors[1][ACTION_FIGHT]);
    //} //else if (nodes.length > heatOutput)
    //    if (nodes[heatOutput].getOutput() > 0.0)
    //        g.drawFastRoundRect(HeatColors[0][(int)(nodes[heatOutput].getOutput()*MAXCOLORS)]);
    if ((matings>0) &&this.Age < (matings + 100.0)) {
      g.drawFastRoundRect(HeatColors[1][ACTION_NONE]);
    } else {
      //g.drawFastRoundRect(HeatColors[0][(int) Math.round(Math.min(unhappiness, 1.0) * MAXCOLORS)]);
      g.drawFastRoundRect(HeatColors[0][MAXCOLORS - (int) Math.round(Math.min(1, Math.max(0, Math.abs(this.getTemperature() / model.getMinIdealTemp() - 1.0))) * MAXCOLORS)]);
    }


    //g.draw4ColorHollowRect(Color.red, Color.green, Color.cyan, Color.pink);
  }

  public void buildHeatColors() {
    int i;
    HeatColors = new Color[2][MAXCOLORS + 1];

    for (i = 0; i <= MAXCOLORS; i++) {
      HeatColors[0][i] = new java.awt.Color(
              1.0f - (1.0f / MAXCOLORS) * i,
              (1.0f / MAXCOLORS) * i,
              1.0f - (1.0f / MAXCOLORS) * i);
    }

    HeatColors[1][ACTION_NONE] = new java.awt.Color(
            1.0f,
            1.0f,
            1.0f);
    HeatColors[1][ACTION_BREED] = new java.awt.Color(
            0.0f,
            0.0f,
            1.0f);
    HeatColors[1][ACTION_FIGHT] = new java.awt.Color(
            1.0f,
            0.0f,
            0.0f);
  }

  public String toString() {
    StringBuffer os = new StringBuffer();
    /*        os.append("Excitatory Connections\n");
    os.append("______________________\n");
    os.append(getNetworkConnections());
    os.append("\n\n");
    os.append("Inhibitory Connections\n");
    os.append("______________________\n");
    os.append(getInhibitoryNetworkConnections());
     **/
    Matrix ConnectionMatrix = new Matrix(RuleType);
    ConnectionMatrix.GenerateMatrix(Genotype);
    os.append(ConnectionMatrix.toString());
    os.append("\n--------------------------------------------------------------\n");
    os.append(getMixedNetworkConnections());
    ConnectionMatrix = null;
    return os.toString();
  }

  public String getNeuralNetwork() {
    if (!nndisplayed) {
      final String key = "weight";
      StringWindow sw = new StringWindow(toString());
      sw.setVisible(true);
      nndisplayed = true;

      try {
        Thread.currentThread().sleep(500);
      } catch (Exception e) {
        System.out.println("e");
      }

      edu.uci.ics.jung.graph.impl.DirectedSparseGraph DSG = new edu.uci.ics.jung.graph.impl.DirectedSparseGraph();

      edu.uci.ics.jung.graph.Vertex v[] =
              new edu.uci.ics.jung.graph.Vertex[nodes.length];
      for (int i = 0; i < nodes.length; i++) {
        v[i] = DSG.addVertex(new edu.uci.ics.jung.graph.impl.SparseVertex());
      }
      for (int i = 0; i < v.length; i++) {
        for (java.util.Iterator iter = nodes[i].getOutEdges().iterator(); iter.hasNext();) {
          DefaultEdge e = (DefaultEdge) iter.next();
          edu.uci.ics.jung.graph.impl.DirectedSparseEdge DSE =
                  new edu.uci.ics.jung.graph.impl.DirectedSparseEdge(v[i], v[findNumber(e.getTo())]);
          DSE.addUserDatum(key, new Double(e.getStrength()), edu.uci.ics.jung.utils.UserData.SHARED);
          DSG.addEdge(DSE);
        }
      }
      edu.uci.ics.jung.visualization.SpringLayout SL = new edu.uci.ics.jung.visualization.SpringLayout(DSG);
      SettableRenderer SR = new SettableRenderer(
              edu.uci.ics.jung.graph.decorators.StringLabeller.getLabeller(DSG));
      edu.uci.ics.jung.visualization.VisualizationViewer VV = new edu.uci.ics.jung.visualization.VisualizationViewer(SL, SR);
      SL.advancePositions();
      SR.setEdgeThicknessFunction(new EdgeThicknessFunction() {

        public int getEdgeThickness(edu.uci.ics.jung.graph.Edge e) {
          int retval = (int) Math.abs(((Double) e.getUserDatum(key)).doubleValue());
          if (retval > 10) {
            return 10;
          }
          return retval;
        }
      });
      SR.setEdgeColorFunction(new EdgeColorFunction() {

        public Color getEdgeColor(edu.uci.ics.jung.graph.Edge e) {
          float c = ((Double) e.getUserDatum(key)).floatValue();
          if (c <= -1.0f) {
            return new java.awt.Color(1.0f, 0.0f, 0.0f);
          } else if (c < 0.0f) {
            return new java.awt.Color(-c, 0.0f, 0.0f);
          } else if (c < 1.0f) {
            return new java.awt.Color(0.0f, 0.0f, c);
          }
          return new java.awt.Color(0.0f, 0.0f, 1.0f);
        }
      });
      SR.setVertexBGColor(new java.awt.Color(1.0f, 1.0f, 1.0f));
      SR.setVertexForegroundColor(new java.awt.Color(0.0f, 0.0f, 0.0f));
      for (int i = 0; i < v.length; i++) {
        String name;
        StringBuffer SB = new StringBuffer();
        //if (i >= (firstInput + numInputs)) {
          name = Integer.toString(i);
        //} else {
        //  name = names[i];
        //}
        SB.append(nodes[i].getBias());
        try {
          if (SB.length() == 3) {
            edu.uci.ics.jung.graph.decorators.StringLabeller.getLabeller(DSG).setLabel(
                    v[i], name + ":0");
          } else if (nodes[i].getBias() < -0.05) {
            edu.uci.ics.jung.graph.decorators.StringLabeller.getLabeller(DSG).setLabel(
                    v[i], name + ":" + SB.substring(0, (SB.length() < 5 ? SB.length() : 5)));
          } else {
            edu.uci.ics.jung.graph.decorators.StringLabeller.getLabeller(DSG).setLabel(
                    v[i], name + ":" + SB.substring(0, (SB.length() < 4 ? SB.length() : 4)));
          }
        } catch (Exception e) {
          System.out.println(e);
        }
      }
      for (int i = 0; (i < numOutputs) && (i < v.length); i++) {
        SL.forceMove(v[i], i * 900 / (numOutputs - 1) + 25, (int) (630 + 60 * Math.sin(i * Math.PI / (numOutputs - 1))));
        SL.lockVertex(v[i]);
      }
      for (int i = numOutputs; (i < numInputs + numOutputs) && (i < v.length); i++) {
        SL.forceMove(v[i], (i - numOutputs) * 900 / (numInputs - 1) + 25, 100 - (int) (90 * Math.sin((i - numOutputs) * Math.PI / (numInputs - 1))));
        SL.lockVertex(v[i]);
      }
      for (int i = numOutputs + numInputs; i < v.length; i++) {
        if ((v[i].getOutEdges().size() == 0)
                || (v[i].getInEdges().size() == 0)) {
          DSG.removeVertex(v[i]);
        }
      }
      javax.swing.JFrame f = new javax.swing.JFrame();
      f.getContentPane().add(VV);
      VV.setBackground(new java.awt.Color(0.0f, 0.0f, 0.0f));
      f.pack();
      f.show();
    }
    return "drawing";
  }

  public int findNumber(uchicago.src.sim.network.Node n) {
    for (int i = 0; i < nodes.length; i++) {
      if (nodes[i] == n) {
        return i;
      }
    }
    return -1;
  }

  public String getNetworkConnections() {
    StringBuffer os = new StringBuffer();
    DefaultEdge edge;
    java.util.Iterator iter;
    java.util.ArrayList al;
    int search;

    for (int i = 0; i < nodes.length; i++) {
      os.append(i + " (bias=" + nodes[i].getBias() + "): ");

      al = nodes[i].getOutEdges();

      iter = al.iterator();
      for (; iter.hasNext();) {
        edge = (DefaultEdge) iter.next();
        for (search = 0; search < nodes.length; search++) {
          if (((Object) nodes[search]) == ((Object) edge.getTo())) {
            break;
          }
        }
        os.append(search + "(" + edge.getStrength() + ")  ");
      }
      os.append("\n");
    }
    return os.toString();
  }

  public String getInhibitoryNetworkConnections() {
    StringBuffer os = new StringBuffer();
    DefaultEdge edge;
    java.util.Iterator iter;
    java.util.ArrayList al;
    int search;

    for (int i = 0; i < nodes.length; i++) {
      os.append(i + ": ");

      al = nodes[i].getOutInhibitoryEdges();

      iter = al.iterator();
      for (; iter.hasNext();) {
        edge = (DefaultEdge) iter.next();
        for (search = 0; search < nodes.length; search++) {
          if (((Object) nodes[search]) == ((Object) edge.getTo())) {
            break;
          }
        }
        os.append(search + "(" + edge.getStrength() + ")  ");
      }
      os.append("\n");
    }
    return os.toString();
  }

  public String getMixedNetworkConnections() {
    StringBuffer os = new StringBuffer();
    DefaultEdge edge;
    java.util.Iterator iter;
    java.util.ArrayList al;
    int search;

    for (int i = 0; i < nodes.length; i++) {
      os.append(i + " (bias=" + nodes[i].getBias() + "): ");



















      al = nodes[i].getOutEdges();
      iter = al.iterator();
      for (; iter.hasNext();) {
        edge = (DefaultEdge) iter.next();
        for (search = 0; search < nodes.length; search++) {
          if (((Object) nodes[search]) == ((Object) edge.getTo())) {
            break;
          }
        }
        os.append(search + "(" + edge.getStrength() + ")  ");
      }


















      os.append("  --  ");

      al = nodes[i].getOutInhibitoryEdges();
      iter = al.iterator();
      for (; iter.hasNext();) {
        edge = (DefaultEdge) iter.next();
        for (search = 0; search < nodes.length; search++) {
          if (((Object) nodes[search]) == ((Object) edge.getTo())) {
            break;
          }
        }
        os.append("i" + search + "(" + edge.getStrength() + ")  ");
      }
      os.append("\n");
    }



    return os.toString();
  }
}
