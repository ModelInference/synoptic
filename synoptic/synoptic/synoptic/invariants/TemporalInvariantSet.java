package synoptic.invariants;

import gov.nasa.ltl.trans.ParseErrorException;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.benchmarks.PerformanceMetrics;
import synoptic.benchmarks.TimedTask;
import synoptic.invariants.fsmcheck.FsmModelChecker;
import synoptic.invariants.ltlcheck.Counterexample;
import synoptic.invariants.ltlcheck.IModelCheckingMonitor;
import synoptic.invariants.ltlchecker.GraphLTLChecker;
import synoptic.main.Main;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.IEvent;
import synoptic.model.MessageEvent;
import synoptic.model.Partition;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.input.GraphBuilder;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;

//import daikonizer.Daikonizer;






public class TemporalInvariantSet implements Iterable<TemporalInvariant> {
	private static Logger logger = Logger.getLogger("TemporalInvSet Logger");
	
	/**
	 * Enable Daikon support to extract structural synoptic.invariants (alpha)
	 */
	public static boolean generateStructuralInvariants = false;
	LinkedHashSet<TemporalInvariant> invariants = new LinkedHashSet<TemporalInvariant>();
	
	/**
	 * Model check that every mined invariant actually holds.
	 */
	static final boolean DOUBLECKECK_MINING = false;

	public TemporalInvariantSet() {
	}

	public TemporalInvariantSet(Set<TemporalInvariant> invariants) {
		this.invariants.addAll(invariants);
	}

	public Set<TemporalInvariant> getSet() {
		return invariants;
	}

	public <T extends INode<T>> boolean check(IGraph<T> g) throws Exception {
		TemporalInvariantSet set = computeInvariants(g);
		boolean result = set.invariants.containsAll(invariants);
		if (!result) {
			logger.info(getUnsatisfiedInvariants(g).toString());
		}
		return result;
	}

	public <T extends INode<T>> TemporalInvariantSet getUnsatisfiedInvariants(
			IGraph<T> g) throws Exception {
		TemporalInvariantSet set = computeInvariants(g);
		TemporalInvariantSet res = new TemporalInvariantSet();
		res.invariants.addAll(invariants);
		res.invariants.removeAll(set.invariants);
		return res;
	}

	public void addAll(Collection<TemporalInvariant> invariants) {
		this.invariants.addAll(invariants);
	}

	public void add(TemporalInvariantSet set) {
		this.invariants.addAll(set.invariants);
	}

	public Iterator<TemporalInvariant> iterator() {
		return invariants.iterator();
	}

	public String toString() {
		return invariants.toString();
	}

	public void add(TemporalInvariant inv) {
		invariants.add(inv);
	}

	public static class RelationPath<T> {
		public TemporalInvariant invariant;
		public List<T> path;

		public String toString() {
			//return invariant.toString();
			StringBuilder result = new StringBuilder();
			result.append(invariant.toString());
			result.append(": ");
			for (T n : path) {
				result.append(((Partition)n).getLabel());
				result.append(" ");
			}
			return result.toString();
		}
	}

	private <T extends INode<T>> RelationPath<T> getCounterExample(
			TemporalInvariant inv, IGraph<T> g, GraphLTLChecker<T> c) {
		RelationPath<T> r = new RelationPath<T>();
		try {
			Counterexample ce = c.check(g, inv,
					new IModelCheckingMonitor() {
						public void subTask(String str) {
						}
					});
			if (ce == null)
				return null;
			r.invariant = inv;
			List<T> trace = c.convertCounterexample(ce);
			if (trace != null) {
				// System.out.println(i.toString() + trace);
				r.path = inv.shorten(trace);
				if (r.path == null) {
					throw new RuntimeException(
							"shortening returned null for " + inv
									+ " and trace " + trace);
				}
				// System.out.println(r.path);
			}
		} catch (ParseErrorException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	public static boolean compare = false;

	public <T extends INode<T>> List<BinaryInvariant> getViolated(List<TemporalInvariant> invs, IGraph<T> graph) {
		List<BinaryInvariant> bitSetInput = new ArrayList<BinaryInvariant>();
		for (TemporalInvariant tinv : invs) {
			bitSetInput.add((BinaryInvariant)tinv);
		}
		return FsmModelChecker.runBitSetChecker(bitSetInput, graph);
	}
	
	public <T extends INode<T>> List<RelationPath<T>> compareViolations(List<TemporalInvariant> invs, IGraph<T> graph) {		
		List<RelationPath<T>> paths = new ArrayList<RelationPath<T>>();
		GraphLTLChecker<T> ch = new GraphLTLChecker<T>();
		
		if (!compare) {
			if (invs.size() > 5) {
				List<BinaryInvariant> viol = this.getViolated(invs, graph);
				for (BinaryInvariant inv : viol) {
					RelationPath<T> path = FsmModelChecker.invariantCounterexample(inv, graph);
					assert(path != null); // same behavior as bitset checker
					paths.add(path);
				}
			} else {
				for (TemporalInvariant tinv : invs) {
					RelationPath<T> path = FsmModelChecker.invariantCounterexample((BinaryInvariant)tinv, graph);
					if (path != null) paths.add(path);
				}
			}
			return paths;
		}
		List<BinaryInvariant> violated = this.getViolated(invs, graph);
		for (int i = 0; i < invs.size(); i++) {
			BinaryInvariant inv = (BinaryInvariant) invs.get(i);
			RelationPath<T> path = this.getCounterExample(inv, graph, ch);
			RelationPath<T> fsm_path = FsmModelChecker.invariantCounterexample(inv, graph);
			if ((fsm_path == null) != (path == null)) {
				System.out.println("value deviates from cannonical in " + inv.toString());
			} else if (fsm_path != null) {
				System.out.println("both found " + inv);
				System.out.println("fsm_path.size = " + fsm_path.path.size());
				System.out.println("path.size = " + path.path.size());
				if (fsm_path.path.size() > path.path.size()) {
					System.out.println("that's curious..");
				}
				if (!path.path.get(path.path.size() - 1).isFinal()) {
					System.out.println("normal path doesn't end with final");
				}
			}
			if ((fsm_path != null) != violated.contains(inv)) {
				System.out.println("Bitset checker deviates from cannonical in " + inv.toString());
			}
			if (fsm_path != null) paths.add(fsm_path);
		}
		return paths;
	}
	
	public <T extends INode<T>> RelationPath<T> getViolation(
			TemporalInvariant inv, IGraph<T> g) {
		TimedTask refinement = PerformanceMetrics.createTask("getViolation", true);
		try {
			if (Main.useFSMChecker) {
				List<TemporalInvariant> invs = new ArrayList<TemporalInvariant>();
				invs.add(inv);
				List<RelationPath<T>> paths = compareViolations(invs, g);
				if (paths.isEmpty()) return null; 
				return paths.get(0);
			} else {
				return getCounterExample(inv, g, new GraphLTLChecker<T>());
			}
		} finally {
			refinement.stop();
		}
	}

	public <T extends INode<T>> List<RelationPath<T>> getViolations(IGraph<T> g) {
		TimedTask violations = PerformanceMetrics.createTask("getViolations", false);
		try {
			List<RelationPath<T>> paths = null;
			if (Main.useFSMChecker) {
				/*
				//BitSet failures = c.whichFail();
				BitSet all = new BitSet();
				all.set(0, c.invariantCount(), true);
				paths = c.findFailures(all);
				*/
				paths = this.compareViolations(new ArrayList<TemporalInvariant>((Collection)invariants), g);
			} else {
				paths = new ArrayList<RelationPath<T>>();
				GraphLTLChecker<T> c = new GraphLTLChecker<T>();
				for (TemporalInvariant i : invariants) {
					RelationPath<T> path = getCounterExample(i, g, c);
					if (path != null)
						paths.add(path);
				}
			}

			if (paths.size() == 0)
				return null;

			Collections.sort(paths, new Comparator<RelationPath<T>>() {
				@Override
				public int compare(RelationPath<T> o1, RelationPath<T> o2) {
					return new Integer(o1.path.size())
							.compareTo(o2.path.size());
				}
			});

			return paths;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			violations.stop();
			System.out.println(violations.toString());
		}
	}

	/**
	 * Returns the first violation encountered in the graph g. The order of
	 * exploration is unspecified.
	 * 
	 * @param <T>
	 *            the node type
	 * @param g
	 *            the graph to check
	 * @return null if no violation is found, the counter-example path otherwise
	 */
	public <T extends INode<T>> RelationPath<T> getFirstViolation(IGraph<T> g) {
		TimedTask violations = PerformanceMetrics.createTask("getFirstViolation", false);
		try {
			if (Main.useFSMChecker) {
				/*
				BitSet failingInvariants = c.whichFail();
				failingInvariants.clear(failingInvariants.nextSetBit(0) + 1, failingInvariants.size());
				List<RelationPath<T>> results = c.findFailures(failingInvariants);
				*/
				List<RelationPath<T>> results = this.compareViolations(new ArrayList<TemporalInvariant>((Collection)invariants), g);
				if (results.isEmpty()) return null;
				return results.get(0);
			} else {
				GraphLTLChecker<T> c = new GraphLTLChecker<T>();
				for (TemporalInvariant i : invariants) {
					// List<Transition<Message>> path = i.check(g);
					RelationPath<T> result = getCounterExample(i, g, c);
					if (result != null) return result;
				}
			}
			return null;
		} finally {
			violations.stop();
		}
	}

	public boolean sameInvariants(TemporalInvariantSet set2) {
		boolean ret = invariants.containsAll(set2.invariants);
		boolean ret2 = set2.invariants.containsAll(invariants);
		if (!ret || !ret2) {
			ArrayList<TemporalInvariant> foo = new ArrayList<TemporalInvariant>();
			foo.addAll(invariants);
			foo.removeAll(set2.invariants);
			System.out.println("Not remotely contained: " + foo);
			foo = new ArrayList<TemporalInvariant>();
			foo.addAll(set2.invariants);
			foo.removeAll(invariants);
			System.out.println("Not locally contained: " + foo);
		}
		return ret && ret2;
	}

	/**
	 * Compute synoptic.invariants of the graph g. Enumerating all possibly synoptic.invariants
	 * syntactically, and then checking them was considered too costly (although
	 * we never benchmarked it!). So we are mining synoptic.invariants from the
	 * transitive closure using {@code extractInvariantsForAllRelations}, which
	 * is supposed to return an over-approximation of the synoptic.invariants that hold
	 * (i.e. it may return synoptic.invariants that do not hold, but may not fail to
	 * return an invariant that does not hold)
	 * 
	 * @param <T>
	 *            The node type of the graph
	 * @param g
	 *            the graph of nodes of type T
	 * @return the set of temporal synoptic.invariants the graph satisfies
	 * @throws Exception 
	 */
	static public <T extends INode<T>> TemporalInvariantSet computeInvariants(
			IGraph<T> g) {
		TimedTask mineInvariants = PerformanceMetrics.createTask(
				"mineInvariants", false);
		try {
//			if (DEBUG) {
//				GraphVizExporter v = new GraphVizExporter();
//				try {
//					v.exportAsDotAndPng("output/pre.dot", g);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			TimedTask itc = PerformanceMetrics.createTask(
					"invariants_transitive_closure", false);
			AllRelationsTransitiveClosure<T> transitiveClosure = new AllRelationsTransitiveClosure<T>(
					g);
//			if (DEBUG) {
//				GraphVizExporter v = new GraphVizExporter();
//				for (String relation : transitiveClosure.getRelations())
//					writeDot("output/post-" + relation + ".dot", g,
//							transitiveClosure.get(relation));
//				v.exportPng(new File("output/post.dot"));
//			}
			// get overapproximation
			itc.stop();
			if (Main.doBenchmarking)
				System.out.println(itc);
			TimedTask io = PerformanceMetrics.createTask(
					"invariants_approximation", false);
			TemporalInvariantSet overapproximatedInvariantsSet = extractInvariantsForAllRelations(
					g, transitiveClosure);
			io.stop();
			if (Main.doBenchmarking)
				System.out.println(io);
			int overapproximatedInvariantsSetSize = overapproximatedInvariantsSet
					.size();
			TimedTask iri = PerformanceMetrics.createTask(
					"invariants_remove_invalid", false);
			if (DOUBLECKECK_MINING) {
				List<RelationPath<T>> violations = overapproximatedInvariantsSet
						.getViolations(g);
				if (violations == null) {
					iri.stop();
					if (Main.doBenchmarking)
						System.out.println(iri);
					printStats(g, overapproximatedInvariantsSet,
							overapproximatedInvariantsSetSize);
					return overapproximatedInvariantsSet;
				}
				// Remove all synoptic.invariants that do not hold
				for (RelationPath<T> i : violations) {
					overapproximatedInvariantsSet.invariants.remove(i);
				}
			}
			iri.stop();
			if (Main.doBenchmarking)
				System.out.println(iri);
			printStats(g, overapproximatedInvariantsSet,
					overapproximatedInvariantsSetSize);
			return overapproximatedInvariantsSet;
		} finally {
			mineInvariants.stop();
		}
	}

	private static <T extends INode<T>> void printStats(IGraph<T> g,
			TemporalInvariantSet overapproximatedInvariantsSet,
			int overapproximatedInvariantsSetSize) {
		Set<String> labels = new HashSet<String>();
		for (T n : g.getNodes())
			labels.add(n.getLabel());
		int possibleInvariants = 3 /* invariant types */* labels.size()
				* labels.size() /* reflexive synoptic.invariants are allowed */;
		
		int percentReduction = possibleInvariants == 0 ? 0 :
			100 - (overapproximatedInvariantsSetSize * 100 / possibleInvariants);
		if (Main.doBenchmarking)
			System.out.println("" + overapproximatedInvariantsSet.size()
					+ " true synoptic.invariants, approximation guessed "
					+ overapproximatedInvariantsSetSize
					+ ", max possible synoptic.invariants " + possibleInvariants + " ("
					+ percentReduction + "% reduction through approximation).");
		PerformanceMetrics.get().record("true_invariants",
				overapproximatedInvariantsSet.size());
		PerformanceMetrics.get().record("approx_invariants",
				overapproximatedInvariantsSetSize);
		PerformanceMetrics.get().record("max_possible_invariants",
				possibleInvariants);
		PerformanceMetrics.get().record("percentReduction", percentReduction);
	}

	/**
	 * Extract synoptic.invariants for all relations, iteratively. Since we are not
	 * considering synoptic.invariants over multiple relations, this is sufficient.
	 * 
	 * @param <T>
	 *            the node type of the graph
	 * @param g
	 *            the graph
	 * @param tcs
	 *            the transitive closure to mine synoptic.invariants from
	 * @return the mined synoptic.invariants
	 * @throws Exception 
	 */
	private static <T extends INode<T>> TemporalInvariantSet extractInvariantsForAllRelations(
			IGraph<T> g, AllRelationsTransitiveClosure<T> tcs) {
		TemporalInvariantSet invariants = new TemporalInvariantSet();
		for (String relation : g.getRelations()) {
			invariants.add(extractInvariants(g, tcs.get(relation), relation));
		}
		return invariants;
	}

	/**
	 * Extract an overapproximated set of synoptic.invariants from the transitive closure
	 * {@code tc} of the graph {@code g}.
	 * 
	 * @param <T>
	 *            the node type of the graph
	 * @param g
	 *            the graph
	 * @param tc
	 *            the transitive closure (of {@code g}) to mine synoptic.invariants from
	 * @param relation
	 *            the relation to consider for the synoptic.invariants
	 * @return the overapproximated set of synoptic.invariants
	 * @throws Exception 
	 */
	private static <T extends INode<T>> TemporalInvariantSet extractInvariants(
			IGraph<T> g, TransitiveClosure<T> tc, String relation) {
		HashMap<String, ArrayList<T>> partitions = new HashMap<String, ArrayList<T>>();
		for (T m : g.getNodes()) {
			if (!partitions.containsKey(m.getLabel()))
				partitions.put(m.getLabel(), new ArrayList<T>());
			partitions.get(m.getLabel()).add(m);
		}
		TemporalInvariantSet set = new TemporalInvariantSet();
		for (String label1 : partitions.keySet()) {
			for (String label2 : partitions.keySet()) {
				Set<T> hasPredecessor = new HashSet<T>();
				Set<T> hasNoPredecessor = new HashSet<T>();
				Set<T> isPredecessor = new HashSet<T>();
				Set<T> isNoPredecessor = new HashSet<T>();
				boolean neverFollowed = true;
				boolean alwaysFollowedBy = true;
				boolean alwaysPreceded = true;
				for (T node1 : partitions.get(label1)) {
					boolean followerFound = false;
					boolean predecessorFound = false;
					for (T node2 : partitions.get(label2)) {
						if (tc.isReachable(node1, node2)) {
							neverFollowed = false;
							followerFound = true;
						}
						if (tc.isReachable(node2, node1)) {
							predecessorFound = true;
							hasPredecessor.add(node1);
							isPredecessor.add(node2);
						} else
							isNoPredecessor.add(node2);
					}
					if (!followerFound)
						alwaysFollowedBy = false;
					if (!predecessorFound) {
						alwaysPreceded = false;
						hasNoPredecessor.add(node1);
					}
				}
				if (neverFollowed)
					set
							.add(new NeverFollowedInvariant(label1, label2,
									relation));
				if (alwaysFollowedBy)
					set.add(new AlwaysFollowedInvariant(label1, label2,
							relation));
				if (alwaysPreceded)
					set.add(new AlwaysPrecedesInvariant(label2, label1,
							relation));
				else if (generateStructuralInvariants) {
					try {
						// TODO
						//Daikonizer.generateStructuralInvaraints(hasPredecessor, hasNoPredecessor,
								//isPredecessor, isNoPredecessor, partitions, label1, label2);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return set;
	}

	
	private static <T extends INode<T>> void writeDot(String filename,
			IGraph<T> g, TransitiveClosure<T> tc) {
		try {
			File f = new File(filename);
			PrintWriter p = new PrintWriter(new FileOutputStream(f));
			p.println("digraph {");

			for (T m : g.getNodes()) {
				p.println(m.hashCode() + " [label=\"" + m.getLabel() + "\"]; ");
			}

			/*
			 * TODO: fix this this does not work for some reason.
			 */
			for (T m : g.getNodes()) {
				for (T n : g.getNodes()) {
					if (tc.isReachable(m, n)) {
						p.println(m.hashCode() + " -> " + n.hashCode() + ";");
					}
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

	public Graph<MessageEvent> getInvariantGraph(String shortName) {
		HashMap<String, MessageEvent> messageMap = new HashMap<String, MessageEvent>();
		for (TemporalInvariant i : invariants) {
			for (String label : i.getPredicates()) {
				if (!messageMap.containsKey(label))
					messageMap.put(label,
							new MessageEvent(new Action(label), 0));
			}
		}

		for (TemporalInvariant i : invariants) {
			if (i instanceof BinaryInvariant
					&& (shortName == null || i.getShortName().equals(shortName))) {
				BinaryInvariant bi = (BinaryInvariant) i;
				messageMap.get(bi.getFirst()).addTransition(
						messageMap.get(bi.getSecond()), bi.getShortName());
			}
		}

		return new Graph<MessageEvent>(messageMap.values());
	}

	public int size() {
		return invariants.size();
	}

	public static TemporalInvariantSet computeInvariantsSplt(
			Graph<MessageEvent> g, String label) throws Exception {
		Graph<MessageEvent> g2 = splitAndDuplicate(g, label);
		GraphVizExporter.quickExport("output/traceCondenser/test.dot", g2);
		return computeInvariants(g2);
	}

	private static Graph<MessageEvent> splitAndDuplicate(Graph<MessageEvent> g,
			String label) {
		GraphBuilder b = new GraphBuilder();
		for (MessageEvent m : g.getInitialNodes()) {
			b.split();
			MessageEvent cur = m;
			while (cur != null) {
				if (cur.getAction().getLabel().equals(label)) {
					b.split();
				}
				b.append(cur.getAction());
				if (cur.getTransitions().size() == 1)
					cur = cur.getTransitions().iterator().next().getTarget();
				else
					cur = null;
			}
		}
		return b.getRawGraph();
	}

	
}
