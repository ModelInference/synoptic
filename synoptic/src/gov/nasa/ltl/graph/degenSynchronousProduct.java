
//
// Copyright (C) 2005 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.ltl.graph;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


/**
 * DOCUMENT ME!
 */
public class degenSynchronousProduct {
  public static void dfs (Graph g, Node[][] nodes, int nsets, Node n0, Node n1) {
    Node n = get(g, nodes, n0, n1);

    List<Edge> t0 = n0.getOutgoingEdges();
    List<Edge> t1 = n1.getOutgoingEdges();

    for (Iterator<Edge> i0 = t0.iterator(); i0.hasNext();) {
      Edge    e0 = i0.next();
      Node    next0 = e0.getNext();
      Edge    theEdge = null;

      boolean found = false;

      for (Iterator<Edge> i1 = t1.iterator(); i1.hasNext() && !found;) {
        Edge e1 = i1.next();

        if (e1.getBooleanAttribute("else")) {
          if (theEdge == null) {
            theEdge = e1;
          }
        } else {
          found = true;

          for (int i = 0; i < nsets; i++) {
            // n1 is the degeneraliser automaton
            // dimitra's code starts here
            int n1id = n1.getIntAttribute("lower_bound");

            if (i >= n1id) // ignore bits before lower bound
            {
              // dimitra's code ends here
              boolean b0 = e0.getBooleanAttribute("acc" + i);
              boolean b1 = e1.getBooleanAttribute("acc" + i);

              if (b0 != b1) {
                found = false;

                break;
              }
            }
          }
        }

        if (found) {
          theEdge = e1;
        }
      }

      if (theEdge != null) {
        Node    next1 = theEdge.getNext();
        boolean newNext = isNew(nodes, next0, next1);
        Node    next = get(g, nodes, next0, next1);
        new Edge(n, next, e0.getGuard(), theEdge.getAction(), null);

        if (newNext) {
          dfs(g, nodes, nsets, next0, next1);
        }
      }
    }
  }

  public static void main (String[] args) {
    Graph g0;
    Graph g1;

    try {
      g0 = Graph.load(args[0]);
      g1 = Graph.load(args[1]);
    } catch (IOException e) {
      System.err.println("Can't load automata");
      System.exit(1);

      return;
    }

    Graph g = product(g0, g1);

    g.save();
  }

  public static Graph product (Graph g0, Graph g1) {
    int nsets = g0.getIntAttribute("nsets");

    if (nsets != g1.getIntAttribute("nsets")) {
      System.err.println("Different number of accepting sets");
      System.exit(1);
    }

    Node[][] nodes;
    Graph    g = new Graph();
    g.setStringAttribute("type", "ba");
    g.setStringAttribute("ac", "nodes");

    nodes = new Node[g0.getNodeCount()][g1.getNodeCount()];

    dfs(g, nodes, nsets, g0.getInit(), g1.getInit());

    return g;
  }

  private static boolean isNew (Node[][] nodes, Node n0, Node n1) {
    return nodes[n0.getId()][n1.getId()] == null;
  }

  private static Node get (Graph g, Node[][] nodes, Node n0, Node n1) {
    if (nodes[n0.getId()][n1.getId()] == null) {
      Node   n = new Node(g);
      String label0 = n0.getStringAttribute("label");
      String label1 = n1.getStringAttribute("label");

      if (label0 == null) {
        label0 = Integer.toString(n0.getId());
      }

      if (label1 == null) {
        label1 = Integer.toString(n1.getId());
      }

      n.setStringAttribute("label", label0 + "+" + label1);

      if (n1.getBooleanAttribute("accepting")) {
        n.setBooleanAttribute("accepting", true);
      }

      return nodes[n0.getId()][n1.getId()] = n;
    }

    return nodes[n0.getId()][n1.getId()];
  }
}