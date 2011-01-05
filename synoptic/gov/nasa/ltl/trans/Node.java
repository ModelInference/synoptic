
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

// Written by Dimitra Giannakopoulou, 19 Jan 2001
package gov.nasa.ltl.trans;

import java.util.BitSet;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * DOCUMENT ME!
 */
class Node implements Comparable<Node> {
  public static int      accepting_conds = 0;
  private static boolean init_collapsed = false;
  private int            nodeId;
  private final TreeSet<Node>  incoming;
  private final TreeSet<Formula> toBeDone;
  private final TreeSet<Formula> old;
  private final TreeSet<Formula> next;
  private BitSet         accepting;
  private final BitSet         right_of_untils;
  private Node           OtherTransitionSource;
  private int equivalenceId;

  public Node () {
    nodeId = Pool.assign();
    incoming = new TreeSet<Node>();
    toBeDone = new TreeSet<Formula>();
    old = new TreeSet<Formula>();
    next = new TreeSet<Formula>();
    OtherTransitionSource = null;
    accepting = new BitSet(accepting_conds);
    right_of_untils = new BitSet(accepting_conds);
  }

  public Node (TreeSet<Node> in, TreeSet<Formula> newForm, TreeSet<Formula> done, TreeSet<Formula> nx,
               BitSet acc, BitSet rous) {
    nodeId = Pool.assign();
    incoming = new TreeSet<Node>(in);
    toBeDone = new TreeSet<Formula>(newForm);
    old = new TreeSet<Formula>(done);
    next = new TreeSet<Formula>(nx);
    OtherTransitionSource = null;
    accepting = new BitSet(accepting_conds);
    accepting.or(acc);
    right_of_untils = new BitSet(accepting_conds);
    right_of_untils.or(rous);
  }

  public static int getAcceptingConds () {
    return accepting_conds;
  }

  public static Node createInitial (Formula form) {
    accepting_conds = form.initialize(); // first mark right forms of untils;

    //    System.out.println("Accepting conditions: " + accepting_conds);
    Node init = new Node();
    init.nodeId = 0;

    if (form.getContent() != 't') {
      init.decompose_ands_for_next(form);
    }

    return init;
  }

  public static void reset_static () {
    accepting_conds = 0;
    init_collapsed = false;
  }

  public TreeSet<Formula> getField_next () {
    return next;
  }

  public TreeSet<Formula> getField_old () {
    return old;
  }

  public int getId () {
    return nodeId;
  }

  public boolean isInitial () {
    return nodeId == 0;
  }

  public int getNodeId () {
    return nodeId;
  }

  public void RTstructure (State[] RTautomaton) {
    boolean safety = false;

    if (RTautomaton[nodeId] == null) {
      RTautomaton[nodeId] = new State(accepting, equivalenceId);
    } else {
      RTautomaton[nodeId].update_acc(accepting, equivalenceId);
    }

    if (is_safety_acc_node()) {
      RTautomaton[nodeId].update_safety_acc(true);
      safety = true;
    }

    Node Alternative = this;

    while (Alternative != null) {
      Iterator<Node> iterIncom = Alternative.incoming.iterator();
      Node     nextNode;

      while (iterIncom.hasNext()) {
        nextNode = iterIncom.next();

        int stateId = nextNode.getId();

        if (RTautomaton[stateId] == null) {
          RTautomaton[stateId] = new State();
        }

        RTautomaton[stateId].add(
              new Transition(Alternative.old, equivalenceId, accepting, safety));
      }

      Alternative = Alternative.OtherTransitionSource;
    }
  }

  public int compareTo (Node f) {
    if (this == f) {
      return 0;
    } else {
      return 1;
    }
  }

  public boolean compare_accepting (Node nd) {
    //if (nodeId == 0)
    //	System.out.println("Has it been collapsed yet? : " + init_collapsed);
    if ((nodeId == 0) && !init_collapsed) {
      // System.out.println("Potentially collapse " + nodeId + " with " + nd.nodeId);
      return true;
    }

    return (accepting.equals(nd.accepting)); // compare their BitSets
  }

  public void decompose_ands_for_next (Formula form) {
    if (form.getContent() == 'A') {
      decompose_ands_for_next(form.getSub1());
      decompose_ands_for_next(form.getSub2());
    } else if (is_redundant(next, null, form) == false) {
      next.add(form);
    }
  }

  public Automaton expand (Automaton states) {
    //		System.out.println("expand entered"); // debugging
    Node tempNode;

    if (toBeDone.isEmpty()) {
      if (nodeId != 0) {
        update_accepting();
      }


      // System.out.println("New is empty!");
      tempNode = states.alreadyThere(this);

      if (tempNode != null) {
        // System.out.println("Node " + nodeId + " collapsed with " + tempNode.nodeId);
        tempNode.modify(this);

        return states;
      } else {
        Node NewN = new Node();
        NewN.incoming.add(this);
        NewN.toBeDone.addAll(next);

        states.add(this);

        return (NewN.expand(states));
      }
    } else // toBeDone is not empty
    {
      Formula temp_form;
      Formula ita = toBeDone.first();
      toBeDone.remove(ita);

      //System.out.println("\n\nExpanding " + ita + " for node " + nodeId);
      if (testForContradictions(ita)) {
        //System.out.println("Finished expand - contradiction");
        return states;
      }

      // no contradiction
      // look in tech report why we do this even when ita is redundant
      if (ita.is_right_of_until(accepting_conds)) {
        right_of_untils.or(ita.get_rightOfWhichUntils());
      }

      TreeSet<Formula> set_checked_against = new TreeSet<Formula>();
      set_checked_against.addAll(old);
      set_checked_against.addAll(toBeDone);

      if (is_redundant(set_checked_against, next, ita)) {
        return expand(states);
      }

      // not redundant either
      // look in tech report why this only when not redundant
      if (ita.getContent() == 'U') { // this is an until formula
        accepting.set(ita.get_untils_index());

        //      	System.out.println("Just set an eventuality requirement");
      }

      if (!ita.is_literal()) {
        switch (ita.getContent()) {
        case 'U':
        case 'W':
        case 'V':
        case 'O':

          Node node2 = split(ita);

          return node2.expand(this.expand(states));

        case 'X':
          decompose_ands_for_next(ita.getSub1());

          return expand(states);

        case 'A':
          temp_form = ita.getSub1();

          if (!old.contains(temp_form)) {
            toBeDone.add(temp_form);
          }

          temp_form = ita.getSub2();

          if (!old.contains(temp_form)) {
            toBeDone.add(temp_form);
          }

          return expand(states);

        default:
          System.out.println("default case of switch entered");

          return null;
        }
      } else // ita represents a literal
      {
        //	System.out.println("Now working on literal " + ita.getContent());
        // must do a test for contradictions first
        if (ita.getContent() != 't') {
          old.add(ita);
        }

        //	System.out.println("added to " + nodeId + " formula " + ita);
        return (expand(states));
      }
    }
  }

  public int get_equivalenceId () {
    return equivalenceId;
  }

  public void set_equivalenceId (int value) {
    equivalenceId = value;
  }

  public void update_accepting () {
    accepting.andNot(right_of_untils);

    // just do now the bitwise or so that accepting gets updated
  }

  private static boolean is_redundant (TreeSet<Formula> main_set, TreeSet<Formula> next_set,
                                       Formula ita) {
    if ((ita.is_special_case_of_V(main_set)) || // my addition - correct???
        ((ita.is_synt_implied(main_set, next_set)) &&
              (!(ita.getContent() == 'U') ||
                (ita.getSub2().is_synt_implied(main_set, next_set))))) {
      //System.out.println("Looks like formula was redundant");
      return true;
    } else {
      return false;
    }
  }

  private boolean is_safety_acc_node () {
    if (next.isEmpty()) {
      return true;
    }

    Iterator<Formula> iterNext = next.iterator();
    Formula  nextForm = null;

    // all formulas present must be of type V or W, otherwise false
    while (iterNext.hasNext()) {
      nextForm = iterNext.next();

      if ((nextForm.getContent() != 'V') && (nextForm.getContent() != 'W')) {
        return false;
      }
    }

    return true;
  }

  private void modify (Node current) {
    boolean match = false;
    Node    Tail = this;
    Node    Alternative = this;

    if ((this.nodeId == 0) && !init_collapsed) {
      accepting = current.accepting;
      init_collapsed = true;
    }

    while (Alternative != null) {
      if (Alternative.old.equals(current.old)) {
        Alternative.incoming.addAll(current.incoming);
        match = true;
      }

      Tail = Alternative;
      Alternative = Alternative.OtherTransitionSource;
    }

    if (!match) {
      Tail.OtherTransitionSource = current;
    }
  }

  private Node split (Formula form) {
    //System.out.println("Split is entered");
    Formula temp_form;

    // first create Node 2
    Node Node2 = new Node(this.incoming, this.toBeDone, this.old, this.next,
                          this.accepting, this.right_of_untils);

    temp_form = form.getSub2();

    if (!old.contains(temp_form)) //New2(n) not in old

    {
      Node2.toBeDone.add(temp_form);
    }

    if (form.getContent() == 'V') // both subformulas are added to New2
    {
      temp_form = form.getSub1();

      if (!old.contains(temp_form)) // subformula not in old

      {
        Node2.toBeDone.add(temp_form);
      }
    }


    // then substitute current Node with Node 1
    temp_form = form.getSub1();

    if (!old.contains(temp_form)) //New1(n) not in old

    {
      toBeDone.add(temp_form);
    }

    temp_form = form.getNext();

    if (temp_form != null) {
      decompose_ands_for_next(temp_form);
    }

    /* following lines are probably unecessary because we never split literals!*/
    if (form.is_literal())/* because we only store literals... */
    {
      old.add(form);
      System.out.println("added " + form); // never supposed to see that
      Node2.old.add(form);
    }

    //System.out.println("Node split into itself and node : " + Node2.nodeId);
    //print();
    //Node2.print();
    return Node2;
  }

  private boolean testForContradictions (Formula ita) {
    Formula Not_ita = ita.negate();

    if (Not_ita.is_synt_implied(old, next)) {
      return true;
    } else {
      return false;
    }
  }
}