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

/**
 * DOCUMENT ME!
 */
public class SuperSetReduction {
	public static void main(String[] args) {
		if (args.length > 1) {
			System.out.println("usage:");
			System.out
			        .println("\tjava gov.nasa.ltl.graph.SuperSetReduction [<filename>]");

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

		g = reduce(g);

		g.save();
	}

	public static Graph reduce(Graph g) {
		final int nsets = g.getIntAttribute("nsets");
		String type = g.getStringAttribute("type");
		String ac = g.getStringAttribute("ac");

		if (!type.equals("gba")) {
			throw new RuntimeException("invalid graph type: " + type);
		}

		if (ac.equals("nodes")) {
			final int nnodes = g.getNodeCount();

			final boolean[][] asets = new boolean[nsets][nnodes];

			g.forAllNodes(new EmptyVisitor() {
				@Override
				public void visitNode(Node n) {
					for (int i = 0; i < nsets; i++) {
						String acc = "acc" + i;

						if (n.getBooleanAttribute(acc)) {
							asets[i][n.getId()] = true;
							n.setBooleanAttribute(acc, false);
						}
					}
				}
			});

			boolean[] remove = new boolean[nsets];

			for (int i = 0; i < nsets; i++) {
				for (int j = 0; (j < nsets) && !remove[i]; j++) {
					if ((i != j) && !remove[j]) {
						if (included(asets[j], asets[i])) {
							remove[i] = true;
						}
					}
				}
			}

			int n_nsets = 0;

			for (int i = 0; i < nsets; i++) {
				if (!remove[i]) {
					n_nsets++;
				}
			}

			boolean[][] n_asets = new boolean[n_nsets][nnodes];

			n_nsets = 0;

			for (int i = 0; i < nsets; i++) {
				if (!remove[i]) {
					n_asets[n_nsets++] = asets[i];
				}
			}

			g.setIntAttribute("nsets", n_nsets);

			for (int i = 0; i < nnodes; i++) {
				Node n = g.getNode(i);

				for (int j = 0; j < n_nsets; j++) {
					if (n_asets[j][i]) {
						n.setBooleanAttribute("acc" + j, true);
					}
				}
			}

			return g;
		} else if (ac.equals("edges")) {
			final int nedges = g.getEdgeCount();

			final boolean[][] asets = new boolean[nsets][nedges];
			final Edge[] edges = new Edge[nedges];

			g.forAllEdges(new EmptyVisitor(new Integer(0)) {
				@Override
				public void visitEdge(Edge e) {
					int id = ((Integer) arg).intValue();
					arg = new Integer(id + 1);

					edges[id] = e;

					for (int i = 0; i < nsets; i++) {
						String acc = "acc" + i;

						if (e.getBooleanAttribute(acc)) {
							asets[i][id] = true;
							e.setBooleanAttribute(acc, false);
						}
					}
				}
			});

			boolean[] remove = new boolean[nsets];

			for (int i = 0; i < nsets; i++) {
				for (int j = 0; (j < nsets) && !remove[i]; j++) {
					if ((i != j) && !remove[j]) {
						if (included(asets[j], asets[i])) {
							remove[i] = true;
						}
					}
				}
			}

			int n_nsets = 0;

			for (int i = 0; i < nsets; i++) {
				if (!remove[i]) {
					n_nsets++;
				}
			}

			boolean[][] n_asets = new boolean[n_nsets][nedges];

			n_nsets = 0;

			for (int i = 0; i < nsets; i++) {
				if (!remove[i]) {
					n_asets[n_nsets++] = asets[i];
				}
			}

			g.setIntAttribute("nsets", n_nsets);

			for (int i = 0; i < nedges; i++) {
				Edge e = edges[i];

				for (int j = 0; j < n_nsets; j++) {
					if (n_asets[j][i]) {
						e.setBooleanAttribute("acc" + j, true);
					}
				}
			}

			return g;
		} else {
			throw new RuntimeException("invalid accepting type: " + ac);
		}
	}

	private static boolean included(boolean[] a, boolean[] b) {
		if (a.length != b.length)
			return false;

		for (int i = 0; i < a.length; i++) {
			if (a[i] && !b[i]) {
				return false;
			}
		}

		return true;
	}
}