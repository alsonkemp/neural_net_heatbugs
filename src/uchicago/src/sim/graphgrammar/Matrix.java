/*
 * Matrix.java
 *
 * Created on December 13, 2002, 5:57 PM
 */

package uchicago.src.sim.graphgrammar;

import uchicago.src.sim.nnheatBugs.StringWindow;
/**
 *
 * @author  Administrator
 */
public class Matrix {
    public static final boolean Overlap_ClipAtEdge  = true;
    public static final boolean Overlap_RuleTarget  = false;
    private static boolean NaturalOrdering = true;
    private static boolean Debug = false;
    public static final int MaxSize = 100;
    public static final int MaxSteps = 100;
    protected static MatrixElementFactory MEF;
    protected static RuleFactory RF;
    private static int RulesApplied;
    private static int RulesAppliedWeight;
    private static int RulesAppliedRuleTarget;
    private static int RulesAppliedBoth;
    private static int RulesAppliedNone;
    private static int RuleType;
    int Width, Height;
    java.util.ArrayList Columns;
    java.util.ArrayList Rows;
    RuleList RL;
    int nextColumn, nextRow;
    
    /** Creates a new instance of Matrix */
    public Matrix(int i) {
        MatrixElement m;
        RuleType = i;
        
        if (MEF == null)
            MEF = new MatrixElementFactory();
        MEF.initialize();
        
        if (RF == null)
            RF = new RuleFactory();
        RF.initialize();

        nextColumn = 0;
        nextRow = 0;
        Columns = new java.util.ArrayList();
        Rows = new java.util.ArrayList();

    }
    public void GenerateMatrix(java.util.BitSet Genotype) {
        int i;
        boolean Changes;
        MatrixElement TempME;
        int TempValue;
        
        Rule TempRule;
        // Rule matrices modify  topleft->bottomright so start on
        // bottom-right to top-left
        Changes = true;

        switch (RuleType){
            case 0:
              RL = new RuleList(MEF, RF);
              break;
            case 1:
              RL = new RuleList_Square(MEF, RF);
              break;
            case 2:
              RL = new RuleList_LargeSquare(MEF, RF);
              break;
            case 3:
              RL = new RuleList_Direct(MEF, RF);
              break;
            case 4:
              RL = new RuleList_Layered(MEF, RF);
              break;
        }
        RL.GenerateRules(Genotype, 0);

        //add and number columns
        nextColumn = 0;
        Columns.add(new java.util.ArrayList());
        ((java.util.ArrayList)Columns.get(0)).add( new Integer(nextColumn++));
        Columns.add(new java.util.ArrayList());
        ((java.util.ArrayList)Columns.get(1)).add( new Integer(nextColumn++));

        //build predicate network
        // [ 0 1]
        // [ 2 3]
        Rows.add(new Integer(nextRow++));
        ((java.util.ArrayList)Columns.get(0)).add( MEF.get(0,Integer.MIN_VALUE) );
        ((java.util.ArrayList)Columns.get(1)).add( MEF.get(1,Integer.MIN_VALUE) );

        Rows.add(new Integer(nextRow++));
        ((java.util.ArrayList)Columns.get(0)).add( MEF.get(2,Integer.MIN_VALUE) );
        ((java.util.ArrayList)Columns.get(1)).add( MEF.get(3,Integer.MIN_VALUE) );
        Width = Height = 2;
        
        int step = -1;
        do {
            Changes = false;
            step++;
            
            for (int yloop = Height-1; yloop >= 0; yloop--) {
                for (int xloop = Width-1; xloop >= 0; xloop--) {
                    TempME = get(xloop, yloop);
                    
                    if ((TempME.RuleTargetOffset != Integer.MIN_VALUE) &&
                         (TempME.RuleTargetOffset < RL.size()) &&
                         (TempME.LastModified != step)) {
                            TempRule = (Rule)RL.get(TempME.RuleTargetOffset);
                            TempME.RuleTargetOffset = Integer.MIN_VALUE;

                            if (TempRule.MatrixType == Rule.MatrixType_Expansion) {
                                int AddWidth = new Double(Math.ceil(Math.sqrt(TempRule.size()))).intValue();
                                int AddHeight = new Double(Math.ceil( ((double)TempRule.size())/AddWidth)).intValue();
                                expandMatrix(step, xloop, yloop, AddWidth-1, AddHeight-1); //-1 to account for existing row
                            }
                            overlapElements(step, xloop, yloop, TempRule);
                            Changes = true;
                    }
                }
            if ((Width > MaxSize) || (Height > MaxSize))
                break;
            }
            if (Matrix.Debug == true) {
                StringWindow sw = new StringWindow(toString());
                sw.setVisible(true);
            }
        } while ( (Width < MaxSize) && (Height < MaxSize) && Changes && (step < MaxSteps));
    }
    
    public void overlapElements(int step, int x, int y, java.util.ArrayList ElementsToAdd){
        MatrixElement TempME, TempETA;
        java.util.Iterator ETAIterator = ElementsToAdd.iterator();
        int NumElements = ElementsToAdd.size();
        
        int AddWidth = new Double(Math.ceil(Math.sqrt(NumElements))).intValue();
        if (AddWidth == 0)
            return;
        int AddHeight = new Double(Math.ceil(NumElements/AddWidth)).intValue();

        if ( (Overlap_ClipAtEdge) && ((x+AddWidth) > Width) )
            AddWidth = Width - x;
        if ( (Overlap_ClipAtEdge) && ((y+AddHeight) > Height) )
            AddHeight = Height - y;

        for (int yloop = y; yloop < (y+AddHeight); yloop++) {
            for (int xloop = x; xloop < (x+AddWidth); xloop++) {
                TempME  = get(xloop % Width, yloop % Height);
                if (ETAIterator.hasNext())
                    TempETA = (MatrixElement)ETAIterator.next();
                 else
                    TempETA = MEF.get();
                
                if (TempETA.Weight != Integer.MIN_VALUE)
                    if (TempME.Weight == Integer.MIN_VALUE)
                        TempME.Weight = TempETA.Weight;
                    else
                        TempME.Weight += TempETA.Weight;
                if (TempME.RuleTargetOffset == Integer.MIN_VALUE) {
                    TempME.RuleTargetOffset = TempETA.RuleTargetOffset;
                    //TempME.setLastModified(step);
                }
            }
        }
    }
    
    private void expandMatrix(int step, int atX, int atY, int X, int Y) {
        int xloop, yloop;
        int addedx=0, addedy=0;
        java.util.ArrayList TempAL;
        MatrixElement m;
        
        if ((atX >= Width) || (atY >= Height)) {
            System.out.println("Matrix : expandMatrix : x>=Width  or  y>=Height\n");
            return;
        }
        
        for (yloop = atY + 1; yloop <= atY + Y; yloop++) {
            if (yloop>=Height)
            {
                for (xloop = 0; xloop < Width; xloop++) {
                    TempAL = (java.util.ArrayList) Columns.get(xloop);
                    TempAL.add( MEF.get(Integer.MIN_VALUE, Integer.MIN_VALUE, step) );
                }
                Rows.add(new Integer(nextRow++));
                Height++;
            } else {
                 if ( (((MatrixElement)get(0, yloop)).LastModified != step) ){
                    for (xloop = 0; xloop < Width; xloop++) {
                        TempAL = (java.util.ArrayList) Columns.get(xloop);
                        TempAL.add(yloop+1, MEF.get(Integer.MIN_VALUE, Integer.MIN_VALUE, step) ); //add 1 to skip first row
                     }
                    Rows.add(yloop, new Integer(nextRow++));
                    Height++;
                }
            }
        }

        for (xloop = atX + 1; xloop <= atX + X; xloop++) {
            if ((xloop>=Width) ||
                ((xloop < Width) && 
                 (((MatrixElement)get(xloop, 0)).LastModified != step))) {
                TempAL = new java.util.ArrayList();
                TempAL.add(new Integer(nextColumn++));
                if (xloop>=Width)
                    Columns.add(TempAL);
                else
                    Columns.add(xloop, TempAL);
                for (yloop = 0; yloop < Height; yloop++) {
                    TempAL.add( MEF.get(Integer.MIN_VALUE, Integer.MIN_VALUE, step) );
                }
                Width++;
            }
        }
    }
    MatrixElement get(int x, int y) {
        if ( (x>=Width) || (y>=Height) ||(x<0) || (y<0)) {
            return null;
        }
        
        java.util.ArrayList TempAL = (java.util.ArrayList)Columns.get(x);
        return (MatrixElement)TempAL.get(y+1);
    }
    
    public java.util.ArrayList getConnectionList() {
        MatrixElement TempME;
        java.util.ArrayList TempAL;
        java.util.ArrayList ConnectionList = new java.util.ArrayList();
        
        for (int yloop = 0; yloop < Height; yloop++) {
            for (int xloop = 0; xloop < Width; xloop++) {
                TempME = get(xloop, yloop);
                if ((TempME.Weight != Integer.MIN_VALUE) ){
                    java.util.ArrayList Connection = new java.util.ArrayList(3);
                    Connection.add(new java.lang.Integer(getNodeRow(yloop)));
                    Connection.add(new java.lang.Integer(getNodeColumn(xloop)));
                    Connection.add(new java.lang.Integer(TempME.Weight));
                    ConnectionList.add(Connection);
                }
            }
        }
        ConnectionList.trimToSize();
        return ConnectionList;
    }

    //figures out in which order the column was inserted
    public int getNodeColumn(int column) {
        java.util.ArrayList TempAL = (java.util.ArrayList)Columns.get(column);

        if (!NaturalOrdering)
            return column;

        return ((Integer)TempAL.get(0)).intValue();
    }
    
    public int getNodeColumnR(int order) {
        java.util.Iterator ti = Columns.iterator();
        java.util.ArrayList TempAL;
        int retval = -1;
        int temp = 0;
        
        if (!NaturalOrdering)
            return order;
 
        for (; ti.hasNext() && retval==-1; ) {
            TempAL = (java.util.ArrayList)ti.next();
            if (((Integer)TempAL.get(0)).intValue() == order)
                retval = temp;
            temp++;
        }
        return retval;
    }
    //figures out in which order the column was inserted
    public int getNodeRow(int row) {
        if (!NaturalOrdering)
            return row;

        return ((Integer)Rows.get(row)).intValue();
    }
    public int getNodeRowR(int order) {
        java.util.Iterator ti = Rows.iterator();
        java.util.ArrayList TempAL;
        int retval = -1, temp = 0;
        
        if (!NaturalOrdering)
            return order;

        for (; ti.hasNext() && retval==-1; ) {
            if (((Integer)ti.next()).intValue() == order)
                retval = temp;
            temp++;
        }
        return retval;
    }
    public int getNumberOfNodes() {
        if (Height > Width)
            return Height;
        else
            return Width;
    }
    public java.lang.String toString() {
        MatrixElement TempME;
        java.util.ArrayList TempAL;
        java.lang.StringBuffer OutputString = new java.lang.StringBuffer();
        java.lang.StringBuffer OutputLine = new java.lang.StringBuffer();
        int temp;
        
        OutputString.append("Rules:\n");
        OutputString.append(RL.toString());
        
        OutputString.append("Matrix:\n");
        OutputString.append("        ");
        for (int xloop = 0; xloop < Width; xloop++) {
            if (getNodeColumnR(xloop)!=-1)
                OutputString.append( (((getNodeColumnR(xloop)<10)&&(getNodeColumnR(xloop)>=0))?" ":"") + 
                    getNodeColumnR(xloop) + ":" + (((xloop<10)&&(xloop>=0))?" ":"")+xloop +",");
        }
        OutputString.append("\n");
        for (int yloop = 0; yloop < Height; yloop++) {
            OutputLine.delete(0, OutputLine.length());
            if (getNodeRowR(yloop)==-1)
                continue;
            if ( (getNodeRowR(yloop) <10) && (getNodeRowR(yloop)>=0))
                OutputLine.append(" ");
            OutputLine.append(getNodeRowR(yloop)+":");
            if (yloop <10)
                OutputLine.append(" ");
            OutputLine.append(yloop);
                OutputLine.append(" | ");
            for (int xloop = 0; xloop < Width; xloop++) {
                TempME = get(getNodeColumnR(xloop), getNodeRowR(yloop));
                if (TempME == null)
                    continue;
                else if (TempME.getWeight() >= 10)
                    OutputLine.append(" ");
                else if (TempME.getWeight() >= 0)
                    OutputLine.append("  ");
                else if (TempME.getWeight() > -10)
                    OutputLine.append(" ");
                
                if (TempME == null)
                    OutputLine.append(" -  ,");
                else if (TempME.getWeight() != Integer.MIN_VALUE)
                    OutputLine.append(TempME.getWeight()+"  ,");
                else 
                    OutputLine.append(" -   ,");
                //OutputString.append(TempME.toString());
            }
            OutputString.append(OutputLine);
            OutputString.append("\n");
        }
        
        OutputString.append("Connections:\n");
        for (int yloop = 0; yloop < Height; yloop++) {
            OutputLine.delete(0, OutputLine.length());
            OutputLine.append("Node "+yloop+" -> ");
            for (int xloop = 0; xloop < Width; xloop++) {
                TempME = get(getNodeColumnR(xloop), getNodeRowR(yloop));
                if  ((TempME != null) && (TempME.Weight != Integer.MIN_VALUE) )
                    OutputLine.append(xloop + "(" + TempME.Weight + "), ");
            }
            OutputString.append(OutputLine);
            OutputString.append("\n");
        }

        return OutputString.toString();
    }    
    public static void setNaturalOrdering (boolean b){
        NaturalOrdering = b;
    }
    public static boolean getNaturalOrdering (){
        return NaturalOrdering;
    }
}