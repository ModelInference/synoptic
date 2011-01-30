package synoptic.model.export;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import plume.Option;

import synoptic.main.Main;
import synoptic.model.Action;
import synoptic.model.MessageEvent;
import synoptic.model.Partition;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.model.nets.Edge;
import synoptic.model.nets.Event;
import synoptic.model.nets.Net;
import synoptic.model.nets.Place;
import synoptic.util.InternalSynopticException;


/*
 * This file is bases on the code from Clemens Hammacher.
 * Source: https://ccs.hammacher.name
 * Licence: Eclipse Public License v1.0. 
 */

public class GraphVizExporter {
	static Logger logger = Logger.getLogger("GraphVizExporter");
	
	static final HashMap<String, String> relationColors;
	static {
		relationColors = new HashMap<String, String>();
		relationColors.put("t", "");
		relationColors.put("i", "blue");
	}
			
	static final String[] dotCommands = {"/usr/bin/dot",
			"C:\\Programme\\Graphviz2.26\\bin\\dot.exe",
			"C:\\Program Files (x86)\\Graphviz2.26.3\\bin\\dot.exe" };
	
	private boolean initial;

	/**
	 * @return Returns the dot command executable or null on error
	 * @throws InternalSynopticException problem looking up a command line option description
	 */
	private static String getDotCommand() {
		for (String dotCommand : dotCommands) {
			File f = new File(dotCommand);
			if (f.exists())
				return dotCommand;
		}
		if (Main.dotExecutablePath == null) {
			try {
			logger.severe("Unable to locate the dot command executable, use cmd line option:\n\t" + 
					Main.getCmdLineOptDesc("dotExecutablePath"));
			} catch (InternalSynopticException e) {
				System.out.println(e);
			}
		}
		return Main.dotExecutablePath;
	}

	public GraphVizExporter() {
		super();
	}
	
	public GraphVizExporter(boolean initial){
		this();
		this.initial = initial;
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
		if (dotCommand == null) {
			// could not locate a dot executable
			return;
		}
		
		String imageExt = "png";
		
		String execCommand = dotCommand + " -O -T" + imageExt + " "
				+ dotFile.getAbsolutePath();
		
		logger.info("Exporting graph to: " + dotFile.toString() + "." + imageExt);
		
		try {
			Runtime.getRuntime().exec(execCommand);
		} catch (IOException e) {
			logger.severe("Could not run dotCommand '" + execCommand
					+ "': " + e.getMessage());
		}
	}

	private <T extends INode<T>> void export(final Writer writer,
			IGraph<T> graph, boolean fast) throws IOException {
		// begin graph
		writer.write("digraph {\n");

		if (Main.exportCanonically) {
			exportGraphCanonically(writer, graph, fast);
		} else {
			exportGraphNonCanonically(writer, graph, fast);	
		}
		
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
	

	private <T extends INode<T>> String nodeDotAttributes(T node, boolean initial, boolean terminal, String color) {
		String attributes = "label=\"" + quote(node.toStringConcise()) + "\"";
		if (initial) {
			attributes = attributes + ",shape=box";
		} else if (terminal) {
			attributes = attributes + ",shape=diamond";
		}
		if (!color.equals("")) {
			attributes = attributes + ",color=" + color;
		}
		return attributes;
	}
	
	
	private <T extends INode<T>> int exportRelationNodes(
			final Writer writer,
			IGraph<T> graph,
			String relation,
			LinkedList<ITransition<T>> allTransitions,
			HashMap<T, Integer> nodeToInt,
			int nodeCnt) throws IOException {
		
		LinkedList<T> rootNodes = new LinkedList<T>(graph.getInitialNodes(relation));
		LinkedList<T> parentNodes = new LinkedList<T>(rootNodes);
		boolean isTerminal, isInitial;
						
		//logger.finest("<exportRelationNodes>, nodeCnt is " + nodeCnt);
		LinkedList<T> childrenNodes = null;
		while (parentNodes.size() != 0) {
			//logger.finest("Main loop with parentNodes: " + parentNodes.toString());
			childrenNodes = new LinkedList<T>();
			Collections.sort(parentNodes);
			
			for (T node : parentNodes) {
			//	logger.finest("Consider parent: " + node.toStringConcise());
				
				if (nodeToInt.containsKey(node)) {
					// Skip nodes that have been processed previously.
				//	logger.finest("Skipping previously seen node: " + node.toStringConcise());
					continue;
				}
				
				isTerminal=false;
				if ((INode<?>)node instanceof Partition) {
					Partition p = (Partition)(INode<?>) node;
					for (MessageEvent m: p.getMessages()) {
						if (m.getTransitions().size() == 0)
							isTerminal = true;
					}
				} else if ((INode<?>)node instanceof MessageEvent) {
					MessageEvent e = (MessageEvent)(INode<?>) node;
					if (e.getTransitions().size() == 0) 
						isTerminal = true; 
				}
				
				isInitial = rootNodes.contains(node);
				
				String attrs = nodeDotAttributes(node, isInitial, isTerminal, relationColors.get(relation));
				writer.write("  " + nodeCnt + " [" + attrs + "];\n");
				nodeToInt.put(node, nodeCnt);
				List<? extends ITransition<T>> transitions = node.getTransitions();
				for (ITransition<T> trans: transitions) {
					T child = trans.getTarget();
					//logger.finest("Considering child: " + child.toStringConcise() + ", and its class is: " + child.getClass().toString());
					childrenNodes.add(child);
					
					//logger.finest("ChildrenNodes is now: " + childrenNodes.toString());
				}
				allTransitions.addAll(transitions);
				nodeCnt += 1;
				//logger.finest("Main loop end, new nodeCnt is " + nodeCnt);
			}
			parentNodes = childrenNodes;
		}
		writer.flush();
		return nodeCnt;
	}
		
	
	private <T extends INode<T>> void exportGraphCanonically(final Writer writer,	
			IGraph<T> graph, boolean fast) throws IOException {
		
		// logger.finest("Performing canonical export..");
		HashMap<T, Integer> nodeToInt = new HashMap<T, Integer>();
		LinkedList<ITransition<T>> allTransitions = new LinkedList<ITransition<T>>();
		int nodeCnt = 0;
		
		nodeCnt = exportRelationNodes(writer, graph, "t", allTransitions, nodeToInt, nodeCnt);
		// logger.finest("</exportRelationNodes relation(t)>, nodeCnt is " + nodeCnt);
		
		exportRelationNodes(writer, graph, "i", allTransitions, nodeToInt, nodeCnt);
		// logger.finest("</exportRelationNodes relation(i)>, nodeCnt is " + nodeCnt);
						
		// Output edges:
		for (ITransition<T> trans: allTransitions) {
			int sourceInt = nodeToInt.get(trans.getSource());
			int targetInt = nodeToInt.get(trans.getTarget());
			writer.write(sourceInt
					+ "->"
					+ targetInt+ " [");
			if (Main.outputEdgeLabels && !initial) {
				writer.write("label=\""
					+ quote(trans.toStringConcise())
					+ "\", weight=\""+trans.toStringConcise()+"\",");
			}
			if (trans.toStringConcise().equals("i")) {
				writer.write(",color=blue");
			}
			writer.write("];" + "\n");
		}
		return;
	}
	
	
	
	private <T extends INode<T>> void exportGraphNonCanonically(final Writer writer,	
			IGraph<T> graph, boolean fast) throws IOException {
		
		logger.finest("Performing standard export..");
		
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
			
			boolean isTerminal=false;
			if ((INode<?>)e instanceof Partition) {
				Partition p = (Partition)(INode<?>)e;
				for (MessageEvent m: p.getMessages()) {
					if (m.getTransitions().size() == 0)
						isTerminal = true;
				}
			}
		
			// TODO: set this to the appropriate relation
			String relation = "t";
			boolean isInitial = graph.getRelations().contains(new Action(relation)) && graph.getInitialNodes(relation).contains(e);
			String attributes = nodeDotAttributes(e, isInitial, isTerminal, relationColors.get(relation));
			
			writer.write("  " + sourceStateNo + " [" + attributes + "];\n");

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
						+ targetStateNo + " [");
				if (Main.outputEdgeLabels && !initial) {
					writer.write("label=\""
						+ quote(trans.toStringConcise())
						+ "\", weight=\""+trans.toStringConcise()+"\",");
				}
				writer.write((trans.toStringConcise().equals("i") ? ",color=blue"
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

	public <T extends INode<T>> void exportAsDotAndPngFast(String fileName, IGraph<T> pg) throws Exception {
		File f = new File(fileName);
		logger.info("Exporting dot file to: " + fileName);
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
