package invariants.ltlchecker;

import invariants.TemporalInvariant;
import invariants.ltlcheck.Counterexample;
import invariants.ltlcheck.IModelCheckingMonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import model.Action;
import model.export.GraphVizExporter;
import model.interfaces.IGraph;
import model.interfaces.INode;
import model.interfaces.ITransition;

import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Node;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.trans.ParseErrorException;

public class GraphLTLChecker<T extends INode<T>>  {
	private static final boolean DEBUG = false;
	private HashMap<Action, Graph> lastTargetGraph = new HashMap<Action, Graph>();
	private HashMap<Action, IGraph<T>> lastSourceGraph = new HashMap<Action, IGraph<T>>();

	/**
	 * Checks the formula after preprocessing it. So it's allowed to have things
	 * in it like WFAIR(a).
	 * 
	 * @param exp
	 *            - The expression to check (it has to be evaluated before!)
	 * @param invariant
	 *            - the formula to check
	 * @return a counter example or <code>null</code> if the formula is
	 *         satisfied
	 * @throws ParseErrorException
	 */
	public Counterexample check(model.interfaces.IGraph<T> sourceGraph, TemporalInvariant invariant,
			IModelCheckingMonitor monitor) throws ParseErrorException {
		if (monitor == null) {
			monitor = new IModelCheckingMonitor() {
				public void subTask(String str) {
					System.out.println(str);
				}
			};
		}

		//formula = LTLFormulaPreprocessor.preprocessFormula(formula);
		//monitor.subTask("Preprocessed LTL formula: " + formula);

		Graph targetGraph = null;
		Action relation = invariant.getRelation();
		if (lastSourceGraph.containsKey(relation) && lastSourceGraph.get(relation).equals(sourceGraph)) {
			targetGraph = lastTargetGraph.get(relation);
		}

		if (targetGraph == null) {
			monitor.subTask("Building CCS Graph...");
			targetGraph = convertGraph(sourceGraph, relation);

			lastSourceGraph.put(relation, sourceGraph);
			lastTargetGraph.put(relation, targetGraph);
		}
		if (DEBUG) {
			GraphVizExporter v = new GraphVizExporter();
			try {
				v.exportAsDotAndPng("output/sourceGraph-"+relation+".dot", sourceGraph);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writeDot(targetGraph, "output/targetGraph-"+relation+".dot");
		}
		// run model-checker for this graph structure
		Counterexample c = invariants.ltlcheck.LtlModelChecker.check(
				targetGraph, invariant, monitor);

		return c;
	}

	@SuppressWarnings("unchecked")
	public void writeDot(Graph g, String filename) {
		try {
			File f = new File(filename);
			PrintWriter p = new PrintWriter(new FileOutputStream(f));
			p.println("digraph {");

			for (Node m : g.getNodes()) {
				p.println(m.hashCode() + " [label=\"" + m.getAttribute("post")
						+ "\"]; ");
			}

			for (Node n : g.getNodes()) {
				for (Edge e : n.getOutgoingEdges()) {
					p.println(e.getSource().hashCode() + " -> "
							+ e.getNext().hashCode() + " [label=\""
							+ ((T) e.getAttribute("inode")).getLabel()
							+ "\"];");
				}
			}

			p.println("}");
			p.close();
			GraphVizExporter v = new GraphVizExporter();
			v.exportPng(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Graph convertGraph(model.interfaces.IGraph<T> sourceGraph, Action relation) {
		Graph targetGraph = new Graph();

		Set<T> initialMessages = sourceGraph.getInitialNodes(relation);
		Set<T> allNodes = sourceGraph.getNodes();
		Node initialState = new Node(targetGraph);
		initialState.setAttribute("post", "P:initial");
		HashMap<T, Node> nextState = new HashMap<T, Node>();
		HashMap<T, Set<Node>> prevStates = new HashMap<T, Set<Node>>();

		for (T initialMessage : initialMessages) {
			if (!prevStates.containsKey(initialMessage))
				prevStates.put(initialMessage, new HashSet<Node>());
			prevStates.get(initialMessage).add(initialState);
		}

		for (T m : allNodes) {
			Node n = new Node(targetGraph);
			nextState.put(m, n);
			n.setAttribute("post", "P:" + m.getLabel());
		}

		for (T m : allNodes) {
			for (Iterator<? extends ITransition<T>> i = m.getTransitionsIterator(relation); i
					.hasNext();) {
				ITransition<T> t = i.next();
				T n = t.getTarget();
				if (!prevStates.containsKey(n))
					prevStates.put(n, new HashSet<Node>());
				prevStates.get(n).add(nextState.get(t.getSource()));
			}
		}
		for (T m : allNodes) {
			if (prevStates.get(m) == null || nextState.get(m) == null)
				throw new RuntimeException("got the null");
			for (Node prev : prevStates.get(m)) {
				Edge e = new Edge(prev, nextState.get(m), "-", m.getLabel(),
						null);
				e.setAttribute("inode", m);
			}
		}
		System.out.println(targetGraph.getEdgeCount());
		return targetGraph;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<T> convertCounterexample(Counterexample c) {
		ArrayList<T> list = new ArrayList<T>();
		for (Edge e : c.getPrefix()) {
			T inode = (T) e.getAttribute("inode");
			assert inode != null;
			list.add(inode);
		}
		for (Edge e : c.getCycle()) {
			T inode = (T) e.getAttribute("inode");
			assert inode != null;
			list.add(inode);
		}

		return list;
	}

}
