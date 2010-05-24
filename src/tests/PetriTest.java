package tests;

import model.export.GraphVizExporter;
import model.input.NetBuilder;
import model.input.PetersonReader;
import model.nets.Event;
import model.nets.Net;

public class PetriTest {
	public static void main(String[] args) throws Exception {
		NetBuilder b = new NetBuilder();
		PetersonReader<Event> r = new PetersonReader<Event>(b);
		GraphVizExporter e = new GraphVizExporter();
		r
				.readGraphSet(
						"traces/PetersonLeaderElection/generated_traces/peterson_trace-n5-s?.txt",
						5);
		Net g = b.getNet();
		e.exportAsDotAndPng("output/peterson/initial.dot", g);
		
		
	}
}
