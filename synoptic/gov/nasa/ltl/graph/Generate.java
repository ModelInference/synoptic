
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

/**
 * DOCUMENT ME!
 */
public class Generate {
  public static Graph generate (int nsets) {
    // nsets is number of accepting conditions
    // nnodes is number of nodes in automaton generated - numbered from 0
    int    nnodes = nsets + 1;

    Node[] nodes = new Node[nnodes];
    Graph  g = new Graph();

    g.setIntAttribute("nsets", nsets);
    g.setStringAttribute("type", "ba");
    g.setStringAttribute("ac", "nodes");

    for (int i = 0; i < nnodes; i++) {
      nodes[i] = new Node(g);

      StringBuffer label = new StringBuffer();

      for (int k = 0; k < i; k++) {
        label.append("acc" + k + "+");
      }

      nodes[i].setStringAttribute("label", label.toString());
    }

    Node n;

    // careful- generating edges acc/ding to which is to be explored first
    // corrected by Dimitra
    for (int i = 0; i < nsets; i++) {
      n = nodes[i];

      for (int j = nsets; j > i; j--) {
        Edge e = new Edge(nodes[i], nodes[j], "-", "-", null);

        for (int k = i; k < j; k++) {
          e.setBooleanAttribute("acc" + k, true);
        }
      }

      Edge e = new Edge(nodes[i], nodes[i], "-", "-", null);
      e.setBooleanAttribute("else", true);
    }


    // now the last node
    n = nodes[nnodes - 1];
    n.setBooleanAttribute("accepting", true);

    Edge e = new Edge(n, n, "-", "-", null);

    for (int k = 0; k < nsets; k++) {
      e.setBooleanAttribute("acc" + k, true);
    }

    for (int i = nsets - 1; i >= 0; i--) {
      e = new Edge(n, nodes[i], "-", "-", null);

      if (i == 0) {
        e.setBooleanAttribute("else", true);
      } else {
        for (int k = 0; k < i; k++) {
          e.setBooleanAttribute("acc" + k, true);
        }
      }
    }

    g.setInit(n);

    //  g.setInit(nodes[0]);
    return g;
  }

  public static void main (String[] args) {
    Graph g = generate(5);

    g.save(Graph.FSP_FORMAT);
  }

  /* 
     public static void main(String[] args) {
       Graph g = generate(Integer.parseInt(args[0]));
       
       g.save();
     }
   */
}