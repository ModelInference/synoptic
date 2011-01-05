
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
package gov.nasa.ltl.trans;

import gov.nasa.ltl.graph.Edge;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * DOCUMENT ME!
 */
class Transition {
  private final TreeSet<Formula> propositions;
  private final int     pointsTo;
  private final BitSet  accepting;
  private final boolean safe_accepting;

  public Transition (TreeSet<Formula> prop, int nd_id, BitSet acc, boolean safety) {
    propositions = prop;
    pointsTo = nd_id;
    accepting = new BitSet(Node.getAcceptingConds());
    accepting.or(acc);
    safe_accepting = safety;
  }

  public void FSPoutput () {
    if (propositions.isEmpty()) {
      System.out.print("TRUE{");
    } else {
      // first print the propositions involved
      Iterator<Formula> it = propositions.iterator();
      Formula      nextForm = null;
      StringBuffer act = new StringBuffer();
      char         cont; // stores content of formula
      boolean      need_AND = false; // connect with AND multiple propositions

      while (it.hasNext()) {
        nextForm = it.next();
        cont = nextForm.getContent();

        if (need_AND) {
          act.append("_AND_");
        }

        need_AND = true;

        switch (cont) {
        case 'N':
          act.append('N');
          act.append(nextForm.getSub1().getName());

          break;

        case 't':
          act.append("TRUE");

          break;

        default:
          act.append(nextForm.getName());

          break;
        }
      }

      System.out.print(act + "{");
    }

    if (Node.accepting_conds == 0) {
      if (safe_accepting == true) {
        System.out.print("0");
      }
    } else {
      for (int i = 0; i < Node.accepting_conds; i++) {
        if (!accepting.get(i)) {
          System.out.print(i);
        }
      }
    }


    // and then the rest - easy
    System.out.print("} -> S" + pointsTo + " ");
  }

  public void SMoutput (gov.nasa.ltl.graph.Node[] nodes,
                        gov.nasa.ltl.graph.Node node) {
    String guard = "-";
    String action = "-";

    if (!propositions.isEmpty()) {
      Iterator<Formula> it = propositions.iterator();
      Formula      nextForm = null;
      StringBuffer sb = new StringBuffer();
      char         cont; // stores content of formula
      boolean      need_AND = false; // connect with AND multiple propositions

      while (it.hasNext()) {
        nextForm = it.next();
        cont = nextForm.getContent();

        if (need_AND) {
          sb.append("&");
        }

        need_AND = true;

        switch (cont) {
        case 'N':
          sb.append('!');
          sb.append(nextForm.getSub1().getName());

          break;

        case 't':
          sb.append("true");

          break;

        default:
          sb.append(nextForm.getName());

          break;
        }
      }

      guard = sb.toString();
    }

    Edge e = new Edge(node, nodes[pointsTo], guard, action);

    if (Node.accepting_conds == 0) {
      //  Dimitra - Jan 10 2003
      // Believe there is a bug with the way we decided whether node was safety accepting
      // with example !<>(Xa \/ <>c)
      //    System.out.println("Entered the safety part of accepting conditions");
      //      if (safe_accepting == true) {
      //        System.out.println("But did I actually set it correctly?");
      e.setBooleanAttribute("acc0", true);

      //      }
    } else {
      for (int i = 0; i < Node.accepting_conds; i++) {
        if (!accepting.get(i)) {
          e.setBooleanAttribute("acc" + i, true);

          //        System.out.println("Transition belongs to set " + i);
        }
      }
    }
  }

  public boolean enabled (Hashtable<String, Boolean> ProgramState) {
    Iterator<Formula> mustHold = propositions.iterator();
    Formula  form = null;
    Boolean  value;

    while (mustHold.hasNext()) {
      form = mustHold.next();

      switch (form.getContent()) {
      case 'N':
        value = ProgramState.get(form.getSub1().getName());

        if (value == null) {
          //          System.out.println("Proposition not defined in program state");
          return false;
        } else if (value.booleanValue()) {
          return false;
        }

        break;

      case 't':
        break;

      case 'p':
        value = ProgramState.get(form.getName());

        if (value == null) {
          //          System.out.println("Proposition not defined in program state");
          return false;
        } else if (!value.booleanValue()) {
          return false;
        }

        break;
      }
    }

    return true;
  }

  public int goesTo () {
    return pointsTo;
  }
}