package tests;

import algorithms.bisim.Bisimulation;
import model.MessageEvent;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.ReverseTracertParser;

public class ReverseTraceroute {
	public static void main(String[] args) throws Exception {
		GraphVizExporter export = new GraphVizExporter();
		//model.Graph<MessageEvent> raw = readMultiple();
		model.Graph<MessageEvent> raw = readMultiple();
		PartitionGraph g = new PartitionGraph(raw, true);
		export.exportAsDotAndPngFast("output/reverseTraceroute/input.dot", g);
		Bisimulation.refinePartitionsSmart(g);
		System.out.println("merging.");
		Bisimulation.mergePartitions(g);

		export.exportAsDotAndPng("output/reverseTraceroute/output.dot", g);
	}
	
	static model.Graph<MessageEvent> readSingle() {
		return (new ReverseTracertParser()).parseTraceFile(
				"traces/ReverseTraceroute/rt_parsed/velia.net-AS29066_revtr.err", 10000, 0);
	}
	
	static model.Graph<MessageEvent> readMultiple() {
		String[] traces_34 = new String[] {
				"APAN-AS7660-2_revtr.err",
				"American-Internet-Services_revtr.err",
				"CERN-AS513_revtr.err",
				"Cogent-AS174_revtr.err",
				"Companhia-Portuguesa-Radio-Marconi-AS8657_revtr.err",
				// "Doris-UA-AS8343_revtr.err",
				"EUnet-Finland-AS6667_revtr.err",
				"Eastlink_revtr.err",
				"Exobit-Networks-AS30653_revtr.err",
				"Friedberg-University-AS680_revtr.err",
				"ISP-Services-BV_revtr.err",
				"Leaseweb_revtr.err",
				"MANAP-AS29129_revtr.err",
				"MuntInternet_revtr.err",
				"NORDUnet-AS2603_revtr.err",
				"National-Educational-and-Research-Information-Network-Bulgaria-AS6802_revtr.err",
				"RUNNet-AS3267_revtr.err", "RedIRIS-AS766_revtr.err",
				"Rede-Nacional-de-Ensino-e-Pesquisa-RNP-AS1916_revtr.err",
				"Silesian-University-of-Technology-AS8508-15744_revtr.err",
				"SolNet-AS9044_revtr.err",
				"T-Systems-Pragonet-AS21142_revtr.err",
				"TATA-Communications-Ltd_revtr.err",
				"Telefonica-AS12956_revtr.err", "Teleglobe-AS6453_revtr.err",
				"Telia-Denmark-AS3308_revtr.err",
				"TeliaNet-Russia-AS1299_revtr.err",
				"TeliaSonera-AS1299_revtr.err", "Telus-AS852_revtr.err",
				"UKR-AS16124_revtr.err", "UNNET-AS31323_revtr.err",
				"UkrSat-AS12369_revtr.err", "Ultraspeed_revtr.err",
				/*"velia.net-AS29066_revtr.err"*/};

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
		
		String[] traces_long = {"velia.net-AS29066_revtr.err", "hotze.com-AS8596_revtr.err"};

		GraphVizExporter export = new GraphVizExporter();
		model.Graph<MessageEvent> g = new model.Graph<MessageEvent>();
		String prefix = "data/rt/parsed/";
		for (String trace : traces_34) {
			g.merge((new ReverseTracertParser()).parseTraceFile(prefix
					+ trace, 1000, 0));
		}
		System.out.println(g.getNodes().size());
		return g;
	}
}
