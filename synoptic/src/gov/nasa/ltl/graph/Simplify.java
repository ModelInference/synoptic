
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


/**
 * DOCUMENT ME!
 */
public class Simplify {
  public static void main (String[] args) {
    if (args.length > 1) {
      System.out.println("usage:");
      System.out.println("\tjava gov.nasa.ltl.graph.Simplify [<filename>]");

      return;
    }

    Graph g = null;

    try {
      if (args.length == 0) {
        g = Graph.load();
      } else {
        g = Graph.load(args[0]);
      }
    } catch (IOException e) {
      System.out.println("Can't load the graph.");

      return;
    }

    g = simplify(g);

    g.save();
  }

  public static Graph simplify (Graph g) {
    boolean simplified;

    do {
      simplified = false;

      for (Iterator<Node> i = g.getNodes().iterator(); i.hasNext();) {
        Node n0 = i.next();

        for (Iterator<Node> j = g.getNodes().iterator(); j.hasNext();) {
          Node n1 = j.next();

          if (n1.getId() <= n0.getId()) {
            continue;
          }

          if (n1.getBooleanAttribute("accepting") != n0.getBooleanAttribute(
                                                           "accepting")) {
            continue;
          }

          boolean equivalent = true;

          for (Iterator<Edge> k = n0.getOutgoingEdges().iterator();
               equivalent && k.hasNext();) {
            Edge e0 = k.next();

            equivalent = false;

            for (Iterator<Edge> l = n1.getOutgoingEdges().iterator();
                 !equivalent && l.hasNext();) {
              Edge e1 = l.next();

              if ((e0.getNext() == e1.getNext()) || 
                      ((e0.getNext() == n0 || e0.getNext() == n1) && 
                        (e1.getNext() == n0 || e1.getNext() == n1))) {
                if (e0.getGuard().equals(e1.getGuard())) {
                  if (e0.getAction().equals(e1.getAction())) {
                    equivalent = true;
                  }
                }
              }
            }
          }

          for (Iterator<Edge> k = n1.getOutgoingEdges().iterator();
               equivalent && k.hasNext();) {
            Edge e1 = k.next();

            equivalent = false;

            for (Iterator<Edge> l = n0.getOutgoingEdges().iterator();
                 !equivalent && l.hasNext();) {
              Edge e0 = l.next();

              if ((e0.getNext() == e1.getNext()) || 
                      ((e0.getNext() == n0 || e0.getNext() == n1) && 
                        (e1.getNext() == n0 || e1.getNext() == n1))) {
                if (e0.getGuard().equals(e1.getGuard())) {
                  if (e0.getAction().equals(e1.getAction())) {
                    equivalent = true;
                  }
                }
              }
            }
          }

          if (equivalent) {
            for (Iterator<Edge> k = n1.getIncomingEdges().iterator();
                 equivalent && k.hasNext();) {
              Edge e = k.next();

              new Edge(e.getSource(), n0, e.getGuard(), e.getAction(), 
                       e.getAttributes());
            }

            n1.remove();

            simplified = true;
          }
        }
      }
    } while (simplified);

    return g;
  }
}