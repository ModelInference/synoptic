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
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

/**
 * DOCUMENT ME!
 */
public class SCCReduction {
	public static void main(String[] args) {
		if (args.length > 1) {
			System.out.println("usage:");
			System.out.println("\tjava " + SCCReduction.class.getName() + " [<filename>]");

			return;
		}

		Graph g = null;

		try {
			if (args.length == 0) {
				g = Graph.load();
			} else {
				g = Graph.load(args[0]);
			}
		} catch (final IOException e) {
			System.out.println("Can't load the graph.");

			return;
		}

		g = reduce(g);

		g.save();
	}

	public static Graph reduce(Graph g) {
		boolean changed;

		for (final Iterator<List<Node>> i = SCC.scc(g).iterator(); i.hasNext();) {
			clearExternalEdges(i.next(), g);
		}

		do {
			changed = false;

			final List<List<Node>> sccs = SCC.scc(g);

			for (final Iterator<List<Node>> i = sccs.iterator(); i.hasNext();) {
				final List<Node> scc = i.next();
				boolean accepting = isAccepting(scc, g);

				if (!accepting && isTerminal(scc)) {
					changed = true;

					for (final Iterator<Node> j = scc.iterator(); j.hasNext();) {
						j.next().remove();
					}
				} else if (isTransient(scc) || !accepting) {
					changed |= anyAcceptingState(scc, g);
					clearAccepting(scc, g);
				}
			}
		} while (changed);

		return g;
	}

	private static boolean isAccepting(List<Node> scc, Graph g) {
		final String type = g.getStringAttribute("type");
		final String ac = g.getStringAttribute("ac");

		if (type.equals("ba")) {
			if (ac.equals("nodes")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					if ((i.next()).getBooleanAttribute("accepting")) {
						return true;
					}
				}

				return false;
			} else if (ac.equals("edges")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					for (final Iterator<Edge> j = n.getOutgoingEdges().iterator(); j
					        .hasNext();) {
						final Edge e = j.next();

						if (e.getBooleanAttribute("accepting")) {
							return true;
						}
					}
				}

				return false;
			} else {
				throw new RuntimeException("invalid accepting type: " + ac);
			}
		} else if (type.equals("gba")) {
			final int nsets = g.getIntAttribute("nsets");
			final BitSet found = new BitSet(nsets);
			int nsccs = 0;

			if (ac.equals("nodes")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					for (int j = 0; j < nsets; j++) {
						if (n.getBooleanAttribute("acc" + j)) {
							if (!found.get(j)) {
								found.set(j);
								nsccs++;
							}
						}
					}
				}
			} else if (ac.equals("edges")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					for (final Iterator<Edge> j = n.getOutgoingEdges().iterator(); j
					        .hasNext();) {
						final Edge e = j.next();

						for (int k = 0; k < nsets; k++) {
							if (e.getBooleanAttribute("acc" + k)) {
								if (!found.get(k)) {
									found.set(k);
									nsccs++;
								}
							}
						}
					}
				}
			} else {
				throw new RuntimeException("invalid accepting type: " + ac);
			}

			return nsccs == nsets;
		} else {
			throw new RuntimeException("invalid graph type: " + type);
		}
	}

	private static boolean isTerminal(List<Node> scc) {
		for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
			final Node n = i.next();

			for (final Iterator<Edge> j = n.getOutgoingEdges().iterator(); j
			        .hasNext();) {
				if (!scc.contains((j.next()).getNext())) {
					return false;
				}
			}
		}

		return true;
	}

	private static boolean isTransient(List<Node> scc) {
		if (scc.size() != 1) {
			return false;
		}

		final Node n = scc.get(0);

		for (final Iterator<Edge> i = n.getOutgoingEdges().iterator(); i.hasNext();) {
			if ((i.next()).getNext() == n) {
				return false;
			}
		}

		return true;
	}

	private static boolean anyAcceptingState(List<Node> scc, Graph g) {
		final String type = g.getStringAttribute("type");
		final String ac = g.getStringAttribute("ac");

		if (type.equals("ba")) {
			if (ac.equals("nodes")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					if (n.getBooleanAttribute("accepting")) {
						return true;
					}
				}
			} else if (ac.equals("edges")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					for (final Iterator<Edge> j = n.getOutgoingEdges().iterator(); j
					        .hasNext();) {
						final Edge e = j.next();

						if (e.getBooleanAttribute("accepting")) {
							return true;
						}
					}
				}
			} else {
				throw new RuntimeException("invalid accepting type: " + ac);
			}
		} else if (type.equals("gba")) {
			final int nsets = g.getIntAttribute("nsets");

			if (ac.equals("nodes")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					for (int j = 0; j < nsets; j++) {
						if (n.getBooleanAttribute("acc" + j)) {
							return true;
						}
					}
				}
			} else if (ac.equals("edges")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					for (final Iterator<Edge> j = n.getOutgoingEdges().iterator(); j
					        .hasNext();) {
						final Edge e = j.next();

						for (int k = 0; k < nsets; k++) {
							if (e.getBooleanAttribute("acc" + j)) {
								return true;
							}
						}
					}
				}
			} else {
				throw new RuntimeException("invalid accepting type: " + ac);
			}
		} else {
			throw new RuntimeException("invalid graph type: " + type);
		}

		return false;
	}

	private static void clearAccepting(List<Node> scc, Graph g) {
		final String type = g.getStringAttribute("type");
		final String ac = g.getStringAttribute("ac");

		if (type.equals("ba")) {
			if (ac.equals("nodes")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					n.setBooleanAttribute("accepting", false);
				}
			} else if (ac.equals("edges")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					for (final Iterator<Edge> j = n.getOutgoingEdges().iterator(); j
					        .hasNext();) {
						final Edge e = j.next();

						e.setBooleanAttribute("accepting", false);
					}
				}
			} else {
				throw new RuntimeException("invalid accepting type: " + ac);
			}
		} else if (type.equals("gba")) {
			final int nsets = g.getIntAttribute("nsets");

			if (ac.equals("nodes")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					for (int j = 0; j < nsets; j++) {
						n.setBooleanAttribute("acc" + j, false);
					}
				}
			} else if (ac.equals("edges")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					for (final Iterator<Edge> j = n.getOutgoingEdges().iterator(); j
					        .hasNext();) {
						final Edge e = j.next();

						for (int k = 0; k < nsets; k++) {
							e.setBooleanAttribute("acc" + k, false);
						}
					}
				}
			} else {
				throw new RuntimeException("invalid accepting type: " + ac);
			}
		} else {
			throw new RuntimeException("invalid graph type: " + type);
		}
	}

	private static void clearExternalEdges(List<Node> scc, Graph g) {
		final String type = g.getStringAttribute("type");
		final String ac = g.getStringAttribute("ac");

		if (type.equals("ba")) {
			if (ac.equals("edges")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					for (final Iterator<Edge> j = n.getOutgoingEdges().iterator(); j
					        .hasNext();) {
						final Edge e = j.next();

						if (!scc.contains(e.getNext())) {
							e.setBooleanAttribute("accepting", false);
						}
					}
				}
			} else if (!ac.equals("nodes")) {
				throw new RuntimeException("invalid accepting type: " + ac);
			}
		} else if (type.equals("gba")) {
			final int nsets = g.getIntAttribute("nsets");

			if (ac.equals("edges")) {
				for (final Iterator<Node> i = scc.iterator(); i.hasNext();) {
					final Node n = i.next();

					for (final Iterator<Edge> j = n.getOutgoingEdges().iterator(); j
					        .hasNext();) {
						final Edge e = j.next();

						if (!scc.contains(e.getNext())) {
							for (int k = 0; k < nsets; k++) {
								e.setBooleanAttribute("acc" + k, false);
							}
						}
					}
				}
			} else if (!ac.equals("nodes")) {
				throw new RuntimeException("invalid accepting type: " + ac);
			}
		} else {
			throw new RuntimeException("invalid graph type: " + type);
		}
	}
}