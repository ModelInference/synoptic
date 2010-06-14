package model.export;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import model.Action;
import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.interfaces.IGraph;
import model.interfaces.INode;
import model.interfaces.ISuccessorProvider;
import model.interfaces.ITransition;
import model.nets.Edge;
import model.nets.Event;
import model.nets.Net;
import model.nets.Place;

/*
 * This file is bases on the code from Clemens Hammacher.
 * Source: https://ccs.hammacher.name
 * Licence: Eclipse Public License v1.0. 
 */

public class GraphVizExporter {
	static final String[] dotCommands = { "/usr/bin/dot",
			"C:\\Programme\\Graphviz2.26\\bin\\dot.exe",
			"C:\\Program Files (x86)\\Graphviz2.26.3\\bin\\dot.exe" };

	private static String getDotCommand() {
		for (String dotCommand : dotCommands) {
			File f = new File(dotCommand);
			if (f.exists())
				return dotCommand;
		}
		return "";
	}

	public GraphVizExporter() {
		super();
	}

	public <T extends INode<T>> void export(File dotFile, IGraph<T> newHead)
			throws Exception {
		final PrintWriter writer;
		try {
			writer = new PrintWriter(dotFile);
		} catch (final IOException e) {
			throw new Exception("Error opening .dot-File: " + e.getMessage(), e);
		}

		export(writer, newHead, false);
	}

	public void export(File dotFile, Net net) throws Exception {
		if (true)
			return;
		final PrintWriter writer;
		try {
			writer = new PrintWriter(dotFile);
		} catch (final IOException e) {
			throw new Exception("Error opening .dot-File: " + e.getMessage(), e);
		}

		export(writer, net);
	}

	/**
	 * Export .png file from given dotFile. The file will be created in the
	 * current working directory.
	 * 
	 * @param dotFile
	 *            - filename of .dot file.
	 */
	public void exportPng(File dotFile) {
		String dotCommand = getDotCommand();
		String execCommand = dotCommand + " -O -Tpng "
				+ dotFile.getAbsolutePath();
		try {
			Runtime.getRuntime().exec(execCommand);
		} catch (IOException e) {
			System.out.println("Could not run dotCommand '" + execCommand
					+ "': " + e.getMessage());
		}
	}

	private <T extends INode<T>> void export(final Writer writer,
			IGraph<T> graph, boolean fast) throws IOException {
		// begin graph
		writer.write("digraph {\n");

		exportGraph(writer, graph, fast);

		writer.write("} // digraph\n");

		// close the dot file
		writer.close();
	}

	private void export(final Writer writer, Net net) throws IOException {
		// begin graph
		writer.write("digraph {\n");

		exportNet(writer, net);

		writer.write("} // digraph\n");

		// close the dot file
		writer.close();
	}

	private <T extends INode<T>> void exportGraph(final Writer writer,
			IGraph<T> graph, boolean fast) throws IOException {
		// write the transitions (nodes are generated implicitly by graphviz)
		final LinkedList<T> queue = new LinkedList<T>();

		final Set<T> statesSeen = new HashSet<T>();
		final HashSet<ITransition<T>> transSeen = new HashSet<ITransition<T>>();

		for (T s : graph.getNodes()) {
			queue.add(s);
			statesSeen.add(s);
		}
		while (!queue.isEmpty()) {
			final T e = queue.poll();
			final int sourceStateNo = e.hashCode();
			String attributes = "label=\"" + quote(e.toStringConcise()) + "\"";
			/*
			 * if (graph.getInitialNodes(new Action("i")).contains(e))
			 * attributes = attributes + ",shape=box,color=blue"; else
			 */
			boolean terminal=false;
			if (e instanceof Partition) {
				Partition p = (Partition)e;
				for (MessageEvent m: p.getMessages()) {
					if (m.getTransitions().size() == 0)
						terminal = true;
				}
			}
			if (graph.getRelations().contains(new Action("t"))
					&& graph.getInitialNodes(new Action("t")).contains(e))
				attributes = attributes + ",shape=box";
			else if (graph.getRelations().contains(new Action("i"))
					&& graph.getInitialNodes(new Action("i")).contains(e))
				attributes = attributes + ",shape=box,color=blue";
			else if (terminal)
				attributes = attributes + ",shape=diamond";

			String comment = "";
			// if (e == newHead.getInitialState()) { // start node
			// attributes += ",shape=box";
			// }
			writer.write("  " + sourceStateNo + " [" + attributes + "];"
					+ comment + "\n");

			Iterable<? extends ITransition<T>> foo = null;
			if (fast)
				foo = e.getTransitionsIterator();
			else
				foo = e.getTransitions();
			for (ITransition<T> trans : foo) {
				if (!transSeen.add(trans) || !graph.getNodes().contains(trans.getTarget())) {
					writer.write("/* skipping " + trans + " */" + "\n");
					continue;
				}
				final T targetExpr = trans.getTarget();
				final int targetStateNo = targetExpr.hashCode();
				writer.write(sourceStateNo
						+ "->"
						+ targetStateNo
						+ " [label=\""
						+ quote(trans.toStringConcise())
						+ "\", weight=\""+trans.toStringConcise()+"\""
						+ (trans.toStringConcise().equals("i") ? ",color=blue"
								: "") + "];" + "\n");
				if (statesSeen.add(targetExpr))
					queue.add(targetExpr);
			}
		}
	}

	private void exportNet(final Writer writer, Net net) throws IOException {
		// write the transitions (nodes are generated implicitly by graphviz)
		Set<Place> initialPlaces = net.getInitalPlaces();

		for (Event e : net.getEvents()) {
			final int eventNo = e.hashCode();
			String attributes = "label=\"" + quote(e.toString() /*
																 * + " (" +
																 * e.hashCode()
																 * + ")"
																 */)
					+ "\",shape=box";
			writer.write(eventNo + " [" + attributes + "];" + "\n");
		}
		for (Place p : net.getPlaces()) {
			final int placeNo = p.hashCode();
			String attributes = "label=\""
					+ (initialPlaces.contains(p) ? "0" : "") + "\"";
			writer.write(placeNo + " [" + attributes + "];" + "\n");
		}

		for (Event e : net.getEvents())
			for (Edge<Event, Place> edge : e.getEdgeIterator()) {
				writer.write(edge.getSource().hashCode() + "->"
						+ edge.getTarget().hashCode() + " [label=\""
						+ edge.getWeight() + "\"];" + "\n");
			}
		for (Place p : net.getPlaces())
			for (Edge<Place, Event> edge : p.getEdgeIterator(net)) {
				writer.write(edge.getSource().hashCode() + "->"
						+ edge.getTarget().hashCode() + " [label=\""
						+ edge.getWeight() + "\"];" + "\n");
			}
	}

	public void debugExport(final Writer writer, PartitionGraph graph)
			throws IOException {
		// begin graph
		writer.write("digraph {\n");
		exportGraph(writer, graph, false);
		exportGraph(writer, graph.getSystemStateGraph(), false);
		for (SystemState<Partition> s : graph.getSystemStateGraph().getNodes()) {
			for (ISuccessorProvider<Partition> sp : s.getSuccessorProviders()) {
				writer.write(s.hashCode() + "->" + sp.hashCode() + " [label=\""
						+ quote("succProv") + "\",color=blue];\n");
			}
		}
		for (Partition p : graph.getNodes()) {
			for (SystemState<Partition> source : p.getSources())
				writer.write(p.hashCode() + "->" + source.hashCode()
						+ " [label=\"" + quote("source") + "\",color=red];\n");

		}
		writer.write("} // digraph\n");

		// close the dot file
		writer.close();
	}

	private static String quote(String string) {
		final StringBuilder sb = new StringBuilder(string.length() + 2);
		for (int i = 0; i < string.length(); ++i) {
			final char c = string.charAt(i);
			switch (c) {
			case '\\':
				sb.append("\\\\");
				break;
			case '"':
				sb.append("\\\"");
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	public String getIdentifier() {
		return "dot File export";
	}

	public <T extends INode<T>> void exportAsDotAndPng(String fileName,
			IGraph<T> g) throws Exception {
		File f = new File(fileName);
		export(f, g);
		exportPng(f);
	}

	public void exportAsDotAndPng(String fileName, Net g) throws Exception {
		File f = new File(fileName);
		export(f, g);
		exportPng(f);
	}

	public static <T extends INode<T>> void quickExport(String fileName,
			IGraph<T> g) {
		GraphVizExporter e = new GraphVizExporter();
		try {
			e.exportAsDotAndPng(fileName, g);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public <T extends INode<T>> String export(IGraph<T> g) {
		StringWriter s = new StringWriter();

		try {
			export(s, g, false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return s.toString();
	}

	public void debugExportAsDotAndPng(String fileName, PartitionGraph pg)
			throws Exception {
		if (true)
			return;
		File f = new File(fileName);
		final PrintWriter writer;
		try {
			writer = new PrintWriter(f);
		} catch (final IOException e) {
			throw new Exception("Error opening .dot-File: " + e.getMessage(), e);
		}

		debugExport(writer, pg);
		exportPng(f);
	}

	public <T extends INode<T>> void exportAsDotAndPngFast(String fileName, IGraph<T> pg) throws Exception {
		File f = new File(fileName);
		final PrintWriter writer;
		try {
			writer = new PrintWriter(f);
		} catch (final IOException e) {
			throw new Exception("Error opening .dot-File: " + e.getMessage(), e);
		}

		export(writer, pg, true);
		writer.close();
		exportPng(f);
	}

}
