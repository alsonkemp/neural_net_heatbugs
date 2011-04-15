/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
/*
 * Created on Jun 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package uchicago.src.sim.nnheatBugs;

import java.awt.Color;

import edu.uci.ics.jung.graph.Vertex;

/**
 * @author danyelf
 */
public interface VertexColorFunction {

	/**
	 * What color should the TEXT of this vertex be?
	 * @param v
	 * @return the Color of the text.
	 */
	public Color getForeColor(Vertex v);

	/**
	 * What color should the FIELD of this vertex be?
	 * @param v
	 * @return the color of the background of the vertex.
	 */
	public Color getBackColor(Vertex v);

}
