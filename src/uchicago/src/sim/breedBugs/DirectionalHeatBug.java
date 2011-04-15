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

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

public class DirectionalHeatBug extends HeatBug {
    protected int heading;
    protected boolean hasTurnedL, hasTurnedR, hasMoved, hasBread, hasFought;
    
    public DirectionalHeatBug(
            HeatBugsModel model, HeatSpace space, Object2DTorus world, 
            int x, int y, int idealTemp, int outputHeat, 
            java.util.BitSet Genotype, int RuleType) {
        super(model,space,world,x,y, idealTemp, outputHeat, Genotype, RuleType);

        heading = Uniform.staticNextIntFromTo(0, 7);
        hasTurnedL = false;
        hasTurnedR = false;
        hasMoved   = false;
        hasHeated  = false;
        hasBread = false;
        hasFought = false;
        numOutputs = 5;
        firstInput = numOutputs;

        numInputs = 9;
        
        names = new String[firstInput+numInputs];
        names[0]  = "l";
        names[1]  = "r";
        names[2]  = "f";
        names[3]  = "B";
        names[4]  = "F";
        names[5]  = "c0";
        names[6]  = "l1";
        names[7]  = "c1";
        names[8]  = "r1";
        names[9]  = "L2";
        names[10] = "l2";
        names[11] = "c2";
        names[12] = "r2";
        names[13] = "R2";
    }
    
    int dh = 0;
    int dd = 0;
        double Output = 0.0;
        double Breed = 0.0;
        double Fight = 0.0;
        Object[] tO = {null, null, null};

    public Object[] step() {
        int i;
        int n;
        int d;

        Age += 1.0;
        tO[0]=null;tO[1]=null;tO[2]=null;
        
        collectExternalInputs();
        for (i = 0; i < nodes.length; i++)
            nodes[i].collectInputs();
        for (i = 0; i < nodes.length; i++)
            nodes[i].calculateOutputs();
        collectOutputs();        
        
        if ((dh != 0)) {
            if ((dh == 1) && (Age>10.0))
                hasTurnedR = true;
            if ((dh == -1) && (Age>10.0))
                hasTurnedL = true;
            heading = (heading + 8 + dh) % 8;
        }

        if (dd != 0) {
            int newX = SimUtilities.norm(x + PolarToRectangular.dx(1,heading), xSize);
            int newY = SimUtilities.norm(y + PolarToRectangular.dy(1,heading), ySize);
            Object temp = world.getObjectAt(newX, newY);
            if ( temp == null) {  //check to see if the new square is empty
                world.putObjectAt(x, y,   null);  //pop off the old square ...
                x = newX; y = newY;
                world.putObjectAt(x, y,   this);  //and land on the new square.
                if (Age>10.0)
                    hasMoved = true;
            } else { //square is occupied
                Age += 10.0;
            }
        }
            
        if (Fight > SigmoidNeuralNetworkNode.getActivationThreshold()) {
            if ((LastAction != ACTION_FIGHT) ) {
                Action = ACTION_FIGHT;
                if ( ((HeatBug)getObjectAtR(1, heading) != null) &&
                      (((HeatBug)getObjectAtR(1, heading)).getAction()!=ACTION_FIGHT) ){
                    tO[0] = new Integer(Action);
                    tO[1] = this;
                    tO[2] = (HeatBug)getObjectAtR(1, heading);
                }
                Age += 5.0;
                hasFought = true;
                LastAction = ACTION_FIGHT;
            } else {
                Action = ACTION_NONE;
                LastAction = ACTION_FIGHT;
            }
        } else if (Breed > SigmoidNeuralNetworkNode.getActivationThreshold()) {
            if ((LastAction != ACTION_BREED) && (canBreed(HeatBugsModel.BreedAtAge)) ) { 
                Action = ACTION_BREED;
                if ( ((HeatBug)getObjectAtR(1, heading) != null) &&
                      (((HeatBug)getObjectAtR(1, heading)).getAction()!=ACTION_FIGHT) &&
                      (((HeatBug)getObjectAtR(1, heading)).RuleType == this.RuleType) ){
                    tO[0] = new Integer(Action);
                    tO[1] = this;
                    tO[2] = (HeatBug)getObjectAtR(1, heading);
                }
                Age += 5.0;
                hasBread = true;
                LastAction = ACTION_BREED;
            } else {
                Action = ACTION_NONE;
                LastAction = ACTION_BREED;
            }
        } else {
            Action = ACTION_NONE;
            LastAction = ACTION_NONE;
        }
        
        // heat our space
            space.setHeat(x, y, outputHeat);
        return tO;
    }
    
    public void collectExternalInputs(){
        int n;
        int d;
        
        //if a square is occupied signal by delivering max heat (disincentive)
        for (n = 0; (n < numInputs) && (nodes.length > (firstInput + n)); n++) {
            if (n == 0){
                nodes[firstInput + n].setExternalInput(getProcessedInput(x,y, SquashingFactor));
            } else if (n <= 3) {
                d = heading + (n-2); //n=1->3
                if (getObjectAtR(1, d) == null)
                    nodes[firstInput + n].setExternalInput(getProcessedInputR(1, d,SquashingFactor));
                else
                    nodes[firstInput + n].setExternalInput(2.0);
            } else {
                d = 2*heading + (n-4-2); //n=7->8
                if (getObjectAtR(2, d) == null)
                    nodes[firstInput + n].setExternalInput(getProcessedInputR(2, d,SquashingFactor));
                else
                    nodes[firstInput + n].setExternalInput(2.0);
            }
        }
    }

   public void collectOutputs() {
        //retrieve outputs and translate them
        // 0: x-1, 1: x+1, 2: y-1, 3: y+1
        if (nodes.length > 0)  dh  = ((nodes[0].getOutput() > SigmoidNeuralNetworkNode.getActivationThreshold()) ? -1 : 0);
        if (nodes.length > 1)  dh += ((nodes[1].getOutput() > SigmoidNeuralNetworkNode.getActivationThreshold()) ? 1 : 0);
        if (nodes.length > 2)  dd  = ((nodes[2].getOutput() > SigmoidNeuralNetworkNode.getActivationThreshold()) ? 1 : 0);
        if (nodes.length > 3)  Breed  =   nodes[3].getOutput();
        if (nodes.length > 4)  Fight  =   nodes[4].getOutput();
    }

    public boolean canBreed(double BreedAtAge) {
        if ((dd!=0)) //can't breed while moving...
            return false;
        if (Age < BreedAtAge)
            return false;
        if (!getHasMoved())
            return false;
        if (getPercentOfOutputsUsed() < 0.75)
            return false;
        if (getPercentOfInputsUsed() < 0.75)
            return false;
        if ((this.Age < (matings  + 100.0) ))
            return false;
        return true;
    }

    public boolean getHasMoved(){
        //return hasMoved && (hasTurnedL && hasTurnedR);
        return hasMoved && (hasTurnedL && hasTurnedR);
        //return hasMoved && (hasTurnedL || hasTurnedR);
        //return hasMoved ;
        //return true;
    }

    public int getHeading() {
        return heading;
    }
}
