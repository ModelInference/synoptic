package main;

import com.martiansoftware.jsap.JSAPResult;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.lang.Integer;

import util.TimedTask;
import utilMDE.ArraysMDE;

import algorithms.bisim.Bisimulation;
import main.TraceParser.Occurrence;
import model.Graph;
import model.MessageEvent;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;

public class Main implements Callable<Integer> {
	/**
	 * @param args
	 * @throws JSAPException
	 */
	public static void main(String[] args) throws Exception {
		ArgumentParser aparser = new ArgumentParser(args);
		if (!aparser.Parse()) {
			aparser.PrintParseErrors();
			System.exit(1);
		}
		Main m = new Main(aparser.config);
		Integer ret = m.call();
		System.out.println("Main.call() returned " + ret.toString());
		System.exit(ret);
	}

	/***********************************************************/

	private JSAPResult config;
	private TraceParser parser;

	public Main(JSAPResult config) {
		this.config = config;
	}
	
	@Override
	public Integer call() throws Exception {
		//TODO: currently all of the logfiles are parsed by the same rules.
		// perhaps [([logfile], [parser], filter)]
		String[] logfiles = this.config.getStringArray("logfiles");
		Boolean verbose = this.config.getBoolean("verbose");
		String parserString = this.config.getString("parser");
		String filter = this.config.getString("filter");
		int lines = this.config.getInt("lines");

		if (verbose) {
			System.out.println("working directory: " + System.getProperty("user.dir"));
			System.out.println("logfiles: " + ArraysMDE.toString(logfiles));
			System.out.println("parser: " + parserString);
			System.out.println("filter: " + filter);
			System.out.println("lines cap: " + lines);
		}

		parser = new TraceParser(parserString);
		//parser.LOG = new Logger("mainlog");
		parser.builder = new GraphBuilder();
		List<Occurrence> occurrences = new ArrayList<Occurrence>();
		for (String file : logfiles) {
			List<Occurrence> parsed = parser.parseTraceFile(file, lines);
			if (parsed != null) {
				occurrences.addAll(parsed);
			}
		}
		parser.generateDirectTemporalRelation(occurrences, true);
		Graph<MessageEvent> graph = ((GraphBuilder)parser.builder).getRawGraph();

		GraphVizExporter export = new GraphVizExporter();

		TimedTask all = new TimedTask("all", 0);
		PartitionGraph g = new PartitionGraph(graph, true);
		export.export(new File("input.dot"), g);
		Bisimulation.refinePartitions(g);
		if (verbose) System.out.println("merging.");
		Bisimulation.mergePartitions(g);
		all.stop();
		if (verbose) System.out.println(all);

//      TODO: print invariants
//		TemporalInvariantSet inv = g.getInvariants();

		return new Integer(0);
	}	

}
