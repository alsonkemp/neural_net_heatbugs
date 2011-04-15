/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
package uchicago.src.sim.nnheatBugs;

import java.awt.*;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.visualization.AbstractRenderer;

/**
 * A renderer with all sorts of buttons to press and dials
 * to turn. In general, if a function is available to get
 * an answer to questions of color. Otherwise, the set
 * fields are used.
 * <p>
 * The default is to paint vertices with Black foreground
 * text and Red backgrounds. Picked vertices are orange.
 * [Whether a vertex is Picked is resolved with
 * <tt>v.getUserDatum(_VisualizationViewer.VIS_KEY);</tt>
 *
 * <p>
 * Note that setting a stroke width other than 1 is likely
 * to slow down the visualization noticably, as is using
 * transparency.
 * 
 * @author danyelf
 */
public class SettableRenderer extends AbstractRenderer {

	private Color vertexFGColor = Color.BLACK;

	private Color vertexPickedColor = Color.ORANGE;
	private Color vertexBGColor = Color.RED;

	private VertexColorFunction vertexColorFunction;

	private EdgeThicknessFunction edgeThicknessFunction;
	private int edgeThickness;

	private Color edgeColor = Color.BLACK;
	private EdgeColorFunction edgeColorFunction;

	private StringLabeller sl;

	public SettableRenderer(StringLabeller sl) {
		this.sl = sl;
	}

	public void setStringLabeller(StringLabeller sl) {
		this.sl = sl;
	}

	public void setEdgeColor(Color c) {
		edgeColor = c;
	}

	/**
	 * Edges are drawn by calling <tt>EdgeColorFunction</tt> with
	 *  the edge, to decide how it is to be drawn.
	 * @param ecf
	 */
	public void setEdgeColorFunction(EdgeColorFunction ecf) {
		this.edgeColorFunction = ecf;
	}

	/**
	 * Forces all edges to draw with this thickness. Sets the edge
	 * thickness function to null.
	 * @param i
	 */
	public void setEdgeThickness(int i) {
		this.edgeThicknessFunction = null;
		this.edgeThickness = i;
	}

	/**
	 * This version takes a function that dynamically chooses an edge thickness.
	 * @param etf
	 */
	public void setEdgeThicknessFunction(EdgeThicknessFunction etf) {
		this.edgeThicknessFunction = etf;
		this.edgeThickness = 0;
	}

	/**
	 * Paints the edge in the color specified by the EdgeColorFunction
	 * or the hard-set color, and at the thickness set with an
	 * <tt>EdgeThicknessFunction</tt>.
	 * @see EdgeThicknessFunction 	EdgeThicknessFunction
	 * @see EdgeColorFunction	EdgeColorFunction
	 */
	public void paintEdge(Graphics g, Edge e, int x1, int y1, int x2, int y2) {
		int edgeWidth;
		if (edgeThicknessFunction != null)
			edgeWidth = edgeThicknessFunction.getEdgeThickness(e);
		else
			edgeWidth = edgeThickness;

		if (edgeWidth == 1)
			drawEdgeSimple(g, e, x1, y1, x2, y2);
		else
			drawEdge(edgeWidth, g, e, x1, y1, x2, y2);
	}

	/**
	 * Draws the edge at the given width.
	 * 
	 * @param edgeWidth
	 * @param g
	 * @param e
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	private void drawEdge(
		int edgeWidth,
		Graphics g,
		Edge e,
		int x1,
		int y1,
		int x2,
		int y2) {
		Graphics2D g2d = (Graphics2D) g;
                int xmid,ymid;
                double angle;

		if (strokeTable[1] == null) {
			Stroke s = new BasicStroke(1);
			strokeTable[1] = s;
		}

		if (strokeTable[edgeWidth] == null) {
			Stroke s = new BasicStroke(edgeWidth);
			strokeTable[edgeWidth] = s;
		}
		g2d.setStroke(strokeTable[edgeWidth]);
		drawEdgeSimple(g, e, x1, y1, x2, y2);
		g2d.setStroke(strokeTable[1]);
	}

	Stroke[] strokeTable = new Stroke[100];

	private void drawEdgeSimple(
		Graphics g,
		Edge e,
		int x1,
		int y1,
		int x2,
		int y2) {
                int xmid,ymid;
                double angle;
                
		if (edgeColorFunction == null) {
			g.setColor(edgeColor);
		} else {
			g.setColor(edgeColorFunction.getEdgeColor(e));
		}
                xmid = (x1+x2)/2;
                ymid = (y1+y2)/2;
		g.drawLine(x1, y1, x2, y2);
                angle = Math.atan2(y2-y1, x2-x1);
                g.drawLine(xmid, ymid, 
                    (int)(xmid-10*Math.cos(angle+45.0/180.0)), (int)(ymid-10*Math.sin(angle+45.0/180.0)));
                g.drawLine(xmid, ymid, 
                    (int)(xmid-10*Math.cos(angle-45.0/180.0)), (int)(ymid-10*Math.sin(angle-45.0/180.0)));
                
	}

	/**
	 * Manually sets the color of a Vertex's foreground (i.e.
	 * its text) 
	 * @param vertexColor
	 */
	public void setVertexForegroundColor(Color vertexColor) {
		this.vertexFGColor = vertexColor;
	}

	/**
	 * Manually sets the color of a picked Vertex's background (i.e.
	 * its field) 
	 * @param vertexColor
	 */
	public void setVertexPickedColor(Color vertexColor) {
		this.vertexPickedColor = vertexColor;
	}

	/**
	 * Manually sets the color of an unpicked Vertex's background (i.e.
	 * its field) 
	 * @param vertexColor
	 */
	public void setVertexBGColor(Color vertexColor) {
		this.vertexBGColor = vertexColor;
	}

	/**
	 * Finds the color of a vertex with a VertexColorFunction
	 * @param vcf
	 */
	public void setVertexColorFunction(VertexColorFunction vcf) {
		this.vertexColorFunction = vcf;
	}

	/**
	 * Simple label function returns the
	 * StringLabeller's notion of v's label.
	 * It may be sometimes useful to 
	 * override this.
	 * @param v a vertex
	 * @return the label on the vertex.
	 */
	protected String getLabel(Vertex v) {
		String s= sl.getLabel( v );
		if ( s == null ) {
			return "?";
		} else {
			return s;
		}
	}

	/**
	 * Paints the vertex, using the settings above (VertexColors,
	 * etc). In this implmenetation, vertices are painted as 
	 * filled squares with textual labels over the filled square.
	 */
	public void paintVertex(Graphics g, Vertex v, int x, int y) {

		String label = getLabel(v);

		Color fg =
			(vertexColorFunction == null)
				? vertexFGColor
				: vertexColorFunction.getForeColor(v);

		if (vertexColorFunction == null) {
			if (isPicked(v)) {
				g.setColor(vertexPickedColor);
			} else
				g.setColor(vertexBGColor);
		} else {
			g.setColor(vertexColorFunction.getBackColor(v));
		}

		g.fillRect(x - 8, y - 6, g.getFontMetrics().stringWidth(label) + 8, 16);
		g.setColor(fg);
		g.drawString(label, x - 4, y + 6);
	}

}
