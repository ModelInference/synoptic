package invariants;

import gov.nasa.ltl.trans.ParseErrorException;
import invariants.ltlcheck.Counterexample;
import invariants.ltlcheck.IModelCheckingMonitor;
import invariants.ltlchecker.GraphLTLChecker;

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
import java.util.List;
import java.util.Set;

import daikon.ValueTuple;
import daikon.inv.Invariant;
import daikon.inv.InvariantStatus;
import daikon.util.Intern;
import daikonizer.Daikonizer;

import algorithms.graph.TransitiveClosure;

import model.Action;
import model.Graph;
import model.IEvent;
import model.MessageEvent;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;
import model.interfaces.IGraph;
import model.interfaces.INode;
import model.interfaces.ITransition;

public class TemporalInvariantSet implements Iterable<TemporalInvariant> {
	public static boolean generateStructuralInvariants = false;
	HashSet<TemporalInvariant> invariants = new HashSet<TemporalInvariant>();
	static boolean DEBUG = false;

	public <T extends INode<T>> boolean check(IGraph<T> g) {
		TemporalInvariantSet set = computeInvariants(g);
		boolean result = set.invariants.containsAll(invariants);
		if (!result && DEBUG) {
			System.out.println(getUnsatisfiedInvariants(g));
		}
		return result;
	}

	public <T extends INode<T>> TemporalInvariantSet getUnsatisfiedInvariants(
			IGraph<T> g) {
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
	}

	public <T extends INode<T>> List<RelationPath<T>> getViolations(IGraph<T> g) {
		List<RelationPath<T>> paths = new ArrayList<RelationPath<T>>();
		GraphLTLChecker<T> c = new GraphLTLChecker<T>();
		for (TemporalInvariant i : invariants) {
			// List<Transition<Message>> path = i.check(g);
			try {
				Counterexample ce = c.check(g, i, new IModelCheckingMonitor() {
					public void subTask(String str) {
					}
				});
				if (ce == null)
					continue;
				RelationPath<T> r = new RelationPath<T>();
				r.invariant = i;
				List<T> trace = c.convertCounterexample(ce);
				if (trace != null) {
					// System.out.println(i.toString() + trace);
					r.path = i.shorten(trace);
					if (r.path == null) {
						throw new RuntimeException(
								"shortening returned null for " + i
										+ " and trace " + trace);
					}
					// System.out.println(r.path);
					paths.add(r);
				}
			} catch (ParseErrorException e) {
				e.printStackTrace();
			}
		}

		if (paths.size() == 0)
			return null;

		Collections.sort(paths, new Comparator<RelationPath<T>>() {
			@Override
			public int compare(RelationPath<T> o1, RelationPath<T> o2) {
				return new Integer(o1.path.size()).compareTo(o2.path.size());
			}
		});

		return paths;
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

	static public <T extends INode<T>> TemporalInvariantSet computeInvariants(
			IGraph<T> g) {
		if (DEBUG) {
			GraphVizExporter v = new GraphVizExporter();
			try {
				v.exportAsDotAndPng("output/pre.dot", g);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		AllRelationsTransitiveClosure<T> tc = new AllRelationsTransitiveClosure<T>(
				g);
		if (DEBUG) {
			GraphVizExporter v = new GraphVizExporter();
			for (Action relation : tc.getRelations())
				writeDot("output/post-" + relation + ".dot", g, tc
						.get(relation));
			v.exportPng(new File("output/post.dot"));
		}
		
		TemporalInvariantSet set = extractInvariantsForAllRelations(g, tc);
		List<RelationPath<T>> vio = set.getViolations(g);
		if (vio == null)
			return set;
		for (RelationPath<T> i : vio) {
			set.invariants.remove(i);
		}
		// System.out.println("done.");
		return set;
	}

	private static <T extends INode<T>> TemporalInvariantSet extractInvariantsForAllRelations(
			IGraph<T> g, AllRelationsTransitiveClosure<T> tcs) {
		TemporalInvariantSet invariants = new TemporalInvariantSet();
		for (Action relation : g.getRelations()) {
			invariants.add(extractInvariants(g, tcs.get(relation), relation));
		}
		return invariants;
	}

	private static <T extends INode<T>> TemporalInvariantSet extractInvariants(
			IGraph<T> g, TransitiveClosure<T> tc, Action relation) {
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
				for (T m1 : partitions.get(label1)) {
					boolean followerFound = false;
					boolean predecessorFound = false;
					for (T n1 : partitions.get(label2)) {
						if (tc.isReachable(m1, n1)) {
							neverFollowed = false;
							followerFound = true;
						}
						if (tc.isReachable(n1, m1)) {
							predecessorFound = true;
							hasPredecessor.add(m1);
							isPredecessor.add(n1);
						} else
							isNoPredecessor.add(n1);
					}
					if (!followerFound)
						alwaysFollowedBy = false;
					if (!predecessorFound) {
						alwaysPreceded = false;
						hasNoPredecessor.add(m1);
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
					ArrayList<String> datafieldList = new ArrayList<String>();
					ArrayList<String> datatypes = new ArrayList<String>();
					getFields(hasPredecessor, datafieldList, datatypes);
					List<Invariant> inv = generateInvariants(hasPredecessor,
							datafieldList, datatypes);
					List<Invariant> invNo = generateInvariants(
							hasNoPredecessor, datafieldList, datatypes);
					List<Invariant> all = generateInvariants(partitions
							.get(label1), datafieldList, datatypes);
					if (inv != null && invNo != null) {
						ArrayList<Invariant> list = getRelevantInvariants(inv,
								invNo, all);
						List<Invariant> inv2 = generateInvariants(
								isPredecessor, datafieldList, datatypes);
						List<Invariant> invNo2 = generateInvariants(
								isNoPredecessor, datafieldList, datatypes);
						List<Invariant> all2 = generateInvariants(partitions
								.get(label2), datafieldList, datatypes);
						ArrayList<Invariant> list2 = getRelevantInvariants(
								inv2, invNo2, all2);
						if (list.size() > 0 || list2.size() > 0) {
							System.out.println("    " + label2 + list2
									+ "\nAP  " + label1 + list);
//							for (Invariant i : list) {
//								double r = getInvariantRelevance(i,
//										(Collection) partitions.get(label1),
//										datafieldList);
//								System.out.println("Conf " + r + " for " + i);
//							}
						}
					}
				}
			}
		}
		return set;
	}

	public static double getInvariantRelevance(Invariant inv,
			Collection<IEvent> events, List<String> argNames) {
		Invariant clone = inv.clone();
		double num = 0;
		for (IEvent e : events) {
			int size = argNames.size();
			Object[] vals = new Object[size];
			int[] mods = new int[size];
			int index = 0;
			for (String s : argNames) {
				if (e.getStringArgument(s) == null)
					vals[index] = Intern.internedLong(-1);
				else
					vals[index] = Intern.internedLong(Long.parseLong(e
							.getStringArgument(s)));
				mods[index] = 0;
				index++;
			}
			ValueTuple vt = new ValueTuple(vals, mods);
			InvariantStatus status = clone.add_sample(vt, 1);
			if (status != InvariantStatus.NO_CHANGE) {
				num++;
			}
		}
		return num / events.size();
	}

	public static ArrayList<Invariant> getRelevantInvariants(
			List<Invariant> inv, List<Invariant> invNo, List<Invariant> all) {
		ArrayList<Invariant> list = new ArrayList<Invariant>();
		// System.out.println(inv);
		// System.out.println(invNo);
		for (Invariant i : inv) {
			boolean found = false;
			for (Invariant j : invNo) {
				if (i.isSameInvariant(j)) {
					found = true;
					break;
				}
			}
			for (Invariant j : all) {
				if (i.isSameInvariant(j)) {
					found = true;
					break;
				}
			}
			if (!found)
				list.add(i);
		}
		// System.out.println("->" + list);
		return list;
	}

	public static <T> List<Invariant> generateInvariants(
			Collection<T> hasPredecessor, List<String> datafieldList,
			List<String> datatypes) {
		Daikonizer d = new Daikonizer("foo", datafieldList, datatypes);
		boolean dont = false;
		for (T n : hasPredecessor) {
			if (!(n instanceof IEvent)) {
				dont = true;
				break;
			}
			IEvent e = (IEvent) n;
			List<String> vals = new ArrayList<String>();
			for (String argName : datafieldList) {
				String s = e.getStringArgument(argName);
				if (s == null || s.length() == 0)
					s = "-1";
				vals.add(s);
			}
			d.addValues((List) vals, (List) vals);
		}
		if (!dont) {
			List<Invariant> enter = new ArrayList<Invariant>();
			List<Invariant> exit = new ArrayList<Invariant>();
			List<Invariant> flow = new ArrayList<Invariant>();
			d.genDaikonInvariants(enter, exit, flow, false);
			return enter;
		}
		return null;
	}

	public static <T extends INode<T>> List<Invariant> generateFlowInvariants(
			Collection<T> messages, Action relation, String targetType) {
		ArrayList<String> datafieldList = new ArrayList<String>();
		ArrayList<String> datatypes = new ArrayList<String>();
		getFields(messages, datafieldList, datatypes);
		return generateFlowInvariants(messages, relation, targetType,
				datafieldList, datatypes);
	}

	public static <T extends INode<T>> List<Invariant> generateFlowInvariants(
			Collection<T> messages, Action relation, String targetType,
			List<String> datafieldList, List<String> datatypes) {
		Daikonizer d = new Daikonizer("foo", datafieldList, datatypes);
		boolean dont = false;
		for (T n : messages) {
			if (!(n instanceof IEvent)) {
				dont = true;
				break;
			}
			IEvent e = (IEvent) n;
			List<String> beginVals = new ArrayList<String>();
			for (String argName : datafieldList) {
				String s = e.getStringArgument(argName);
				if (s == null || s.length() == 0)
					s = "-1";
				beginVals.add(s);
			}
			IEvent e2 = null;
			for (ITransition<T> t : n.getTransitionsIterator(relation)) {
				if (t.getTarget().getLabel().equals(targetType)) {
					e2 = (IEvent) t.getTarget();
					break;
				}
			}
			List<String> endVals = new ArrayList<String>();
			if (e2 != null) {
				for (String argName : datafieldList) {
					String s = e2.getStringArgument(argName);
					if (s == null || s.length() == 0)
						s = "-1";
					endVals.add(s);
				}
			}
			d.addValues((List) beginVals, (List) endVals);
		}
		if (!dont) {
			List<Invariant> enter = new ArrayList<Invariant>();
			List<Invariant> exit = new ArrayList<Invariant>();
			List<Invariant> flow = new ArrayList<Invariant>();
			d.genDaikonInvariants(enter, exit, flow, false);
			return flow;
		}
		return null;
	}

	private static <T> void getFields(Collection<T> hasPredecessor,
			ArrayList<String> datafieldList, ArrayList<String> datatypes) {
		Set<String> datafields = new HashSet<String>();
		for (T n : hasPredecessor) {
			if (!(n instanceof IEvent))
				break;
			IEvent e = (IEvent) n;
			datafields.addAll(e.getStringArguments());
		}
		datafieldList.addAll(datafields);
		for (String s : datafieldList)
			datatypes.add("int");
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
			for (String label : i.getLabels()) {
				if (!messageMap.containsKey(label))
					messageMap.put(label, new MessageEvent(new Action(label),
							null, null, 0));
			}
		}

		for (TemporalInvariant i : invariants) {
			if (i instanceof BinaryInvariant
					&& (shortName == null || i.getShortName().equals(shortName))) {
				BinaryInvariant bi = (BinaryInvariant) i;
				messageMap.get(bi.getFirst()).addTransition(
						messageMap.get(bi.getSecond()),
						new Action(bi.getShortName()));
			}
		}

		return new Graph<MessageEvent>(messageMap.values());
	}

	public int size() {
		return invariants.size();
	}

	public static TemporalInvariantSet computeInvariantsSplt(
			Graph<MessageEvent> g, String label) {
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

	public static List<Invariant> generateInvariants(Set<MessageEvent> messages) {
		ArrayList<String> datafieldList = new ArrayList<String>();
		ArrayList<String> datatypes = new ArrayList<String>();
		getFields(messages, datafieldList, datatypes);
		return generateInvariants(messages, datafieldList, datatypes);
	}
}
