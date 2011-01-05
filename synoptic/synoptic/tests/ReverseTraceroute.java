package tests;

import invariants.TemporalInvariant;
import invariants.TemporalInvariantSet;
import invariants.TemporalInvariantSet.RelationPath;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import benchmarks.PerformanceMetrics;
import benchmarks.TimedTask;

import algorithms.bisim.Bisimulation;
import algorithms.graph.Operation;
import model.Graph;
import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.Relation;
import model.export.GraphVizExporter;
import model.input.ReverseTracertParser;
import model.interfaces.IGraph;
import model.interfaces.IModifiableGraph;
import model.interfaces.INode;

public class ReverseTraceroute {
	public static class ReplaceOperation implements Operation {
		private ArrayList<Partition> s;

		public ReplaceOperation(ArrayList<Partition> s) {
			this.s = s;
		}

		@Override
		public Operation commit(PartitionGraph g,
				IModifiableGraph<Partition> partitionGraph) {
			HashSet<MessageEvent> nodes = new HashSet<MessageEvent>();
			String str = "";
			for (Partition p : s) {
				partitionGraph.remove(p);
				// nodes.addAll(p.getMessages());
				str = str + p.getLabel() + "*";
			}
			nodes.addAll(s.get(0).getMessages());
			nodes.addAll(s.get(s.size() - 1).getMessages());
			System.out.println(str);
			Partition newPartition = new Partition(nodes);
			newPartition.setLabel(str);
			partitionGraph.add(newPartition);
			return null;
		}

	}

	public static void main(String[] args) throws Exception {
		GraphVizExporter export = new GraphVizExporter();
		model.Graph<MessageEvent> raw = readMultiple();
		// model.Graph<MessageEvent> raw = readSingle();
		// export.exportAsDotAndPngFast("output/reverseTraceroute/raw.dot",
		// raw);
		TimedTask all = new TimedTask("all");
		PartitionGraph g = new PartitionGraph(raw, true);
		export.exportAsDotAndPngFast("output/reverseTraceroute/input.dot", g);
		Bisimulation.refinePartitions(g);
		//System.out.println("merging.");
		Bisimulation.mergePartitions(g);
		all.stop();
		TemporalInvariantSet inv = g.getInvariants()
				.getUnsatisfiedInvariants(g);
		// for (TemporalInvariant i : inv) {
		// System.out.println(i);
		// }
		System.out.println(inv.size());
		List<RelationPath<Partition>> vio = g.getInvariants().getViolations(g);
		if (vio != null) {
			for (RelationPath<Partition> v : vio) {
				System.out.println(v.invariant);
			}
			System.out.println(vio.size());

		}
		int states = g.getNodes().size();
		HashMap<Partition, ArrayList<Partition>> seq = getMessageEventSequences(g);
		export.exportAsDotAndPng("output/reverseTraceroute/output.dot", g);
		for (ArrayList<Partition> s : seq.values()) {
			if (s.size() > 1)
				g.apply(new ReplaceOperation(s));
		}
		System.out.println(states);
		export.exportAsDotAndPng(
				"output/reverseTraceroute/output-condensed.dot", g);
		System.out.println(all);
	}

	public static model.Graph<MessageEvent> readOverkill() {
		return readOverkill(Integer.MAX_VALUE);
	}

	public static model.Graph<MessageEvent> readOverkill(int limit) {
		model.Graph<MessageEvent> g = new model.Graph<MessageEvent>();
		String prefix = "traces/ReverseTraceroute/rt_parsed_rich/";
		File dir = new File(prefix);
		int read = 0;
		ArrayList<File> files = new ArrayList<File>();
		for (File file : dir.listFiles()) {
			files.add(file);
		}
		Collections.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return new Long(o1.length()).compareTo(o2.length());
			}
		});
		for (File file : files) {
			if (read++ >= limit)
				break;
			if (file.isDirectory())
				continue;
			g.merge((new ReverseTracertParser()).parseTraceFile(prefix
					+ file.getName(), 10000, 0));
		}
		PerformanceMetrics.get().record("log size", g.getNodes().size());
		return g;
	}

	static model.Graph<MessageEvent> readSingle() {
		Graph<MessageEvent> g = (new ReverseTracertParser())
				.parseTraceFile(
						"traces/ReverseTraceroute/rt_parsed_rich/Viafacil-AS16814_revtr.err",
						100000, 0);

		//System.out.println(g.getNodes().size());
		return g;
	}

	static model.Graph<MessageEvent> readMultiple() {
		String[] traces_34 = new String[] {// "APAN-AS7660-2_revtr.err",
				// "American-Internet-Services_revtr.err",
				// "CERN-AS513_revtr.err",
				"Internet-Partners,-Inc.-AS10248-2_revtr.err",
				"Cogent-AS174_revtr.err",
				// "Companhia-Portuguesa-Radio-Marconi-AS8657_revtr.err",
				// "Viatel-AS8190_revtr.err",
				// "3GNTW-AS25137_revtr.err",
				"Broadcastchile-Networks-AS6535_revtr.err",
				"Elite-AS29611_revtr.err",
		// "Rede-Nacional-de-Ensino-e-Pesquisa-RNP-AS1916_revtr.err",
		// "hotze.com-AS8596_revtr.err",
		// "Titan-Networks-AS20640_revtr.err"
		// "velia.net-AS29066_revtr.err",
		// "Telesweet-AS41399_revtr.err"
		// "TATA-Communications-Ltd_revtr.err",
		// "Telus-AS852_revtr.err"
		// "Ultraspeed_revtr.err"//"Doris-UA-AS8343_revtr.err",//"Friedberg-University-AS680_revtr.err"

		/*
		 * "EUnet-Finland-AS6667_revtr.err", "Eastlink_revtr.err", /*
		 * "Exobit-Networks-AS30653_revtr.err",
		 * "Friedberg-University-AS680_revtr.err", "ISP-Services-BV_revtr.err",
		 * "Leaseweb_revtr.err", "MANAP-AS29129_revtr.err",
		 * "MuntInternet_revtr.err", "NORDUnet-AS2603_revtr.err",
		 * "National-Educational-and-Research-Information-Network-Bulgaria-AS6802_revtr.err"
		 * , "RUNNet-AS3267_revtr.err", "RedIRIS-AS766_revtr.err",
		 * "Rede-Nacional-de-Ensino-e-Pesquisa-RNP-AS1916_revtr.err",
		 * "Silesian-University-of-Technology-AS8508-15744_revtr.err",
		 * "SolNet-AS9044_revtr.err", "T-Systems-Pragonet-AS21142_revtr.err",
		 * "TATA-Communications-Ltd_revtr.err", "Telefonica-AS12956_revtr.err",
		 * "Teleglobe-AS6453_revtr.err", "Telia-Denmark-AS3308_revtr.err",
		 * "TeliaNet-Russia-AS1299_revtr.err", "TeliaSonera-AS1299_revtr.err",
		 * "Telus-AS852_revtr.err", "UKR-AS16124_revtr.err",
		 * "UNNET-AS31323_revtr.err", "UkrSat-AS12369_revtr.err",
		 * "Ultraspeed_revtr.err", "velia.net-AS29066_revtr.err"
		 */};

		String[] traces_62 = new String[] {
				"APAN-AS7660-2_revtr.err",
				"AboveNet_revtr.err",
				"American-Internet-Services_revtr.err",
				"Brain-Technology-SpA-Playnet_revtr.err",
				"CERN-AS513_revtr.err",
				"Centauri-Communications,-Inc-AS35975_revtr.err",

				"Cogent-AS174_revtr.err",
				"Companhia-Portuguesa-Radio-Marconi-AS8657_revtr.err",
				"Doris-UA-AS8343_revtr.err",
				"EUnet-Finland-AS6667_revtr.err",
				"Eastlink_revtr.err",
				"Elite-AS29611_revtr.err",
				"EuroNet-AS5390_revtr.err",
				"Exobit-Networks-AS30653_revtr.err",
				"France-Teaser-AS13273_revtr.err",
				"Friedberg-University-AS680_revtr.err",
				"Funet_revtr.err",
				"Global-Crossing-AS3549_revtr.err",
				"HEAnet-AS1213_revtr.err",
				"IP2-Internet-AS34486_revtr.err",
				"ISP-Services-BV_revtr.err",
				"ISPnet,-Inc._revtr.err",
				"Internet-Partners,-Inc.-AS10248-2_revtr.err",
				"Laxin-IT-Services-GmbH-Co.-KG_revtr.err",
				"Leaseweb_revtr.err",
				"MANAP-AS29129_revtr.err",
				"MORENet-AS2572_revtr.err",
				"Magnet-Networks_revtr.err",
				"MuntInternet_revtr.err",
				"NORDUnet-AS2603_revtr.err",
				"National-Educational-and-Research-Information-Network-Bulgaria-AS6802_revtr.err",
				"Netvision-Telecom-AS39737_revtr.err",
				"NewNet-Ltd-AS9191_revtr.err",
				"PauService-Internet-Services-AS29158_revtr.err",
				"Phyxia-AS35627_revtr.err", "Princeton_revtr.err",
				"RHnet-Iceland-University-Research-Network-AS15474_revtr.err",
				"RUNNet-AS3267_revtr.err", "RedIRIS-AS766_revtr.err",
				"Rede-Nacional-de-Ensino-e-Pesquisa-RNP-AS1916_revtr.err",
				"SUNET-Swedish-university-network-AS1653_revtr.err",
				"San-Diego-Super-Computer-Center-AS1227_revtr.err",
				"Silesian-University-of-Technology-AS8508-15744_revtr.err",
				"SolNet-AS9044_revtr.err", "Sunrise-AS6730_revtr.err",
				"T-Systems-Pragonet-AS21142_revtr.err",
				"TATA-Communications-Ltd_revtr.err",
				"Telefonica-AS12956_revtr.err", "Teleglobe-AS6453_revtr.err",
				"Telesweet-AS41399_revtr.err",
				"Telia-Denmark-AS3308_revtr.err",
				"TeliaNet-Russia-AS1299_revtr.err",
				"TeliaSonera-AS1299_revtr.err",
				"Telmex-Chile-AS19338_revtr.err", "Telus-AS852_revtr.err",
				"Titan-Networks-AS20640_revtr.err", "UKR-AS16124_revtr.err",
				"UNNET-AS31323_revtr.err", "UkrSat-AS12369_revtr.err",
				"Ultraspeed_revtr.err", "Viatel-AS8190_revtr.err", };

		String[] traces_long = { "velia.net-AS29066_revtr.err",
				"hotze.com-AS8596_revtr.err" };

		GraphVizExporter export = new GraphVizExporter();
		model.Graph<MessageEvent> g = new model.Graph<MessageEvent>();
		String prefix = "traces/ReverseTraceroute/rt_parsed_rich/";
		for (String trace : traces_34) {
			g.merge((new ReverseTracertParser()).parseTraceFile(prefix + trace,
					10000, 0));
		}
		//System.out.println(g.getNodes().size());

		return g;
	}

	private static HashMap<Partition, ArrayList<Partition>> getMessageEventSequences(
			PartitionGraph net) {
		HashMap<Partition, ArrayList<Partition>> entries = new HashMap<Partition, ArrayList<Partition>>();
		HashSet<Partition> seen = new HashSet<Partition>();
		for (Partition event : net.getNodes()) {
			if (!seen.add(event))
				continue;
			Set<Partition> post = getPostMessageEvents(event);
			if (post.size() != 1)
				continue;
			// if (getPreMessageEvents(net, event).size() > 1)
			// continue;
			entries.put(event, new ArrayList<Partition>(Collections
					.singleton(event)));
			Iterator<Partition> iter = post.iterator();
			while (iter.hasNext()) {
				Partition next = iter.next();
				seen.add(next);
				if (entries.get(event).contains(next))
					break;
				if (getPreMessageEvents(net, next).size() > 1)
					break;
				Set<Partition> post2 = getPostMessageEvents(next);
				if (post2.size() > 1)
					break;
				if (entries.containsKey(next)) {
					for (Partition old : entries.get(next)) {
						if (entries.get(event).contains(old))
							break;
						entries.get(event).add(old);
					}
					entries.remove(next);
					break;
				}
				entries.get(event).add(next);

				if (post2.size() == 0) {
					break;
				}
				// at this point we know post.size() == 1
				iter = post2.iterator();
			}
		}
		return entries;
	}

	private static Set<Partition> getPreMessageEvents(PartitionGraph net,
			Partition event) {
		HashSet<Partition> pre = new HashSet<Partition>();
		for (Partition p : net.getNodes()) {
			for (Relation<Partition> t : p.getTransitionsIterator()) {
				if (t.getTarget() == event)
					pre.add(p);
			}
		}
		return pre;
	}

	private static Set<Partition> getPostMessageEvents(Partition next) {
		Set<Partition> post = new HashSet<Partition>();
		for (Relation<Partition> t : next.getTransitions()) {
			post.add(t.getTarget());
		}
		return post;
	}

}
