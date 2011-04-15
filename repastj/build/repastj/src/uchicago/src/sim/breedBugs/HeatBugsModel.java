/*$$
 * packages uchicago.src.*
 * Copyright (c) 1999, Trustees of the University of Chicago
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following
 * conditions are met:
 *
 *	 Redistributions of source code must retain the above copyright notice,
 *	 this list of conditions and the following disclaimer.
 *
 *	 Redistributions in binary form must reproduce the above copyright notice,
 *	 this list of conditions and the following disclaimer in the documentation
 *	 and/or other materials provided with the distribution.
 *
 *	 Neither the name of the University of Chicago nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Nick Collier
 * nick@src.uchicago.edu
 *
 * packages cern.jet.random.*
 * Copyright (c) 1999 CERN - European Laboratory for Particle
 * Physics. Permission to use, copy, modify, distribute and sell this
 * software and its documentation for any purpose is hereby granted without
 * fee, provided that the above copyright notice appear in all copies
 * and that both that copyright notice and this permission notice appear in
 * supporting documentation. CERN makes no representations about the
 * suitability of this software for any purpose. It is provided "as is"
 * without expressed or implied warranty.
 *
 * Wolfgang Hoschek
 * wolfgang.hoschek@cern.ch
 *$$*/
package uchicago.src.sim.breedBugs;

import cern.jet.random.Uniform;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.event.SliderListener;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.analysis.*;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.BatchController;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.network.*;
import uchicago.src.sim.graphgrammar.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.Dimension;

/**
 * A translation of the Swarm example simulation Heat Bugs. The heat bugs
 * simulation consists of heat bugs - simple agents that absorb and expel heat
 * and a heatspace which diffuses this heat into the area surrounding the
 * bug. Heat bugs have an ideal temperature and will move about the space
 * in attempt to achieve this idea temperature.
 *
 * @author Swarm Project and Nick Collier
 * @version $Revision: 1.10 $ $Date: 2002/12/05 18:02:07 $
 */

public class HeatBugsModel extends SimModelImpl {

    class DoubleLineSource implements DataSource {
        public Object execute() {
            if (schedule.getCurrentTime() == EndOfSim)
                return new String("\n\n\n");
            else
                return new String();
        }

    }

    class StructureStatSource implements DataSource {
        HeatBugsModel h;
        public StructureStatSource(HeatBugsModel h) {
            this.h = h;
        }
        
        public Object execute() {
            return h.getStructureStats();
        }

    }
    
    static  uchicago.src.sim.engine.SimInit init = new uchicago.src.sim.engine.SimInit();
    private int EndOfSim = 50000;
    private int numBugs = 500;
    private double evapRate = 0.999;
    private double diffusionConstant = 0.1;
    private int worldXSize = 190;
    private int worldYSize = 120;
    private int minIdealTemp = 15000;
    private int maxIdealTemp = 15000;
    private int minOutputHeat = 15000;
    private int maxOutputHeat = 15000;
    private int initialGenes = 8192;
    public static double ReapAtAge = 1000.0;
    public static double BreedAtAge = ReapAtAge / 5.0;
    private float randomMoveProbability = 0.0f;
    private int RuleType = 1;
    private int MinNodes = 15;
    private double SquashingFactor = 5000.0;
    private int numPreviousStructures = 10;
    private int[] PreviousStructures = new int[100];
    private int pauseVal = -1;
    private Schedule schedule;
    private ArrayList heatBugList = new ArrayList();
    private Object2DTorus world;
    private HeatSpace space;
    private DataRecorder recorder;
    private int iteration;
    private DisplaySurface dsurf;
    private java.util.ArrayList BestList;
    private int NumBest = 10;
    private int BestListUpdateStep = -1;

    private int NumHeatSpots = 0;
    private int SizeHeatSpots = 4;
    private int OutputHeatSpots = (int)space.MAX/8;
    private double DurationHeatSpots = 200.0;
    private Dimension HeatSpots[];
    
    public HeatBugsModel() {
        iteration = 0;

    }
    
    private void buildModel() {
        java.util.BitSet Genotype;
        
        space = new HeatSpace(diffusionConstant, evapRate, worldXSize, worldYSize);
        world = new Object2DTorus(space.getSizeX(), space.getSizeY());
    }
    
    private void buildDisplay() {
        
        Object2DDisplay agentDisplay = new Object2DDisplay(world);
        agentDisplay.setObjectList(heatBugList);
        
        // 64 shades of red
        ColorMap map = new ColorMap();
        for (int i = 0; i < 64; i++) {
            //map.mapColor(i, i / 63.0, 0, 0);
            map.mapColor(i, i / 72.0, i / 72.0, i / 72.0);
        }
        
        Value2DDisplay heatDisplay = new Value2DDisplay(space, map);
        heatDisplay.setZeroTransparent(true);
        heatDisplay.setDisplayMapping(512, 0);
        dsurf.addDisplayable(heatDisplay, "Heat Space");
        dsurf.addDisplayableProbeable(agentDisplay, "Bugs");
        //    dsurf.setSnapshotFileName("./heatBugPic");
        
        // UNCOMMENT BELOW TO CREATE A MOVIE
        //dsurf.setMovieName("./HeatBugMovie", DisplaySurface.QUICK_TIME);
        
        addSimEventListener(dsurf);
    }
    
    public void step() {
        java.util.ArrayList ActionList = new java.util.ArrayList();
        Object[] StepAction;
        
        for (;10*heatBugList.size() < 9*numBugs ;)
            addNewBug();
        
        updateHeatSpots();
        for (int i = 0; i < heatBugList.size(); i++) {
            HeatBug bug = (HeatBug) heatBugList.get(i);
            if ((StepAction = bug.step()) != null) {
                ActionList.add(StepAction);
            }
        }
        space.update();
        space.diffuse();

        if ((schedule.getCurrentTime() > 100)){// && (schedule.getCurrentTime() % 10) == 0) {
            ReapAndBreed(ActionList);
        }

        if (dsurf.getFrame().getExtendedState() != java.awt.Frame.ICONIFIED)
            dsurf.updateDisplay();
        if ((dsurf.getFrame().getExtendedState() != java.awt.Frame.ICONIFIED) && 
             (Math.round(schedule.getCurrentTime()) < 10) )
            dsurf.getFrame().setExtendedState(java.awt.Frame.ICONIFIED);

        if (Math.round(schedule.getCurrentTime()) % 100 == 0) {
            System.out.println();
            System.out.println(
                   ((this.getController().isBatch())?((BatchController)this.getController()).getBatchCount():0l)+
                   ":"+schedule.getCurrentTime());
            System.out.println("\tAverageNumberOfNodes: " +
                    getBestAverageNumberOfNodes() + "/" +
                getAverageNumberOfNodes());
            //System.out.println("\tAverageSizeOfGenome: "+getAverageSizeOfGenome());
            System.out.println("\tFitness: " + 
                    getBestUnhappiness()+" : " + 
                    getAverageUnhappiness(0.75)+" : " +
                    getWorstUnhappiness());
            System.out.println("\tNumberOfMovers: "+ getNumMovers() + " out of " + heatBugList.size());
            /*System.out.println("\tConnections: " + 
                    getBestAverageNumberOfConnections() + "/" +
                    getAverageNumberOfConnections() + " : " +
                    getBestAverageNumberOfInhibitoryConnections() + "/" +
                    getAverageNumberOfInhibitoryConnections()); */
            System.out.println("\tConnection Ratio: " + 
                    getBestAverageConnectionRatio() + "/" +
                    getAverageConnectionRatio() + " : " +
                    getBestAverageInhibitoryConnectionRatio() +"/" +
                    getAverageInhibitoryConnectionRatio());
            System.out.println("\tBias Ratio: " + 
                    getBestAverageBiasRatio() + "/" +
                    getAverageBiasRatio());
            System.out.println("\tNatOrd / WeightL / BiasIden / IOClean: " + getNaturalOrdering() + " " +
                getWeightLearning() + " " + getBiasOnIdentity() + " " +getIOClean());
            System.out.println("\tnumPreviousStructures: " + getNumPreviousStructures());
            System.out.println("\tStructure Stats: " + getBestStructureRatio()+" : "+getStructureStats());
            System.runFinalization();
            System.gc();
            System.out.println("\tMemory: (free, used, max) - "
                            +Runtime.getRuntime().getRuntime().freeMemory()/1024/1024+" : "
                            +Runtime.getRuntime().totalMemory()/1024/1024+" : "
                            +Runtime.getRuntime().maxMemory()/1024/1024);
        }
    }
    
    private void buildSchedule() {
        recorder = new DataRecorder("./nnHeatBugs.txt", this);
        recorder.setDelimiter(" \t");
        recorder.createNumericDataSource("AverageNumberOfNodes", this, "getAverageNumberOfNodes",3,3);
        recorder.createNumericDataSource("AverageSizeOfGenome", this, "getAverageSizeOfGenome",4,3);
        recorder.createNumericDataSource("AverageFitness", this, "getAverageUnhappiness",2,4);
        recorder.createNumericDataSource("BestFitness", this, "getBestUnhappiness",2,4);
        recorder.createNumericDataSource("NumberOfMovers", this, "getNumMovers",4,0);
        recorder.createNumericDataSource("NumberOfBreeders", this, "getNumBreeders",4,0);
        recorder.createNumericDataSource("AverageNumberOfConnections", this, "getAverageNumberOfConnections",4,4);
        recorder.createNumericDataSource("AverageNumberOfInhibitoryConnections", this, "getAverageNumberOfInhibitoryConnections",4,4);
        recorder.createNumericDataSource("ConnectionRatio", this, "getAverageConnectionRatio",4,4);
        recorder.createNumericDataSource("IConnectionRatio", this, "getAverageInhibitoryConnectionRatio",4,4);
        recorder.createNumericDataSource("SquashingFactor", this, "getSquashingFactor",4,2);
        recorder.addObjectDataSource("Structures", new StructureStatSource(this));
        recorder.addObjectDataSource("doubleline", new DoubleLineSource());

        schedule.scheduleActionBeginning(1, this, "step");
        schedule.scheduleActionAtInterval(10.0,new BasicAction() {public void execute() {
            recorder.record();
            recorder.writeToFile();}});
        //if (this.getController().isBatch())
        //    schedule.scheduleActionAt(EndOfSim, this, "stop", Schedule.LAST);
    }
    
    public void begin() {
        //setRngSeed(1972L);
        buildModel();
        buildDisplay();
        dsurf.display();
        buildSchedule();
        
    }
    
    public void setup() {
        
        if (dsurf != null)
            dsurf.dispose();
        dsurf = null;
        schedule = null;
        System.gc();
        
        dsurf = new DisplaySurface(this, "Heat Bugs Display");
        registerDisplaySurface("Main", dsurf);
        schedule = new Schedule(1);
        
        pauseVal = -1;
        
        heatBugList = new ArrayList();
        world = null;
        space = null;
        setupCustomAction();

        HeatSpots = new Dimension[NumHeatSpots];
        int xinc = 0;
        int yinc = 0;
        if (NumHeatSpots > 0) {
            xinc = worldXSize / (int)Math.ceil(Math.sqrt((double)NumHeatSpots));
            yinc = worldXSize / (int)Math.ceil(Math.sqrt((double)NumHeatSpots));
        }
        for (int i = 0; i <NumHeatSpots; i++){
            HeatSpots[i] = new java.awt.Dimension((i%3)*xinc, 
                                                    (i/3)*yinc);
        }
    }
    public String[] getInitParam() {
        String[] params = {"evapRate", "diffusionConstant", 
        "minIdealTemp", "minOutputHeat", "worldXSize",
        "worldYSize", "numBugs", "Pause", "Model","RuleType","SquashingFactor",
        "NaturalOrdering", "WeightLearning","BiasOnIdentity","IOClean",
        "NumPreviousStructures","InitialGenes"};
        return params;
    }
    
    public Schedule getSchedule() {
        return schedule;
    }
    
    public String getName() {
        return "HeatBugs";
    }
    
    private void setupCustomAction() {
        
        modelManipulator.init();
        
        // this adds a button to the Custom Action tab that
        // will set the heat space to 0 heat when clicked
        modelManipulator.addButton("Deep Freeze", new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                for (int i = 0; i < space.getSizeX(); i++) {
                    for (int j = 0; j < space.getSizeY(); j++) {
                        space.putValueAt(i, j, 0);
                    }
                }
                space.update();
            }
        });
        
        // this will add a slider to the Custom Action tab that will
        // increment the output heat of each bug by a the slider amount.
        // The code doesn't really work when trying to decrement the heat
        // by sliding the slider to the left. But it is a good example
        // of what you can do with a slider.
        modelManipulator.addSlider("Increment Heat", 0, 100, 10, new SliderListener() {
            public void execute() {
                for (int i = 0; i < heatBugList.size(); i++) {
                    HeatBug bug = (HeatBug) heatBugList.get(i);
                    int outputHeat = bug.getOutputHeat();
                    if (isSlidingLeft && !isAdjusting) {
                        outputHeat -= outputHeat * value * .01;
                    } else if (!isSlidingLeft && !isAdjusting) {
                        outputHeat += outputHeat * value * .01;
                    }
                    bug.setOutputHeat(outputHeat);
                }
            }
        });
    }
    
    private void ReapAndBreed(java.util.ArrayList ActionList){
        int i,j;
        int x,y,x2,y2,x3,y3;
        HeatBug hb, mate1, mate2,newhb1, newhb2;
        java.util.BitSet g1,g2;
        Object [] tO;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            hb = (HeatBug)heatBugList.get(i);
            if ( ((10*heatBugList.size() > 9*numBugs) && (hb.getAge() > ReapAtAge)) || 
                 ((hb.getAge() > BreedAtAge) && !hb.getHasMoved() && (hb.matings <0))){
                world.putObjectAt(hb.getX(),hb.getY(),null);
                if (heatBugList.remove(i) == null)
                    System.out.println("HeatBugsModel: removal failure @ 1");
            }
        }
        for (i = 0; (i < ActionList.size()) && (heatBugList.size()<=numBugs); i++) {
            tO = (Object[])ActionList.get(i);
            if (tO[0] == null) {
                ;
            } else if ( ((Integer)tO[0]).intValue() == HeatBug.ACTION_FIGHT){
                mate2 = (HeatBug)tO[2];
                if ((mate2.getAction() != HeatBug.ACTION_FIGHT) && (heatBugList.contains(mate2))) {
                    world.putObjectAt(mate2.getX(), mate2.getY(), null);
                    if (!heatBugList.remove(mate2))
                            System.out.println("HeatBugsModel: removal failed @ 2");
                }
            } else if (((Integer)tO[0]).intValue() == HeatBug.ACTION_BREED) {
                mate1 = (HeatBug)tO[1];
                mate2 = (HeatBug)tO[2];
                do {
                    x = Uniform.staticNextIntFromTo(0, world.getSizeX()-1);
                    y = Uniform.staticNextIntFromTo(0, world.getSizeY()-1);
                    //x = SimUtilities.norm(mate1.getX() + Uniform.staticNextIntFromTo(-10,10), world.getSizeX());
                    //y = SimUtilities.norm(mate1.getY() + Uniform.staticNextIntFromTo(-10,10), world.getSizeY());
                } while ((world.getObjectAt(x, y) != null) );
                
                do {
                    x2 = Uniform.staticNextIntFromTo(0, world.getSizeX()-1);
                    y2 = Uniform.staticNextIntFromTo(0, world.getSizeY()-1);
                    //x2 = SimUtilities.norm(mate2.getX() + Uniform.staticNextIntFromTo(-10,10), world.getSizeX());
                    //y2 = SimUtilities.norm(mate2.getY() + Uniform.staticNextIntFromTo(-10,10), world.getSizeY());
                } while ((world.getObjectAt(x2, y2) != null) || ((x==x2) && (y==y2)) );

                do {
                    x3 = Uniform.staticNextIntFromTo(0, world.getSizeX()-1);
                    y3 = Uniform.staticNextIntFromTo(0, world.getSizeY()-1);
                    //x2 = SimUtilities.norm(mate2.getX() + Uniform.staticNextIntFromTo(-10,10), world.getSizeX());
                    //y2 = SimUtilities.norm(mate2.getY() + Uniform.staticNextIntFromTo(-10,10), world.getSizeY());
                } while ((world.getObjectAt(x3, y3) != null) || ((x==x3) && (y==y3)) || ((x2==x3) && (y2==y3)) );

                g1 = crossover(mate1.getGenotype(), mate2.getGenotype());
                mate1.setMatings();
                addPreviousStructure(mate1.getStructure());
                newhb1 = new DirectionalHeatBug(this, space, world, x ,  y, minIdealTemp, minOutputHeat, g1, mate1.RuleType);
                newhb1.buildNetwork();
                world.putObjectAt(x , y,newhb1);
                heatBugList.add(newhb1);

                g2 = crossover(mate2.getGenotype(), mate1.getGenotype());
                mate2.setMatings();
                addPreviousStructure(mate2.getStructure());
                newhb2 = new DirectionalHeatBug(this, space, world, x2 ,  y2, minIdealTemp, minOutputHeat, g2, mate2.RuleType);
                newhb2.buildNetwork();
                world.putObjectAt(x2 , y2,newhb2);
                heatBugList.add(newhb2);

                // move the destination mate to discourage bug buildups.
                world.putObjectAt(mate2.getX(), mate2.getY(), null);
                world.putObjectAt(x3, y3, mate2);
                mate2.setX(x3);
                mate2.setY(y3);

            }
        }
        
    }
    
    public java.util.BitSet randomGenotype(int ig){
        java.util.BitSet Genotype = new java.util.BitSet(initialGenes+1);
        
        for (int j = 0; j < initialGenes; j++)
            if (Uniform.staticNextIntFromTo(0,1) == 1)
                Genotype.set(j);
        Genotype.set(initialGenes);
        return Genotype;
    }

    // properties
    public int getNumBugs() {
        return numBugs;
    }
    
    public void setNumBugs(int numBugs) {
        this.numBugs = numBugs;
    }
    
    public double getEvapRate() {
        return evapRate;
    }
    public void setNaturalOrdering(boolean b) {
        Matrix.setNaturalOrdering(b);
    }
    public int getInitialGenes() {
        return this.initialGenes;
    }
    
    public void setInitialGenes(int i) {
        this.initialGenes = i;
    }

    public boolean getNaturalOrdering() {
        return Matrix.getNaturalOrdering();
    }

    public double getDoubleNaturalOrdering(){
        return (Matrix.getNaturalOrdering() ? 1.0 : 0.0);
    }

    public void setWeightLearning(boolean b) {
        NeuralNetworkNode.setWeightLearning(b);
    }

    public boolean getWeightLearning() {
        return NeuralNetworkNode.getWeightLearning();
    }

    public double getDoubleWeightLearning(){
        return (NeuralNetworkNode.getWeightLearning() ? 1.0 : 0.0);
    }
    
    public void setBiasOnIdentity(boolean b) {
        HeatBug.setBiasOnIdentity(b);
    }

    public boolean getBiasOnIdentity() {
        return HeatBug.getBiasOnIdentity();
    }

    public double getDoubleBiasOnIdentity(){
        return (HeatBug.getBiasOnIdentity() ? 1.0 : 0.0);
    }

    public void setIOClean(boolean b) {
        HeatBug.setIOClean(b);
    }

    public boolean getIOClean() {
        return HeatBug.getIOClean();
    }

    public double getDoubleIOClean(){
        return (HeatBug.getIOClean() ? 1.0 : 0.0);
    }
    
    public void setNumPreviousStructures(double val) {
        this.numPreviousStructures = Math.round((float) val);
    }

    public double getNumPreviousStructures() {
        return this.numPreviousStructures;
    }

    public void setSquashingFactor(double val) {
        SquashingFactor = val;
    }

    public double getSquashingFactor() {
        return SquashingFactor;
    }


    public void setEvapRate(double rate) {
        evapRate = rate;
    }
    
    public double getDiffusionConstant() {
        return diffusionConstant;
    }
    
    public void setDiffusionConstant(double constant) {
        diffusionConstant = constant;
    }
    
    public int getWorldXSize() {
        return worldXSize;
    }
    
    public void setWorldXSize(int size) {
        worldXSize = size;
    }
    public DisplaySurface getDisplaySurface() {
        return dsurf;
    }
    public int getWorldYSize() {
        return worldYSize;
    }
    
    public void setWorldYSize(int size) {
        worldYSize = size;
    }
    
    public int getMinIdealTemp() {
        return minIdealTemp;
    }


    public double getNumRuleType1() {
        int i;
        int sum = 0;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            if (((HeatBug)heatBugList.get(i)).RuleType == 1)
                sum++;
        }
        return (double)sum;
    }

    public double getNumBreeders() {
        int i;
        int sum = 0;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            if (((HeatBug)heatBugList.get(i)).canBreed(BreedAtAge))
                sum++;
        }
        return (double)sum;
    }

    public String getStructureStats() {
        int i,j;
        int temp;
        Integer tempkey;
        int highest = 1;
        double best=0.0;
        java.util.TreeMap tm = new java.util.TreeMap();
        StringBuffer retStringBuffer = new StringBuffer();

        
        for (i = 0; i < heatBugList.size(); i ++) {
            if (tm.containsKey(new Integer(((HeatBug)heatBugList.get(i)).getStructure()))) {
                temp = ((Integer)tm.get(
                    new Integer(((HeatBug)heatBugList.get(i)).getStructure()))).intValue();
                temp++;
                tm.put(new Integer(((HeatBug)heatBugList.get(i)).getStructure()), new Integer(temp));
                if (temp > highest)
                    highest = temp;
            } else
                tm.put(new Integer(((HeatBug)heatBugList.get(i)).getStructure()),new Integer(1));
        }

        j = 0;
        for (; (j < 20) && (highest > 0); ) {
            java.util.Iterator iter = tm.keySet().iterator();
            for (; iter.hasNext() && (j<20); ) {
                tempkey = (Integer)iter.next();
                temp = ((Integer)tm.get(tempkey)).intValue();
                if (temp == highest) {
                    retStringBuffer.append(tempkey.toString()+" ("+temp+")  ");
                    j++;
                }
            }
            highest--;
        }
        
        return retStringBuffer.toString();
    }
    public double getBestUnhappiness() {
        return getAverageUnhappiness(0.05);
    }
    public double getAverageUnhappiness() {
        return getAverageUnhappiness(0.5);
    }
    public double getAverageUnhappiness(double percentage) {
        int i;
        double sum = 0.0;
        java.util.TreeMap tm = new java.util.TreeMap();

        for (i = 0; i < heatBugList.size(); i ++) {
            if ( ((DirectionalHeatBug)heatBugList.get(i)).getHasMoved())
                tm.put(new Double(((DirectionalHeatBug)heatBugList.get(i)).getTotalUnhappiness()),
                    heatBugList.get(i));
        }

        java.util.Iterator iter = tm.values().iterator();
        for (i = 0; (i < (percentage*numBugs)) && iter.hasNext(); i++)
                sum += ((DirectionalHeatBug)iter.next()).getTotalUnhappiness();

        return sum/i;
    }
    public double getNumMovers() {
        int i;
        int sum = 0;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            if (((DirectionalHeatBug)heatBugList.get(i)).getHasMoved())
                sum++;
        }
        return (double)sum;
    }
    public double getBestAverageNumberOfNodes() {
        int i;
        double sum=0.0;

        fillBestList();

        java.util.Iterator iter = BestList.iterator();
        for (; iter.hasNext(); )
            sum += ((DirectionalHeatBug)iter.next()).getNumberOfNodes();

        return sum/BestList.size();
    }
    public double getBestAverageNumberOfConnections() {
        int i;
        double sum=0.0;
        
        fillBestList();

        java.util.Iterator iter = BestList.iterator();
        for (; iter.hasNext(); )
            sum += ((DirectionalHeatBug)iter.next()).getNumConnections();
        
        return sum/BestList.size();
    }
    public double getBestAverageBiasRatio() {
        int i;
        double sum=0.0;
        
        fillBestList();

        java.util.Iterator iter = BestList.iterator();
        for (; iter.hasNext(); )
            sum += ((DirectionalHeatBug)iter.next()).getBiasRatio();
        return sum/BestList.size();
    }
    public double getBestAverageConnectionRatio() {
        int i;
        double sum=0.0;
        
        fillBestList();

        java.util.Iterator iter = BestList.iterator();
        for (; iter.hasNext(); )
            sum += ((DirectionalHeatBug)iter.next()).getConnectionsRatio();
        return sum/BestList.size();
    }
    public double getBestAverageInhibitoryConnectionRatio() {
        int i;
        double sum=0.0;
        
        fillBestList();

        java.util.Iterator iter = BestList.iterator();
        for (; iter.hasNext(); )
            sum += ((DirectionalHeatBug)iter.next()).getInhibitoryConnectionsRatio();
        return sum/BestList.size();
    }
    public double getBestAverageNumberOfInhibitoryConnections() {
        int i;
        double sum=0.0;
        
        fillBestList();

        java.util.Iterator iter = BestList.iterator();
        for (; iter.hasNext(); )
            sum += ((DirectionalHeatBug)iter.next()).getNumInhibitoryConnections();
        return sum/BestList.size();
    }
    public void fillBestList() {
        int i;
        double best=0.0;
        java.util.TreeMap tm = new java.util.TreeMap();

        if (BestList == null)
            BestList = new java.util.ArrayList();
        if (BestListUpdateStep == schedule.getCurrentTime())
            return;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            if ( ((DirectionalHeatBug)heatBugList.get(i)).getHasMoved())
                tm.put(new Double(((DirectionalHeatBug)heatBugList.get(i)).getTotalUnhappiness()),
                    heatBugList.get(i));
        }

        BestList.clear();
        java.util.Iterator iter = tm.values().iterator();
        for (i = 0; (i < NumBest) && iter.hasNext(); i++)
            BestList.add(iter.next());

        BestListUpdateStep = (int)schedule.getCurrentTime();
    }
    public double getWorstUnhappiness() {
        int i;
        double worst=-1000.0;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            if ( (((DirectionalHeatBug)heatBugList.get(i)).getTotalUnhappiness() > worst) && 
                  (((DirectionalHeatBug)heatBugList.get(i)).getHasMoved()) ) 
                worst = ((DirectionalHeatBug)heatBugList.get(i)).getTotalUnhappiness();
        }
        return worst;
    }
    public double getAverageNumberOfNodes() {
        int i;
        double sum=0.0;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            sum += ((DirectionalHeatBug)heatBugList.get(i)).getNumberOfNodes();
        }
        return sum/heatBugList.size();
    }
    public double getAverageNumberOfConnections() {
        int i;
        double sum=0.0;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            sum += ((DirectionalHeatBug)heatBugList.get(i)).getNumConnections();
        }
        return sum/heatBugList.size();
    }
    public double getAverageBiasRatio() {
        int i;
        double sum=0.0;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            sum += ((DirectionalHeatBug)heatBugList.get(i)).getBiasRatio();
        }
        return sum/heatBugList.size();
    }
    public double getAverageConnectionRatio() {
        int i;
        double sum=0.0;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            sum += ((DirectionalHeatBug)heatBugList.get(i)).getConnectionsRatio();
        }
        return sum/heatBugList.size();
    }
    public double getAverageInhibitoryConnectionRatio() {
        int i;
        double sum=0.0;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            sum += ((DirectionalHeatBug)heatBugList.get(i)).getInhibitoryConnectionsRatio();
        }
        return sum/heatBugList.size();
    }
    public double getAverageNumberOfInhibitoryConnections() {
        int i;
        double sum=0.0;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            sum += ((DirectionalHeatBug)heatBugList.get(i)).getNumInhibitoryConnections();
        }
        return sum/heatBugList.size();
    }
    public double getAverageSizeOfGenome() {
        int i;
        double sum=0.0;
        
        for (i = 0; i < heatBugList.size(); i ++) {
            sum += ((DirectionalHeatBug)heatBugList.get(i)).getGenotype().length();
        }
        return sum/heatBugList.size();
    }
    public double getBestStructureRatio() {
        int i,j;
        int temp;
        Integer tempkey;
        int highest = 1;
        double best=0.0;
        java.util.TreeMap tm = new java.util.TreeMap();

        
        for (i = 0; i < heatBugList.size(); i ++) {
            if (tm.containsKey(new Integer(((DirectionalHeatBug)heatBugList.get(i)).getStructure()))) {
                temp = ((Integer)tm.get(
                    new Integer(((DirectionalHeatBug)heatBugList.get(i)).getStructure()))).intValue();
                temp++;
                tm.put(new Integer(((DirectionalHeatBug)heatBugList.get(i)).getStructure()), new Integer(temp));
                if (temp > highest)
                    highest = temp;
            } else
                tm.put(new Integer(((DirectionalHeatBug)heatBugList.get(i)).getStructure()),new Integer(1));
        }
        
        return ((double)highest)/numBugs;
    }

    
    public void setMinIdealTemp(int temp) {
        minIdealTemp = temp;
    }
    
    public int getMaxIdealTemp() {
        return maxIdealTemp;
    }
    
    public void setMaxIdealTemp(int temp) {
        maxIdealTemp = temp;
    }
    
    public int getMinOutputHeat() {
        return minOutputHeat;
    }
    
    public void setMinOutputHeat(int heat) {
        minOutputHeat = heat;
    }
    
    public int getMaxOutputHeat() {
        return maxOutputHeat;
    }
    
    public void setMaxOutputHeat(int heat) {
        maxOutputHeat = heat;
    }
    
    public float getRandomMoveProbability() {
        return randomMoveProbability;
    }
    
    public void setRandomMoveProbability(float prob) {
        randomMoveProbability = prob;
    }
    
    public int getRuleType(){
        return RuleType;
    }

    public void setRuleType(int i){
        RuleType = i;
    }
    
    private java.util.BitSet crossover(java.util.BitSet g1, java.util.BitSet g2){
        //return splitCrossover(g1,g2);
        return interleavedRuleCrossover(g1, g2);
    }

    private java.util.BitSet splitCrossover(java.util.BitSet g1, java.util.BitSet g2){
        double Crossover_StartBitProbability  = 0.05;
        double Crossover_MiddleBitProbability = 0.05;
        double Crossover_EndBitProbability    = 0.05;
        double Crossover_RemoveBitProbability = 0.25;
        double Crossover_MutateBitProbability = 0.25;
        int CrossoverPoint;
        
        if (g1.length() > g2.length())
            CrossoverPoint = Uniform.staticNextIntFromTo(1,g2.length() - 1); //ignore marker
        else
            CrossoverPoint = Uniform.staticNextIntFromTo(1,g1.length() - 1); //ignore marker
        
        int SourceBit = 0;
        int DestBit = 0;
        int loop;
        java.util.BitSet NewGenotype = new java.util.BitSet();
        if (Uniform.staticNextDoubleFromTo(0,1.0) < Crossover_StartBitProbability) {
            if (Uniform.staticNextBoolean() == true){
                NewGenotype.set(DestBit);
                ++DestBit;
            } else {
                NewGenotype.clear(DestBit);
                ++DestBit;
            }
        }
        for (loop = 0; loop < CrossoverPoint; loop++) {
            if (g1.get(SourceBit) == true)
                NewGenotype.set(DestBit);
            else
                NewGenotype.clear(DestBit);
            ++DestBit;
            ++SourceBit;
        }
        if (Uniform.staticNextDoubleFromTo(0,1.0) < Crossover_MiddleBitProbability) {
            if (Uniform.staticNextBoolean() == true){
                NewGenotype.set(DestBit);
                ++DestBit;
            } else {
                NewGenotype.clear(DestBit);
                ++DestBit;
            }
        }
        
        for (loop = SourceBit; loop < g2.length() - 1; loop++) {
            if (g2.get(SourceBit) == true)
                NewGenotype.set(DestBit);
            else
                NewGenotype.clear(DestBit);
            ++DestBit; ++SourceBit;
        }
        if (Uniform.staticNextDouble() < Crossover_EndBitProbability) {
            if (Uniform.staticNextBoolean() == true){
                NewGenotype.set(DestBit);
                ++DestBit;
            } else {
                NewGenotype.clear(DestBit);
                ++DestBit;
            }
        }
        
        NewGenotype.set(DestBit);
        
        if (Uniform.staticNextDouble() < Crossover_MutateBitProbability) {
            NewGenotype.flip(Uniform.staticNextIntFromTo(0,NewGenotype.length()-1) );
        }
        return NewGenotype;
    }

    private java.util.BitSet interleavedRuleCrossover(java.util.BitSet g1, java.util.BitSet g2){
        double Crossover_MutateRuleProbability = 0.05;
        double Crossover_MutateBitProbability = 0.25;
        int CrossoverPoint;
        int SourceBit = 0;
        int loop, location;
        java.util.BitSet NewGenotype = new java.util.BitSet();
        final int ILength = 8;

        for (loop = 0; loop < g1.length(); loop++) {
            if (((loop % (2*ILength))/ILength == 0) || (loop >= g2.length())) {
                if (g1.get(loop))
                    NewGenotype.set(loop);
                else
                    NewGenotype.clear(loop);
            } else { 
                if (g2.get(loop))
                    NewGenotype.set(loop);
                else
                    NewGenotype.clear(loop);
            }
        }
        NewGenotype.set(g1.length()-1);

        if (Uniform.staticNextDouble() < Crossover_MutateRuleProbability) {
            location = Uniform.staticNextIntFromTo(0,NewGenotype.length()-8)/8;
            for (loop = 0; loop < 8; loop++)
                if (Uniform.staticNextBoolean())
                    NewGenotype.set(loop+location);
                else
                    NewGenotype.clear(loop+location);
        }
        if (Uniform.staticNextDouble() < Crossover_MutateBitProbability) {
            location = Uniform.staticNextIntFromTo(0,NewGenotype.length()-1);
            NewGenotype.flip(location);
        }

        return NewGenotype;
    }
    
    public void addPreviousStructure(int structure){
        if (numPreviousStructures > 0){
            for (int k = 0; k < numPreviousStructures-1; k++) {
                PreviousStructures[k] = PreviousStructures[k+1];
            }
            PreviousStructures[numPreviousStructures - 1] = structure;
        }
    }
    
    public boolean inPreviousStructures(int structure){
        if (numPreviousStructures > 0){
            for (int k = 0; k < numPreviousStructures; k++) {
                if (PreviousStructures[k] == structure) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void updateHeatSpots() {
        SizeHeatSpots = 5+(Math.round(schedule.getCurrentTime()) % 500) / 10;
        if (SizeHeatSpots >= 30)
            SizeHeatSpots = 60 - SizeHeatSpots;
        int Offset = SizeHeatSpots /2;

        for (int i = 0; i < NumHeatSpots; i++){
            for (int j = 1; j < SizeHeatSpots; j++) {
                space.addHeat(SimUtilities.norm(HeatSpots[i].width + j - Offset, worldXSize),
                               SimUtilities.norm(HeatSpots[i].height    - Offset, worldYSize), OutputHeatSpots);
                space.addHeat(SimUtilities.norm(HeatSpots[i].width     - Offset, worldXSize),
                               SimUtilities.norm(HeatSpots[i].height +j - Offset, worldYSize), OutputHeatSpots);
                space.addHeat(SimUtilities.norm(HeatSpots[i].width  + j - Offset, worldXSize),
                               SimUtilities.norm(HeatSpots[i].height + SizeHeatSpots - Offset, worldYSize), OutputHeatSpots);
                space.addHeat(SimUtilities.norm(HeatSpots[i].width  + SizeHeatSpots - Offset, worldXSize),
                               SimUtilities.norm(HeatSpots[i].height +j - Offset, worldYSize), OutputHeatSpots);
            }
        }
        if (NumHeatSpots>0) {
            if (Uniform.staticNextDoubleFromTo(0, 1.0) < (1.0/DurationHeatSpots)){
                HeatSpots[Uniform.staticNextIntFromTo(0, NumHeatSpots-1)] = new Dimension(
                Uniform.staticNextIntFromTo(0, worldXSize),
                Uniform.staticNextIntFromTo(0, worldYSize));
            }
        }
    }

    public void addNewBug(){
        java.util.BitSet Genotype;
        DirectionalHeatBug bug;
        int rt;
        
            int idealTemp = Uniform.staticNextIntFromTo(minIdealTemp, maxIdealTemp);
            int outputHeat = Uniform.staticNextIntFromTo(minOutputHeat, maxOutputHeat);
            int x, y;
            
            do {
                x = Uniform.staticNextIntFromTo(0, space.getSizeX() - 1);
                y = Uniform.staticNextIntFromTo(0, space.getSizeY() - 1);
            } while (world.getObjectAt(x, y) != null);
            
            rt = RuleType;//((Uniform.staticNextIntFromTo(0, 1000) % 2));// * 2) + 1;
            do {
                Genotype = randomGenotype(initialGenes);
                bug = new DirectionalHeatBug((HeatBugsModel)this, space, world, x, y, idealTemp, outputHeat, Genotype, rt);
                bug.buildNetwork();
            } while ( (bug.getNumberOfNodes() < MinNodes) &&
                        (bug.getPercentOfInputsUsed()<0.99) &&
                        (bug.getPercentOfOutputsUsed()<0.99)); 
            world.putObjectAt(x, y, bug);
            heatBugList.add(bug);
            bug.setSquashingFactor(SquashingFactor);
            
            if ((heatBugList.size() % 100 )== 0){
                System.runFinalization();
                System.gc();
                try { 
                    Thread.currentThread().sleep(10l);
                } catch (Exception e) {}
                System.out.println("\tNum bugs: "+ heatBugList.size() + " Nodes:" + getAverageNumberOfNodes());
                System.out.println("\tMemory: (free, used, max) - "
                            +Runtime.getRuntime().getRuntime().freeMemory()/1024/1024+" : "
                            +Runtime.getRuntime().totalMemory()/1024/1024+" : "
                            +Runtime.getRuntime().maxMemory()/1024/1024);
            }
    }
    
    public static void main(String[] args) {

        HeatBugsModel model = new HeatBugsModel();
        if (args.length > 0)
            init.loadModel(model, args[0], true);
        else
            init.loadModel(model, null, false);
    }
}
