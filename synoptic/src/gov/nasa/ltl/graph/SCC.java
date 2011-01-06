
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
import java.util.LinkedList;
import java.util.List;


/**
 * DOCUMENT ME!
 */
public class SCC {
  public static void help () {
    System.err.println("usage:");
    System.err.println("\tDegenalize [-join|-degeneralize] [outfile]");
    System.exit(1);
  }

  public static void main (String[] args) {
    String outname = null;

    for (int i = 0, l = args.length; i < l; i++) {
      if (outname == null) {
        outname = args[i];
      } else {
        help();
      }
    }

    try {
      Graph g = Graph.load("out.sm");

      List<List<Node>> scc = scc(g);

      for (Iterator<List<Node>> i = scc.iterator(); i.hasNext();) {
        List<Node> l = i.next();
        System.out.println("component:");

        for (Iterator<Node> j = l.iterator(); j.hasNext();) {
          Node n = j.next();

          System.out.println("  " + n.getStringAttribute("label"));
        }

        System.out.println();
      }

      if (outname == null) {
        g.save();
      } else {
        g.save(outname);
      }
    } catch (IOException e) {
      e.printStackTrace();

      return;
    }
  }

  public static void print (List<List<Node>> sccs) {
    System.out.println("Strongly connected components:");

    int cnt = 0;

    for (Iterator<List<Node>> i = sccs.iterator(); i.hasNext();) {
      List<Node> scc = i.next();

      System.out.println("\tSCC #" + (cnt++));

      for (Iterator<Node> j = scc.iterator(); j.hasNext();) {
        Node n = j.next();
        System.out.println("\t\t" + n.getId() + " - " +
                           n.getStringAttribute("label"));
      }
    }
  }

  public static List<List<Node>> scc (Graph g) {
    Node init = g.getInit();

    if (init == null) {
      return new LinkedList<List<Node>>();
    }

    init.setBooleanAttribute("_reached", true);

    SCCState s = new SCCState();
    visit(init, s);

    final List<List<Node>> scc = new java.util.ArrayList<List<Node>>(s.SCC);

    for (int i = 0; i < s.SCC; i++) {
    	scc.add(new LinkedList<Node>());
    }

    g.forAllNodes(new EmptyVisitor() {
      @Override
      public void visitNode (Node n) {
      	scc.get(n.getIntAttribute("_scc")).add(n);

        n.setBooleanAttribute("_reached", false);
        n.setBooleanAttribute("_dfsnum", false);
        n.setBooleanAttribute("_low", false);
        n.setBooleanAttribute("_scc", false);
      }
    });

    List<List<Node>> list = new LinkedList<List<Node>>();

    for (int i = 0; i < s.SCC; i++) {
      list.add(scc.get(i));
    }

    return list;
  }

  private static void visit (Node p, SCCState s) {
    s.L.add(0, p);
    p.setIntAttribute("_dfsnum", s.N);
    p.setIntAttribute("_low", s.N);
    s.N++;

    for (Iterator<Edge> i = p.getOutgoingEdges().iterator(); i.hasNext();) {
      Edge e = i.next();
      Node q = e.getNext();

      if (!q.getBooleanAttribute("_reached")) {
        q.setBooleanAttribute("_reached", true);
        visit(q, s);
        p.setIntAttribute("_low",
                          Math.min(p.getIntAttribute("_low"),
                                   q.getIntAttribute("_low")));
      } else if (q.getIntAttribute("_dfsnum") < p.getIntAttribute("_dfsnum")) {
        if (s.L.contains(q)) {
          p.setIntAttribute("_low",
                            Math.min(p.getIntAttribute("_low"),
                                     q.getIntAttribute("_dfsnum")));
        }
      }
    }

    if (p.getIntAttribute("_low") == p.getIntAttribute("_dfsnum")) {
      Node v;

      do {
        v = s.L.remove(0);
        v.setIntAttribute("_scc", s.SCC);
      } while (v != p);

      s.SCC++;
    }
  }

  /**
   * DOCUMENT ME!
   */
  private static class SCCState {
    protected SCCState() {
      // null operation
    }
    public int  N = 0;
    public int  SCC = 0;
    public List<Node> L = new LinkedList<Node>();
  }
}