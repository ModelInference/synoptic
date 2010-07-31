package tests;

import invariants.TemporalInvariantSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import model.Action;
import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.Transition;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;
import model.input.NfsTraceParser;
import model.input.ReverseTracertParser;
import model.scalability.ScalableGraph;import algorithms.ktail.KTail;
import algorithms.ktail.StateUtil;

public class StateTest extends TestCase {

	public void testKEquals1() {
		// A simple case: just one state should be equal to itself
		SystemState<Transition<SystemState>> s = new SystemState<Transition<SystemState>>("foo");
		s.addSuccessorProvider(GraphBuilder.makeSuccessorProvider());

		assertTrue(StateUtil.kEquals(s, s, 0, true));
		assertTrue(StateUtil.kEquals(s, s, 1, true));
		assertTrue(StateUtil.kEquals(s, s, 10, true));
	}

	public void testKEquals2() {
		// A more complicated case, a two-node system.
		SystemState<MessageEvent> s2 = new SystemState<MessageEvent>("s2");
		SystemState<MessageEvent> s1 = new SystemState<MessageEvent>("s1");
		s1.addSuccessorProvider(GraphBuilder.makeSuccessorProvider(Collections.singleton(new MessageEvent(new Action("foo"), s1, s2, 1))));
		s2.addSuccessorProvider(GraphBuilder.makeSuccessorProvider());
		
		SystemState<MessageEvent> s2prime = new SystemState<MessageEvent>("s2prime");
		SystemState<MessageEvent> s1prime = new SystemState<MessageEvent>("s1prime");
		s1prime.addSuccessorProvider(GraphBuilder.makeSuccessorProvider(Collections.singleton(new MessageEvent(new Action("foo"), s1prime, s2prime, 1))));
		s2prime.addSuccessorProvider(GraphBuilder.makeSuccessorProvider());

		assertTrue(StateUtil.kEquals(s1, s1prime, 0, true));
		assertTrue(StateUtil.kEquals(s1, s1prime, 1, true));
		assertTrue(StateUtil.kEquals(s1, s1prime, 10, true));
	}

	public void testKEquals3() {
		// A three node system that is equal at k:1 but not k:2
		SystemState<MessageEvent> s3 = new SystemState<MessageEvent>("s3");
		SystemState<MessageEvent> s2 = new SystemState<MessageEvent>("s2");
		SystemState<MessageEvent> s1 = new SystemState<MessageEvent>("s1");
		s1.addSuccessorProvider(GraphBuilder.makeSuccessorProvider(Collections.singleton(new MessageEvent(new Action("foo"), s1, s2, 1))));
		s2.addSuccessorProvider(GraphBuilder.makeSuccessorProvider(Collections.singleton(new MessageEvent(new Action("bar"), s2, s3, 1))));
		s3.addSuccessorProvider(GraphBuilder.makeSuccessorProvider());
		
		SystemState<MessageEvent> s3prime = new SystemState<MessageEvent>("s3");
		SystemState<MessageEvent> s2prime = new SystemState<MessageEvent>("s2");
		SystemState<MessageEvent> s1prime = new SystemState<MessageEvent>("s1");
		s1prime.addSuccessorProvider(GraphBuilder.makeSuccessorProvider(Collections.singleton(new MessageEvent(new Action("foo"), s1prime, s2prime, 1))));
		s2prime.addSuccessorProvider(GraphBuilder.makeSuccessorProvider(Collections.singleton(new MessageEvent(new Action("baz"), s2prime, s3prime, 1))));
		s3prime.addSuccessorProvider(GraphBuilder.makeSuccessorProvider());
		
		assertTrue(StateUtil.kEquals(s1, s1prime, 0, true));
		assertTrue(StateUtil.kEquals(s1, s1prime, 1, true));
		assertFalse(StateUtil.kEquals(s1, s1prime, 10, true));
	}

	public void testkMerge() throws Exception {
		GraphBuilder gb = new GraphBuilder();
		gb.append(new Action("foo"));
		gb.append(new Action("bar"));
		gb.split();
		gb.append(new Action("foo"));
		gb.append(new Action("baz"));
		
		PartitionGraph g = gb.getGraph(false);
		
		System.out.println("BEFORE k-Merge:");
		GraphVizExporter export = new GraphVizExporter();
		System.out.println(export.export(g.getSystemStateGraph()));
		export.exportAsDotAndPng("output/tests/StateTest.testkMerge.before.dot", g.getSystemStateGraph());
		List<SystemState<Partition>> initial = new ArrayList<SystemState<Partition>>();
		initial.addAll(g.getSystemStateGraph().getInitialNodes());
		assertTrue(StateUtil.kEquals(initial.get(0), initial.get(1), 1, true));
		StateUtil.kMerge(g, initial.get(0), initial.get(1), 1);

		System.out.println("AFTER k-Merge:");
		System.out.println(export.export(g.getSystemStateGraph()));
		export.exportAsDotAndPng("output/tests/StateTest.testkMerge.after.dot", g.getSystemStateGraph());
	}

	public void testInvariantReductionTwoc() {
		PartitionGraph tpc = traceTwoPhaseCommit();

		TemporalInvariantSet inv = TemporalInvariantSet.computeInvariants(tpc);
		System.out.println("INV-before: " + inv);
		TemporalInvariantSet inv2 = TemporalInvariantSet.computeInvariants(tpc);
		System.out.println("INV-after: " + inv2);

		KTail.kReduce(tpc, 1, true, true);
		TemporalInvariantSet inv3 = TemporalInvariantSet.computeInvariants(tpc);
		System.out.println("INV-reduced: " + inv3);

		System.out.println("SAT: " + inv.check(tpc));

		GraphVizExporter.quickExport("output/tests/StateTest.testInvariantReductionTwoc.after-S.dot", tpc.getSystemStateGraph());
		GraphVizExporter.quickExport("output/tests/StateTest.testInvariantReductionTwoc.after.dot", tpc);
	}

	public void testReduceTraces() throws Exception {
		GraphVizExporter export = new GraphVizExporter();

		PartitionGraph pingPong = tracePingPong();
		// reducing ping/pong with subsumption gives the same graph as bisim
		KTail.kReduce(pingPong, 1, true, true);
		System.out.println("*** PING-PONG with subsumption, invariants");
		System.out.println(export.export(pingPong.getSystemStateGraph()));
		// without subsumption
		pingPong = tracePingPong();
		KTail.kReduce(pingPong, 1, false, false);
		System.out.println("*** PING-PONG without subsumption, invariants");
		System.out.println(export.export(pingPong.getSystemStateGraph()));

		PartitionGraph threePhase = traceThreePhase();
		KTail.kReduce(threePhase, 1, true, false);
		System.out
				.println("*** THREE-PHASE with subsumption, without invariants");
		System.out.println(export.export(threePhase.getSystemStateGraph()));

		PartitionGraph paxos = tracePaxosDuelingProposers();
		KTail.kReduce(threePhase, 1, true, false);
		System.out
				.println("*** PAXOS DUELING PROPOSERS with subsumption, without invariants");
		KTail.kReduce(paxos, 1, true, false);
		System.out.println(export.export(paxos.getSystemStateGraph()));

		PartitionGraph tpc = traceTwoPhaseCommit();
		TemporalInvariantSet inv = TemporalInvariantSet.computeInvariants(tpc);
		System.out.println("INV-before: " + inv);
		TemporalInvariantSet inv2 = TemporalInvariantSet.computeInvariants(tpc);
		System.out.println("INV-after: " + inv2);

		// reducing 2-phase commit correctness depends on k
		KTail.kReduce(tpc, 2, true, true);
		System.out
				.println("*** 2-PHASE COMMIT with subsumption, invariants, and k=2");
		System.out.println(export.export(tpc.getSystemStateGraph()));

		tpc = traceTwoPhaseCommit();
		KTail.kReduce(tpc, 1, true, true);

		TemporalInvariantSet inv3 = TemporalInvariantSet.computeInvariants(tpc);
		System.out.println("INV: " + inv3);

		System.out.println("SAT: " + inv.check(tpc));

		System.out
				.println("*** 2-PHASE COMMIT with subsumption, invariants, and k=1");
		System.out.println(export.export(tpc.getSystemStateGraph()));
		File f = new File("test.dot");
		export.export(f, tpc);

		// Graph tpco = traceTwoPhaseCommitOther();
		// tpco.kReduce(2, false);
		// System.out.println(tpco.ToDot());
	}

	public void testNfs() throws Exception {
		// Graph g = (new NfsTraceParser()).parseTraceFile(
		// "data/nfs/anon-lair62-010901-0000.txt", 1000,
		// NfsTraceParser.SPLIT_BY_FILE);
		// // g.mergeInputEquivalentTraces();
		// g.kReduce(1, true, false);
		// // g.rebuildTransitionSystem();
		// //Graph mg = Bisimulation.getMinimizedSystem(g, 0.00001);
		// System.out.println("***NFS");
		//
		GraphVizExporter export = new GraphVizExporter();
		// System.out.println(export.exportMessagesAsTransitions(g));
		// //export.export(new File("nfs.dot"), mg);

		PartitionGraph sg = new PartitionGraph((new NfsTraceParser()).parseTraceFileLarge(
				"data/nfs/anon-lair62-010901-0000.txt", 5000, 200,
				NfsTraceParser.SPLIT_BY_FILE), true);
		
		System.out.println("***NFS scalable GK-Tail");

	}

	public void testTwitter() throws Exception {

	}

	// public void testGraphCopy() throws Exception {
	// GraphVizExporter export = new GraphVizExporter();
	//    
	// Graph tpc = traceTwoPhaseCommit();
	// System.out.println(export.exportMessagesAsTransitions(tpc));
	// Graph tpcCopy = new Graph(tpc);
	// System.out.println(export.exportMessagesAsTransitions(tpcCopy));
	// }

	// public void testCommonStructure() throws Exception {
	// GraphVizExporter export = new GraphVizExporter();
	//    
	// Graph pingPong = tracePingPong();
	// // reducing ping/pong with subsumption gives the same graph as bisim
	// pingPong.kReduce(1, true, true);
	// System.out.println(export.exportMessagesAsTransitions(pingPong));
	// // Now export the same thing as a message/transition system:
	// File testFile = new File("output/test.dot");
	// export.export(testFile, pingPong);
	// }

	// /////////////////////////////////////////////////////
	// Below here are the traces:
	// - ping/pong
	// - three-phase algorithm
	// - PAXOS dueling proposers
	// - Two-phase commit
	// /////////////////////////////////////////////////////

	public static PartitionGraph tracePingPong() {
		// State lastState = new State("done");
		// Random r = new Random();
		// for (int i = 0; i < 20; i++) {
		// State ping = new State("a" + i);
		//    
		// //boolean isLost = r.nextDouble() < 0.05;
		// boolean isLost = false;
		// boolean isStatus = r.nextBoolean();
		// State status = (isStatus) ? new State("status_" + i) : null;
		//    
		// State pong = new State("b" + i);
		//  
		// if (isLost) {
		// ping.AddTransition("ping", lastState, 1);
		// } else {
		// ping.AddTransition("ping", pong, 1);
		// if (isStatus) {
		// pong.AddTransition("pong", status, 1);
		// status.AddTransition("status", lastState, 1);
		// } else {
		// pong.AddTransition("pong", lastState, 1);
		// }
		// }
		//    
		// lastState = ping;
		// }
		//  
		// State startState = new State("start");
		// startState.AddTransition("start", lastState, 1);
		//  
		// Graph g = new Graph("pingpong");
		// g.startState = startState;

		String[] trace = new String[] { "ping", "pong", "ping", "pong",
		// "status",
				"ping", "pong", "ping", "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "status",
		// "ping",
		// "pong",
		// "status",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "status",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "status",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		// "ping",
		// "pong",
		};

		return GraphBuilder.buildGraph(trace);
	}

	public PartitionGraph traceThreePhase() {
		String[] trace = new String[] { "ready?", "no", "ready?", "no",
				"ready?", "no", "ready?", "no", "ready?", "no", "ready?", "no",
				"ready?", "yes", "do_work", "ok", "do_work", "ok", "do_work",
				"ok", "do_work", "ok", "do_work", "ok", "do_work", "ok",
				"do_work", "ok", "do_work", "ok", "do_work", "ok", "do_work",
				"ok", "do_work", "ok", "do_work", "ok", "send_results",
				"result", "result", "result", "result", "result", };

		return GraphBuilder.buildGraph(trace);
	}

	public PartitionGraph tracePaxosDuelingProposers() {
		String[] trace = new String[] { "Request", "Prepare", "Promise",
				"Prepare", "Promise", "Prepare", "Nack", "Prepare", "Promise",
				"Accept!", "Nack", "Prepare", "Promise", "Accept!", "Nack",
				"Prepare", "Promise", "Accept!", "Nack", "Prepare", "Promise",
				"Accept!", "Nack", };

		return GraphBuilder.buildGraph(trace);
	}

	public static PartitionGraph traceTwoPhaseCommit() {
		String[] trace1 = new String[] { "p", "p", "c", "c", "txc", "txc", };
		String[] trace2 = new String[] { "p", "p", "c", "a", "txa", "txa", };
		String[] trace3 = new String[] { "p", "p", "a", "c", "txa", "txa", };
		String[] trace4 = new String[] { "p", "p", "a", "a", "txa", "txa", };
		return GraphBuilder.buildGraph(new String[][] { trace1, trace2, trace3,
				trace4 });
	}

	public PartitionGraph traceTwoPhaseCommitOther() {
		String[] trace = new String[] { "tx_prepare", "tx_prepare", "abort",
				"commit", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"commit", "commit", "tx_commit", "tx_commit", "tx_prepare",
				"tx_prepare", "abort", "abort", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "commit", "commit", "tx_commit",
				"tx_commit", "tx_prepare", "tx_prepare", "commit", "abort",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "commit",
				"commit", "tx_commit", "tx_commit", "tx_prepare", "tx_prepare",
				"commit", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "commit", "commit", "tx_commit", "tx_commit",
				"tx_prepare", "tx_prepare", "commit", "abort", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "abort", "commit",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "commit",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"abort", "commit", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "abort", "abort", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "commit", "abort", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "commit", "abort",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "abort",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"abort", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "commit", "commit", "tx_commit", "tx_commit",
				"tx_prepare", "tx_prepare", "abort", "abort", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "commit", "abort",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "commit",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"abort", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "abort", "commit", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "commit", "abort", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "commit", "abort",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "abort",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"commit", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "commit", "abort", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "commit", "commit", "tx_commit",
				"tx_commit", "tx_prepare", "tx_prepare", "abort", "commit",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "abort",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"abort", "commit", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "abort", "abort", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "commit", "abort", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "abort", "abort",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "abort",
				"commit", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"commit", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "abort", "commit", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "abort", "commit", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "abort", "commit",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "abort",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"abort", "commit", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "abort", "abort", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "abort", "commit", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "abort", "commit",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "commit",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"abort", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "abort", "commit", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "commit", "commit", "tx_commit",
				"tx_commit", "tx_prepare", "tx_prepare", "commit", "abort",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "commit",
				"commit", "tx_commit", "tx_commit", "tx_prepare", "tx_prepare",
				"commit", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "commit", "abort", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "abort", "abort", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "commit", "commit",
				"tx_commit", "tx_commit", "tx_prepare", "tx_prepare", "abort",
				"commit", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"abort", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "commit", "commit", "tx_commit", "tx_commit",
				"tx_prepare", "tx_prepare", "commit", "abort", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "abort", "commit",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "abort",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"abort", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "abort", "commit", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "commit", "commit", "tx_commit",
				"tx_commit", "tx_prepare", "tx_prepare", "abort", "abort",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "abort",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"commit", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "commit", "abort", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "commit", "abort", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "commit", "commit",
				"tx_commit", "tx_commit", "tx_prepare", "tx_prepare", "abort",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"abort", "commit", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "abort", "abort", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "abort", "commit", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "abort", "abort",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "commit",
				"commit", "tx_commit", "tx_commit", "tx_prepare", "tx_prepare",
				"commit", "commit", "tx_commit", "tx_commit", "tx_prepare",
				"tx_prepare", "commit", "abort", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "commit", "commit", "tx_commit",
				"tx_commit", "tx_prepare", "tx_prepare", "commit", "abort",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "abort",
				"commit", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"abort", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "abort", "commit", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "commit", "abort", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "abort", "abort",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "abort",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"abort", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "abort", "commit", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "abort", "commit", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "commit", "commit",
				"tx_commit", "tx_commit", "tx_prepare", "tx_prepare", "abort",
				"abort", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"commit", "abort", "tx_abort", "tx_abort", "tx_prepare",
				"tx_prepare", "abort", "commit", "tx_abort", "tx_abort",
				"tx_prepare", "tx_prepare", "abort", "commit", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "abort", "abort",
				"tx_abort", "tx_abort", "tx_prepare", "tx_prepare", "abort",
				"commit", "tx_abort", "tx_abort", "tx_prepare", "tx_prepare",
				"commit", "commit", "tx_commit", "tx_commit", "tx_prepare",
				"tx_prepare", "commit", "commit", "tx_commit", "tx_commit",
				"tx_prepare", "tx_prepare", "commit", "abort", "tx_abort",
				"tx_abort", "tx_prepare", "tx_prepare", "abort", "abort",
				"tx_abort", "tx_abort",

		};
		return GraphBuilder.buildGraph(trace);

	}
}
