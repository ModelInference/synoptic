package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import synoptic.algorithms.graph.IOperation;
import synoptic.algorithms.graph.PartitionSplit;
import synoptic.invariants.CanImmediatelyFollowedInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverImmediatelyFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.TOInvariantMiner;
import synoptic.main.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.EventType;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.StringEventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.tests.SynopticTest;

public class PartitionGraphTests extends SynopticTest {

	@Test
	public void splitThenMergePartitionsTest() throws Exception {
		String[] events = new String[] { "1 a", "--", "1 a" };
		TraceParser parser = new TraceParser();
		parser.addRegex("^(?<TIME>)(?<TYPE>)$");
		parser.addPartitionsSeparator("^--$");

		String traceStr = concatinateWithNewlines(events);
		ArrayList<EventNode> parsedEvents = parser.parseTraceString(traceStr,
				testName.getMethodName(), -1);
		ChainsTraceGraph inputGraph = parser
				.generateDirectTORelation(parsedEvents);

		TOInvariantMiner miner = new ChainWalkingTOInvMiner();
		TemporalInvariantSet invariants = miner.computeInvariants(inputGraph);
		PartitionGraph pGraph = new PartitionGraph(inputGraph, true, invariants);

		// The set of nodes should be: INITIAL, a, TERMINAL
		assertTrue(pGraph.getNodes().size() == 3);

		// Find the 'a' partition and assign it to splitP
		Partition splitP = getNodeByName(pGraph, new StringEventType("a"));
		assertTrue(splitP != null);

		PartitionSplit split = new PartitionSplit(splitP);

		// Add the first two event to the split.
		split.addEventToSplit(parsedEvents.get(0));
		// split.addEventToSplit(parsedEvents.get(1));

		// Perform the split.
		IOperation rewind = pGraph.apply(split);

		exportTestGraph(pGraph, 1);

		// Check the graph after the split.

		// The set of nodes is now: INITIAL, a, a', TERMINAL
		assertTrue(pGraph.getNodes().size() == 4);
		assertTrue(pGraph.getDummyInitialNodes().size() == 1);
		Partition pInitial = pGraph.getDummyInitialNodes().iterator().next();
		assertTrue(pInitial.getTransitions().size() == 2);
		Partition pA1 = pInitial.getTransitions().get(0).getTarget();
		assertTrue(pA1.getEType().equals(new StringEventType("a")));
		assertTrue(pA1.getTransitions().size() == 1);
		assertTrue(pA1.getTransitions().get(0).getTarget().getEType()
				.isTerminalEventType());
		Partition pA2 = pInitial.getTransitions().get(1).getTarget();
		assertTrue(pA2.getEType().equals(new StringEventType("a")));
		assertTrue(pA2.getTransitions().size() == 1);
		assertTrue(pA2.getTransitions().get(0).getTarget().getEType()
				.isTerminalEventType());

		// Undo the split.
		pGraph.apply(rewind);
		// The set of nodes should be back to: INITIAL, a, TERMINAL
		assertTrue(pGraph.getNodes().size() == 3);
	}

	// TODO: Test the multi-split operation.

	// TODO: Test merge operation as a primary operation (not as a rewind).

	private <T extends INode<T>> T getNodeByName(IGraph<T> g, EventType nodeName) {
		for (T node : g.getNodes()) {
			if (node.getEType().equals(nodeName)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Creates a partition graph and and checks to see if all synthetic traces
	 * are exported properly (based on what they "should" be). TODO: Test for
	 * cycles (when cycle functionality has been properly implemented).
	 */
	@Test
	public void exportSyntheticTracesTest() throws Exception {

		// This should create two synthetic traces: I a b c T, I q a b T.
		String[] events = new String[] { "1 0 a", "2 0 b", "3 1 q", "4 1 a",
				"5 1 b", "6 1 c" };
		TraceParser parser = new TraceParser();
		parser.addRegex("^(?<TIME>)(?<nodename>)(?<TYPE>)$");
		parser.setPartitionsMap("\\k<nodename>");

		TOInvariantMiner miner = new ChainWalkingTOInvMiner();
		PartitionGraph pGraph = genInitialPartitionGraph(events, parser, miner);

		// Prepare the output with what it should be (as mentioned above).
		Set<List<Partition>> pTraces = pGraph.getSyntheticTraces();

		// Should only have 2 synthetic traces.
		assertTrue(pTraces.size() == 2);
		for (List<Partition> p : pTraces) {
			// Synthetic traces will only have either a or q after INITIAL.
			boolean aTrace = p.get(1).getEType()
					.equals(new StringEventType("a"));
			boolean qTrace = p.get(1).getEType()
					.equals(new StringEventType("q"));
			assertTrue(aTrace || qTrace);
			assertTrue(p.get(0).isInitial());
			if (aTrace) {
				assertTrue(p.get(2).getEType().equals(new StringEventType("b")));
				assertTrue(p.get(3).getEType().equals(new StringEventType("c")));
			} else {
				assertTrue(p.get(2).getEType().equals(new StringEventType("a")));
				assertTrue(p.get(3).getEType().equals(new StringEventType("b")));
			}
			assertTrue(p.get(4).isTerminal());
		}
	}

	/**
	 * Test implicit invariant mining on midbranching example in
	 * traces/abstract/
	 * 
	 * @throws Exception
	 */
	@Test
	public void implicitInvariantsMidBranching() throws Exception {

		String[] events = new String[] { "1 0 c", "2 0 b", "3 0 a", "4 0 d",
				"1 1 f", "2 1 b", "3 1 a", "4 1 e", "1 2 f", "2 2 b", "3 2 a",
				"4 2 d" };

		TraceParser parser = new TraceParser();
		parser.addRegex("^(?<DTIME>)(?<nodename>)(?<TYPE>)$");
		parser.setPartitionsMap("\\k<nodename>");

		TOInvariantMiner miner = new ChainWalkingTOInvMiner();
		PartitionGraph pGraph = genInitialPartitionGraph(events, parser, miner);

		TemporalInvariantSet implicitInvariants = pGraph
				.getImmediatelyFollowsInvariants();

		int numEventTypes = pGraph.getNodes().size();
		assertEquals("Number of mined invariants", numEventTypes
				* numEventTypes, implicitInvariants.numInvariants());

		// As observed from this example's initial model.
		int expectedNumNIFbys = numEventTypes * numEventTypes - 9;

		// Build set of CIFby invariants
		Set<NeverImmediatelyFollowedInvariant> NIFbys = new HashSet<NeverImmediatelyFollowedInvariant>();
		for (ITemporalInvariant invariant : implicitInvariants.getSet()) {
			if (invariant instanceof NeverImmediatelyFollowedInvariant) {
				NIFbys.add((NeverImmediatelyFollowedInvariant) invariant);
			}
		}
		
		assertEquals("Number of NIFby invariants", expectedNumNIFbys,
				NIFbys.size());

		// TODO: test that the CIFbys are correct
	}
}