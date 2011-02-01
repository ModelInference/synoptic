package synoptic.invariants.ltlchecker;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import synoptic.benchmarks.PerformanceMetrics;
import synoptic.benchmarks.TimedTask;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.RelationPath;
import synoptic.invariants.ltlcheck.Counterexample;
import synoptic.invariants.ltlcheck.IModelCheckingMonitor;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;



import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Node;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.trans.ParseErrorException;

public class GraphLTLChecker<T extends INode<T>> {
	private static final boolean DEBUG = false;
	/**
	 * Cache for that last target graphs.
	 */
	private HashMap<String, Graph> lastTargetGraph = new HashMap<String, Graph>();
	//CACHE:
	/**
	 * Cache for the last source graphs.
	 */
	private HashMap<String, IGraph<T>> lastSourceGraph = new HashMap<String, IGraph<T>>();
	//CACHE:

	/**
	 * Checks the formula after preprocessing it. So it's allowed to have things
	 * in it like WFAIR(a).
	 *
	 * @param sourceGraph
	 *            - The expression to check (it has to be evaluated before!)
	 * @param invariant
	 *            - the formula to check
	 * @return a counter example or <code>null</code> if the formula is
	 *         satisfied
	 * @throws ParseErrorException
	 */
	public Counterexample check(IGraph<T> sourceGraph,
			ITemporalInvariant invariant, IModelCheckingMonitor monitor)
			throws ParseErrorException {
		if (monitor == null) {
			monitor = new IModelCheckingMonitor() {
				public void subTask(String str) {
					System.out.println(str);
				}
			};
		}

		// formula = LTLFormulaPreprocessor.preprocessFormula(formula);
		// monitor.subTask("Preprocessed LTL formula: " + formula);
		TimedTask transToMC = PerformanceMetrics.createTask("transToMC");
		Graph targetGraph = null;
		String relation = invariant.getRelation();
		if (lastSourceGraph.containsKey(relation)
				&& lastSourceGraph.get(relation).equals(sourceGraph)) {
			targetGraph = lastTargetGraph.get(relation);
		}


		if (lastSourceGraph.size() > 5) {
			lastSourceGraph.clear();
			lastTargetGraph.clear();
		}


		if (targetGraph == null) {
			monitor.subTask("Building CCS Graph...");
			targetGraph = convertGraph(sourceGraph, relation);

			lastSourceGraph.put(relation, sourceGraph);
			lastTargetGraph.put(relation, targetGraph);
		}
		transToMC.stop();
		if (DEBUG) {
			GraphVizExporter v = new GraphVizExporter();
			try {
				v.exportAsDotAndPng("output/sourceGraph-" + relation + ".dot",
						sourceGraph);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writeDot(targetGraph, "output/targetGraph-" + relation + ".dot");
		}
		// run synoptic.model-checker for this graph structure
		Counterexample c = synoptic.invariants.ltlcheck.LtlModelChecker.check(
				targetGraph, invariant, monitor);

		return c;
	}

	// TODO: refactor this code to instead use the GraphVizExporter
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
					p
							.println(e.getSource().hashCode() + " -> "
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

	private Graph convertGraph(IGraph<T> sourceGraph,
			String relation) {
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
			for (Iterator<? extends ITransition<T>> i = m
					.getTransitionsIterator(relation); i.hasNext();) {
				ITransition<T> t = i.next();
				T n = t.getTarget();
				if (!prevStates.containsKey(n))
					prevStates.put(n, new HashSet<Node>());
				prevStates.get(n).add(nextState.get(t.getSource()));
			}
		}
		for (T m : allNodes) {
			if (prevStates.get(m) == null)
				throw new InternalSynopticException("null in prevStates");
			if (nextState.get(m) == null)
				throw new InternalSynopticException("null in nextState");
			for (Node prev : prevStates.get(m)) {
				Edge e = new Edge(prev, nextState.get(m), "-", m.getLabel(),
						null);
				e.setAttribute("inode", m);
			}
		}
		// System.out.println(targetGraph.getEdgeCount());
		return targetGraph;
	}


	@SuppressWarnings("unchecked")
	private ArrayList<T> convertCounterexample(Counterexample c) {
		ArrayList<T> list = new ArrayList<T>();
		for (Edge e : c.getPrefix()) {
			//System.out.println(e.getSource().getAttribute("post") + " -> "
			//		+ e.getNext().getAttribute("post"));
			T inode = (T) e.getAttribute("inode");
			if (inode == null) {
				// Translation done in synoptic.invariants.ltlcheck.LtlModelChecker.check
				// inserts artificial start and end nodes, which have no inode
				// attribute. We ignore them.
				continue;
			}
			list.add(inode);
		}
		for (Edge e : c.getCycle()) {
			T inode = (T) e.getAttribute("inode");
			if (inode == null) {
				// Translation done in synoptic.invariants.ltlcheck.LtlModelChecker.check
				// inserts artificial start and end nodes, which have no inode
				// attribute. We ignore them.
				continue;
			}
			list.add(inode);
		}

		return list;
	}

	/**
	 * Returns a counter-example path that violates a specific invariant in a
	 * graph.
	 *
	 * @param <T> the type of nodes in graph g
	 * @param inv invariant for which we are to find a violating path
	 * @param g the graph within which the violating path must be found
	 * @return a path in g that violates inv or null if one doesn't exist
	 */
	public RelationPath<T> getCounterExample(ITemporalInvariant inv, IGraph<T> g) {
		RelationPath<T> r = null;
		try {
			Counterexample ce = this.check(g, inv,
					new IModelCheckingMonitor() {
						public void subTask(String str) {
						}
					});
			if (ce == null)
				return null;
			ArrayList<T> trace = this.convertCounterexample(ce);
			if (trace != null) {
				r = new RelationPath<T>(inv, inv.shorten(trace));
				if (r.path == null) {
					throw new InternalSynopticException("shortening returned null for " + inv
									+ " and trace " + trace);
				}
			}
		} catch (ParseErrorException e) {
			e.printStackTrace();
		}
		return r;
	}

}
