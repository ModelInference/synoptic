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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * DOCUMENT ME!
 */
public class Node {

	private Graph graph;

	private List<Edge> outgoingEdges;

	private List<Edge> incomingEdges;

	private Attributes attributes;

	public Node(Graph g, Attributes a) {
		init(g, a);
	}

	public Node(Graph g) {
		init(g, null);
	}

	public Node(Node n) {
		init(n.graph, new Attributes(n.attributes));

		for (Iterator<Edge> i = n.outgoingEdges.iterator(); i.hasNext();) {
			new Edge(this, i.next());
		}

		for (Iterator<Edge> i = n.incomingEdges.iterator(); i.hasNext();) {
			new Edge(i.next(), this);
		}
	}

	public synchronized void setAttributes(Attributes a) {
		int id = getId();
		attributes = new Attributes(a);
		setId(id);
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public synchronized void setAttribute(String name, Object value) {
		if (name.equals("_id")) {
			return;
		}

		attributes.set(name, value);
	}

	public synchronized void setBooleanAttribute(String name, boolean value) {
		if (name.equals("_id")) {
			return;
		}

		attributes.setBoolean(name, value);
	}

	public boolean getBooleanAttribute(String name) {
		return attributes.getBoolean(name);
	}

	public Graph getGraph() {
		return graph;
	}

	public synchronized int getId() {
		return attributes.getInt("_id");
	}

	public int getIncomingEdgeCount() {
		return outgoingEdges.size();
	}

	public List<Edge> getIncomingEdges() {
		return new LinkedList<Edge>(incomingEdges);
	}

	public synchronized void setIntAttribute(String name, int value) {
		if (name.equals("_id")) {
			return;
		}

		attributes.setInt(name, value);
	}

	public int getIntAttribute(String name) {
		return attributes.getInt(name);
	}

	public int getOutgoingEdgeCount() {
		return outgoingEdges.size();
	}

	public List<Edge> getOutgoingEdges() {
		return new LinkedList<Edge>(outgoingEdges);
	}

	public synchronized void setStringAttribute(String name, String value) {
		if (name.equals("_id")) {
			return;
		}

		attributes.setString(name, value);
	}

	public String getStringAttribute(String name) {
		return attributes.getString(name);
	}

	public synchronized void forAllEdges(Visitor v) {
		for (Iterator<Edge> i = new LinkedList<Edge>(outgoingEdges).iterator(); i.hasNext();) {
			v.visitEdge(i.next());
		}
	}

	public synchronized void remove() {
		for (Iterator<Edge> i = new LinkedList<Edge>(outgoingEdges).iterator(); i.hasNext();) {
			(i.next()).remove();
		}

		for (Iterator<Edge> i = new LinkedList<Edge>(incomingEdges).iterator(); i.hasNext();) {
			(i.next()).remove();
		}

		graph.removeNode(this);
	}

	synchronized void setId(int id) {
		attributes.setInt("_id", id);
	}

	synchronized void addIncomingEdge(Edge e) {
		incomingEdges.add(e);
	}

	synchronized void addOutgoingEdge(Edge e) {
		outgoingEdges.add(e);
	}

	synchronized void removeIncomingEdge(Edge e) {
		incomingEdges.remove(e);
	}

	synchronized void removeOutgoingEdge(Edge e) {
		outgoingEdges.remove(e);
	}

	// Modified by robbyjo - Jul 15, 2002
	void save(PrintStream out, int format) {
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

	private void init(Graph g, Attributes a) {
		graph = g;

		if (a == null) {
			attributes = new Attributes();
		} else {
			attributes = a;
		}

		incomingEdges = new LinkedList<Edge>();
		outgoingEdges = new LinkedList<Edge>();

		graph.addNode(this);
	}

	// Modified by ckong - Sept 7, 2001
	private void save_fsp(PrintStream out) {
		///System.out.print("S" + getId() + "=(");
		out.print("S" + getId() + "=(");

		for (Iterator<Edge> i = outgoingEdges.iterator(); i.hasNext();) {
			(i.next()).save(out, Graph.FSP_FORMAT);

			if (i.hasNext()) {
				//System.out.print(" |");
				out.print(" |");
			}
		}

		//System.out.print(")");
		out.print(")");
	}

	private void save_sm(PrintStream out) {
		int id = getId();
		out.print("  ");
		out.println(outgoingEdges.size());
		attributes.unset("_id");
		out.print("  ");
		out.println(attributes);
		setId(id);

		for (Iterator<Edge> i = outgoingEdges.iterator(); i.hasNext();) {
			(i.next()).save(out, Graph.SM_FORMAT);
		}
	}

	// robbyjo's contribution
	private void save_spin(PrintStream out) {
		String ln = System.getProperty("line.separator");
		String lntab = ln + "     :: ";

		if (getBooleanAttribute("accepting")) {
			out.print("accept_");
		}

		out.print("S" + getId() + ":" + ln + "     if" + lntab);

		for (Iterator<Edge> i = outgoingEdges.iterator(); i.hasNext();) {
			Edge e = i.next();
			e.save(out, Graph.SPIN_FORMAT);

			if (i.hasNext()) {
				out.print(lntab);
			}
		}

		out.print(ln + "     fi;\n");
	}

	private void save_xml(PrintStream out) {
		int id = getId();
		out.println("<node id=\"" + id + "\">");
		attributes.unset("_id");
		attributes.save(out, Graph.XML_FORMAT);
		setId(id);

		for (Iterator<Edge> i = outgoingEdges.iterator(); i.hasNext();) {
			(i.next()).save(out, Graph.XML_FORMAT);
		}

		out.println("</node>");
	}
}