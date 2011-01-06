package synoptic.tests;

import synoptic.model.export.GraphVizExporter;
import synoptic.model.input.NetBuilder;
import synoptic.model.input.PetersonReader;
import synoptic.model.nets.Event;
import synoptic.model.nets.Net;

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
