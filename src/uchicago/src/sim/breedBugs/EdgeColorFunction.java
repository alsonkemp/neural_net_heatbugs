/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
package uchicago.src.sim.breedBugs;

import java.awt.Color;

import edu.uci.ics.jung.graph.Edge;

/**
 * An EdgeColorFunction returns a <tt>Color</tt>, given an <tt>Edge</tt>.
 * Is used for <tt>SettableRenderer</tt> in order to allow a variety of
 * colors to be chosen based on whatever the user wants to use.
 */
public interface EdgeColorFunction {
	
	public Color getEdgeColor( Edge e );

}
