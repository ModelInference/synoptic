
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

import java.io.PrintStream;
import java.util.StringTokenizer;


/**
 * DOCUMENT ME!
 */
public class Edge {
  private Node       source;
  private Node       next;
  private String     guard;
  private String     action;
  private Attributes attributes;

  public Edge (Node s, Node n, String g, String a, Attributes as) {
    init(s, n, g, a, as);
  }

  public Edge (Node s, Node n, String g, String a) {
    init(s, n, g, a, null);
  }

  public Edge (Node s, Node n, String g) {
    init(s, n, g, "-", null);
  }

  public Edge (Node s, Node n) {
    init(s, n, "-", "-", null);
  }

  public Edge (Node s, Edge e) {
    init(s, e.next, new String(e.guard), new String(e.action),
         new Attributes(e.attributes));
  }

  public Edge (Edge e, Node n) {
    init(e.source, n, new String(e.guard), new String(e.action),
         new Attributes(e.attributes));
  }

  public Edge (Edge e) {
    init(e.source, e.next, new String(e.guard), new String(e.action),
         new Attributes(e.attributes));
  }

  public String getAction () {
    return action;
  }

  public synchronized void setAttributes (Attributes a) {
    attributes = new Attributes(a);
  }

  public Attributes getAttributes () {
    return attributes;
  }

  public void setAttribute(String name, Object value)
  {
  	attributes.set(name, value);
  }

  public Object getAttribute(String name)
  {
  	return attributes.get(name);
  }

  public void setBooleanAttribute (String name, boolean value) {
    attributes.setBoolean(name, value);
  }

  public boolean getBooleanAttribute (String name) {
    return attributes.getBoolean(name);
  }

  public String getGuard () {
    return guard;
  }

  public void setIntAttribute (String name, int value) {
    attributes.setInt(name, value);
  }

  public int getIntAttribute (String name) {
    return attributes.getInt(name);
  }

  public Node getNext () {
    return next;
  }

  public Node getSource () {
    return source;
  }

  public void setStringAttribute (String name, String value) {
    attributes.setString(name, value);
  }

  public String getStringAttribute (String name) {
    return attributes.getString(name);
  }

  public synchronized void remove () {
    source.removeOutgoingEdge(this);
    next.removeIncomingEdge(this);
  }

  // Modified by robbyjo - Jul 15, 2002
  void save (PrintStream out, int format) {
    switch (format) {
    case Graph.SM_FORMAT:
      save_sm(out);

      break;

    case Graph.FSP_FORMAT:
      save_fsp(out);

      break;

    case Graph.XML_FORMAT:
      save_xml(out);

      break;

    case Graph.SPIN_FORMAT:
      save_spin(out);

      break;

    default:
      throw new RuntimeException("Unknown format!");
    }
  }

  private void init (Node s, Node n, String g, String a, Attributes as) {
    source = s;
    next = n;
    guard = g;
    action = a;

    if (as == null) {
      attributes = new Attributes();
    } else {
      attributes = as;
    }

    s.addOutgoingEdge(this);
    n.addIncomingEdge(this);
  }

  // Modified by ckong - Sept 7, 2001
  private void save_fsp (PrintStream out) {
    String g;
    String accs = "";

    if (guard.equals("-")) {
      g = "TRUE";
    } else {
      g = guard;
    }

    int nsets = source.getGraph().getIntAttribute("nsets");

    if (nsets == 0) {
      if (getBooleanAttribute("accepting")) {
        accs = "@";
      }
    } else {
      boolean      first = true;
      StringBuffer sb = new StringBuffer();

      for (int i = 0; i < nsets; i++) {
        if (getBooleanAttribute("acc" + i)) {
          if (first) {
            first = false;
          } else {
            sb.append(",");
          }

          sb.append(i);
        }
      }

      if (!first) {
        accs = "{" + sb.toString() + "}";
      }
    }


    //System.out.print(g + " -" + accs + "-> S" + next.getId());
    out.print(g + accs + "-> S" + next.getId());
  }

  private void save_sm (PrintStream out) {
    out.print("    ");
    out.println(next.getId());
    out.print("    ");
    out.println(guard);
    out.print("    ");
    out.println(action);
    out.print("    ");
    out.println(attributes);
  }

  // robbyjo's contribution
  private void save_spin (PrintStream out) {
    String          g = guard.equals("-") ? "1" : guard;
    String          accs = "";

    StringTokenizer tok = new StringTokenizer(new String(g), "&");
    g = "";

    while (tok.hasMoreTokens()) {
      g += tok.nextToken();

      if (tok.hasMoreTokens()) {
        g += " && ";
      }
    }

    tok = new StringTokenizer(new String(g), "|");
    g = "";

    while (tok.hasMoreTokens()) {
      g += tok.nextToken();

      if (tok.hasMoreTokens()) {
        g += " || ";
      }
    }

    int nsets = source.getGraph().getIntAttribute("nsets");

    if (nsets == 0) {
      if (getBooleanAttribute("accepting")) {
        accs = "@";
      }
    } else {
      boolean      first = true;
      StringBuffer sb = new StringBuffer();

      for (int i = 0; i < nsets; i++) {
        if (getBooleanAttribute("acc" + i)) {
          if (first) {
            first = false;
          } else {
            sb.append(",");
          }

          sb.append(i);
        }
      }

      if (!first) {
        accs = "{" + sb.toString() + "}";
      }
    }

    out.print("(" + g + ") " + accs + "-> goto ");

    if (next.getBooleanAttribute("accepting")) {
      out.print("accept_");
    }

    out.print("S" + next.getId());
  }

  private void save_xml (PrintStream out) {
    out.println("<transition to=\"" + next.getId() + "\">");

    if (!guard.equals("-")) {
      out.println("<guard>" + xml_quote(guard) + "</guard>");
    }

    if (!action.equals("-")) {
      out.println("<action>" + xml_quote(action) + "</action>");
    }

    attributes.save(out, Graph.XML_FORMAT);
    out.println("</transition>");
  }

  private String xml_quote (String s) {
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);

      switch (ch) {
      case '&':
        sb.append("&amp;");

        break;

      case '<':
        sb.append("&lt;");

        break;

      case '>':
        sb.append("&gt;");

        break;

      default:
        sb.append(ch);

        break;
      }
    }

    return sb.toString();
  }
}