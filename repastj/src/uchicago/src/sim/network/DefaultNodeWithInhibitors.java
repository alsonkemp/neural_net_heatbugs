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

package uchicago.src.sim.network;

import java.util.*;
import uchicago.src.sim.util.Random;


/**
 * This node allows for inputs/outputs and inhibitory inputs/outputs.
 *
 */
public class DefaultNodeWithInhibitors extends DefaultNode {


  /**
   * A list of the inhibitory edges into this Node.
   */
  protected ArrayList inInhibitoryEdges = new ArrayList(3);

  /**
   * A Map of nodes whose Inhibitory edges are into this Node. The Node is the key
   * and a HashSet of edges is the value.
   */

  // ToDo when we go to 1.4.1 or > then we can use a LinkedHashMap
  protected OrderedHashMap inInhibitoryMap = new OrderedHashMap();

  /**
   * A list of the Inhibitory edges out of this Node.
   */
  protected ArrayList outInhibitoryEdges = new ArrayList(3);

  /**
   * A Map of nodes whose Inhibitory edges are out of this Node. The Node is the key
   * and a Hashset of edges is the value.
   */

// ToDo when we go to 1.4.1 or > then we can use a LinkedHashMap
  protected OrderedHashMap outInhibitoryMap = new OrderedHashMap();

  
  /**
   * Creates a DefaultNode with a blank label.
   */
  public DefaultNodeWithInhibitors() {
    super();
  }

    /**
   * Gets the ArrayList of Inhibitory edges into this node. The order of the list
   * is the order in which the Edges where added.
   */
  public ArrayList getInInhibitoryEdges() {
    return inInhibitoryEdges;
  }

  /**
   * Gets the ArrayList of inhibitory Edge out of this node. The order of the list
   * is the order in which the Edges where added.
   */
  public ArrayList getOutInhibitoryEdges() {
    return outInhibitoryEdges;
  }

  /**
   * Gets all of the Nodes that this DefaultNode has an inhibitory edge
   * from. This is a list of all the nodes on the end of this node's
   * in edges.
   */
  public ArrayList getFromInhibitoryNodes() {
    return getInInhibitoryNodes();
  }

  /**
   * Gets all of the Nodes that this DefaultNode has an inhibitory edge to. This
   * is a list of all the nodes on the end of this node's out edges.
   */
  public ArrayList getToInhibitoryNodes() {
    return getOutInhibitoryNodes();
  }

    /**
   * Gets all of the Nodes that this DefaultNode has an inhibitory edge to. This
   * is a list of all the nodes on the end of this node's inhibitory out edges.
   * The iteration order of these Nodes will be the order in which the
   * corresponding edges were first added to this DefaultNode.
   */
  public ArrayList getOutInhibitoryNodes() {
    return new ArrayList(outInhibitoryMap.keys);
  }

    /**
   * Gets all of the Nodes that this DefaultNode has an inhibitory edge
   * from. This is a list of all the nodes on the end of this node's
   * inhibitory in edges. The iteration order of these Nodes will be the order in which the
   * corresponding edges were first added to this DefaultNode.
   */
  public ArrayList getInInhibitoryNodes() {
    return new ArrayList(inInhibitoryMap.keys);
  }

  /**
   * Does this Node have an inhibitory edge to or from the specified node.
   *
   * @param node the node to check if this DefaultNode contains an edge to.
   */
  public boolean hasInhibitoryEdgeToOrFrom(Node node) {
    return inInhibitoryMap.containsKey(node) || outInhibitoryMap.containsKey(node);
  }

  /**
   * Adds an inhibitory In Edge to this DefaultNode.
   *
   * @param edge the "in" edge to add
   */
  public void addInInhibitoryEdge(Edge edge) {
    Node from = edge.getFrom();
    HashSet s = (HashSet)inInhibitoryMap.get(from);
    if (s == null) {
      s = new HashSet();
      inInhibitoryMap.put(from, s);
      s.add(edge);
      inInhibitoryEdges.add(edge);

    } else if (!s.contains(edge)) {
      s.add(edge);
      inInhibitoryEdges.add(edge);
    }
  }

    /**
   * Adds an out Edge to this DefaultNode
   *
   * @param edge the "out" edge to add
   */
  public void addOutInhibitoryEdge(Edge edge) {
    Node to = edge.getTo();
    HashSet s = (HashSet)outInhibitoryMap.get(to);
    if (s == null) {
      s = new HashSet();
      outInhibitoryMap.put(to, s);
      s.add(edge);
      outInhibitoryEdges.add(edge);
    } else if (!s.contains(edge)) {
      s.add(edge);
      outInhibitoryEdges.add(edge);
    }
  }
  
  /**
   * Adds inhibitory out edges.
   */
  public void addOutInhibitoryEdges(Collection edges) {
    Iterator i = edges.iterator();
    while (i.hasNext()) {
      Edge edge = (Edge)i.next();
      addOutInhibitoryEdge(edge);
    }
  }

  /**
   * Adds inhibitory in edges.
   */
  public void addInInhibitoryEdges(Collection edges) {
    Iterator i = edges.iterator();
    while (i.hasNext()) {
      Edge edge = (Edge)i.next();
      addInInhibitoryEdge(edge);
    }
  }

  /**
   * Clears (removes) all the in edges.
   */
  public void clearInEdges() {
    inMap.clear();
    inEdges.clear();
    inInhibitoryMap.clear();
    inInhibitoryEdges.clear();
  }

  /**
   * Clears (removes) all the out edges. This does not <b>NOT</b> remove
   * these cleared out edges as in edges from the Nodes on the other side
   * of these edges.
   */
  public void clearOutEdges() {
    outMap.clear();
    outEdges.clear();
    outInhibitoryMap.clear();
    outInhibitoryEdges.clear();
  }

  /**
   * Removes the specified inhibitory edge from the list of inhibitory "in" edges. This does
   * <b>NOT</b> remove the edge as an out edge from the Node on the other
   * side of this Edge. This does not <b>NOT</b> remove
   * these cleared in edges as out edges from the Nodes on the other side
   * of these edges.
   *
   * @param edge the edge to remove
   */
  public void removeInInhibitoryEdge(Edge edge) {
    Node node = edge.getFrom();
    inInhibitoryMap.remove(node);
    inInhibitoryEdges.remove(edge);
  }

  /**
   * Removes the specified inhibitory edge from the list of inhibitory "out" edges. This does
   * <b>NOT</b> remove the edge as an in edge from the Node on the other
   * side of this Edge.
   *
   * @param edge the edge to remove
   */
  public void removeOutInhibitoryEdge(Edge edge) {
    Node node = edge.getTo();
    outInhibitoryMap.remove(node);
    outInhibitoryEdges.remove(edge);
  }

  /**
   * Gets a node at random from the list of inhibitory out edges. This will return null
   * if there are no out edges.
   */
  public Node getRandomNodeOutInhibitory() {
    if (outInhibitoryEdges.size() > 0) {
      int index = Random.uniform.nextIntFromTo(0, outInhibitoryEdges.size() - 1);
      Edge e = (Edge)outInhibitoryEdges.get(index);
      return e.getTo();
    }

    return null;
  }

  /**
   * Gets a node at random from the list of Inhibitory in edges. This will return null
   * if there are no in edges.
   */
  public Node getRandomNodeInInhibitory() {
    if (inInhibitoryEdges.size() > 0) {
      int index = Random.uniform.nextIntFromTo(0, inInhibitoryEdges.size() - 1);
      Edge e = (Edge)inInhibitoryEdges.get(index);
      return e.getFrom();
    }
    return null;
  }

  /**
   * Gets a node at random from the list of nodes that this node has an
   * Inhibitory edge from. This is identical to getRandomNodeIn().
   */
  public Node getRandomFromNodeInhibitory() {
    return getRandomNodeInInhibitory();
  }

  /**
   * Gets a node at random from the list of nodes that this node has an
   * Inhibitory edge to. This is identical to getRandomNodeOut().
   */
  public Node getRandomToNodeInhibitory() {
    return getRandomNodeOutInhibitory();
  }

  /**
   * Removes all the Inhibitory edges that link from this Node to the specified node.
   * This does <b>NOT</b> remove these edges as from edges from the
   * specified node.
   */
  public void removeEdgesToInhibitory(Node node) {
    HashSet s = (HashSet)outInhibitoryMap.remove(node);
    if (s != null) outInhibitoryEdges.removeAll(s);
  }

  /**
   * Removes all the Inhibitory edges that link to this Node from the specified node.
   * This does <b>NOT</b> remove these edges as to edges from the
   * specified node.
   */
  public void removeEdgesFromInhibitory(Node node) {
    HashSet s = (HashSet)inInhibitoryMap.remove(node);
    if (s != null) inInhibitoryEdges.removeAll(s);
  }


  /**
   * Creates an Inhibitory out edge from this node to a randomly chosen node in
   * the specified list using the specified edge. If allowSelfLoops is
   * true, then the created Edge may return a self loop, assuming that
   * this Node is an element in the list. If not, then the returned
   * Edge will always be to some other Node. Note that this method
   * adds the edge as an in edge to the random node, and as an out
   * edge to this DefaultNode.
   *
   * @param list the list of nodes to create the Edge to.
   * @param edge the edge to use as the link
   * @param allowSelfLoops if true then self loops are allowed. If not the
   * self loops are disallowed.
   */
  public Edge makeRandomOutInhibitoryEdge(List list, Edge edge, boolean allowSelfLoops) {
    if (list.size() == 0) throw new IllegalArgumentException("list size must be greater that 0");

    int limit = list.size() - 1;
    DefaultNodeWithInhibitors to = null;
    if (allowSelfLoops) {
      int index = Random.uniform.nextIntFromTo(0, limit);
      to = (DefaultNodeWithInhibitors)list.get(index);
    } else {
      int index = Random.uniform.nextIntFromTo(0, limit);
      to = (DefaultNodeWithInhibitors)list.get(index);
      while (to == this) {
	index = Random.uniform.nextIntFromTo(0, limit);
	to = (DefaultNodeWithInhibitors)list.get(index);
      }
    }

    edge.setTo(to);
    edge.setFrom(this);
    to.addInInhibitoryEdge(edge);
    this.addOutInhibitoryEdge(edge);
    return edge;
  }

  /**
   * Creates an Inhibitory in edge to this node from a randomly chosen node in
   * the specified list using the specified edge. If allowSelfLoops is
   * true, then the created Edge may return a self loop, assuming that
   * this Node is an element in the list. If not, then the returned
   * Edge will always be to some other Node. Note that this method
   * adds the edge as an out edge to the random node, and as an in
   * edge to this DefaultNode.
   *
   * @param list the list of nodes to create the Edge from.
   * @param edge the edge to use as the link
   * @param allowSelfLoops if true then self loops are allowed. If not the
   * self loops are disallowed.
   */
  public Edge makeRandomInInhibitoryEdge(List list, Edge edge, boolean allowSelfLoops) {
    if (list.size() == 0) throw new IllegalArgumentException("list size must be greater that 0");

    DefaultNodeWithInhibitors from = null;
    int limit = list.size() - 1;
    if (allowSelfLoops) {
      int index = Random.uniform.nextIntFromTo(0, limit);
      from = (DefaultNodeWithInhibitors)list.get(index);
    } else {
      int index = Random.uniform.nextIntFromTo(0, limit);
      from= (DefaultNodeWithInhibitors)list.get(index);
      while (from == this) {
	index = Random.uniform.nextIntFromTo(0, limit);
	from = (DefaultNodeWithInhibitors)list.get(index);
      }
    }

    edge.setTo(this);
    edge.setFrom(from);
    from.addOutInhibitoryEdge(edge);
    this.addInInhibitoryEdge(edge);
    return edge;
  }

  /**
   * Returns the number of Inhibitory out edges contained by the Node.
   */
  public int getNumOutInhibitoryEdges() {
    return outInhibitoryEdges.size();
  }

  /**
   * Returns the number of Inhibitory in edges contained by the Node.
   */
  public int getNumInInhibitoryEdges() {
    return inInhibitoryEdges.size();
  }

  /**
   * Returns true if this DefaultNode has an Inhibitory Edge to the specified Node,
   * otherwise false.
   */
  public boolean hasEdgeToInhibitory(Node node) {
    return getEdgesToInhibitory(node) != null;
  }

  /**
   * Returns true if this DefaultNode has an Edge from the specified
   * Node, otherwise false.
   */
  public boolean hasEdgeFromInhibitory(Node node) {
    return getEdgesFromInhibitory(node) != null;
  }

  /**
   * Gets the out degree of this DefaultNode. Same as
   * getNumOutEdges().
   */
  public int getOutInhibitoryDegree() {
    return outInhibitoryEdges.size();
  }


  /**
   * Gets the in degree of this DefaultNode. Same as getNumInEdges().
   */
  public int getInInhibitoryDegree() {
    return inInhibitoryEdges.size();
  }

  /**
   * Returns the Edges from this Node to the specified Node.  This
   * will return null if no such Edges exist.
   */
  public HashSet getEdgesToInhibitory(Node node) {
    return (HashSet)outInhibitoryMap.get(node);
  }

  /**
   * Returns the Edges from the specified Node to this Node. This will
   * return null if no such Edges exits.
   */
  public HashSet getEdgesFromInhibitory(Node node) {
    return (HashSet)inInhibitoryMap.get(node);
  }
}