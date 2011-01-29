package synoptic.tests.units;

import java.util.List;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.main.TraceParser.Occurrence;
import synoptic.model.Graph;
import synoptic.model.MessageEvent;
import synoptic.model.PartitionGraph;
import synoptic.model.input.GraphBuilder;
import synoptic.util.InternalSynopticException;

/**
 * Tests for synoptic.invariants.TemporalInvariantSet class.
 *
 * @author ivan
 */
public class TemporalInvariantSetTests {
	/**
	 * We test invariants by parsing a string representing a log,
	 * and mining invariants from the resulting graph.
	 */
	TraceParser parser;

	/**
	 * Default relation used in invariant mining.
	 */
	static final String defRelation = "t";

	/**
	 * Sets up the parser state and Main static variables
	 *
	 * @throws ParseException
	 */
	@Before
	public void setUp() throws ParseException {
		Main.recoverFromParseErrors = false;
		Main.ignoreNonMatchingLines = false;
		Main.debugParse = false;
		parser = new TraceParser();
		parser.addRegex("^(?<TYPE>)$");
	}

	/**
	 * Creates a single string out of an array of strings, joined together
	 * and delimited using a newline
	 *
	 * @param strAr array of strings to join
	 * @return the joined string
	 */
	private String joinString(String[] strAr) {
		StringBuilder sb = new StringBuilder();
		for (String s : strAr) {
			sb.append(s);
			sb.append('\n');
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * Generates a TemporalInvariantSet based on a sequence of log events.
	 *
	 * @param a log of events, each one in the format: (?<TYPE>)
	 * @return an invariant set for this sequence of events
	 * @throws ParseException
	 * @throws InternalSynopticException
	 */
	private TemporalInvariantSet genInvariants(String[] events) throws ParseException, InternalSynopticException {
		String traceStr = joinString(events);
		List<Occurrence> parsedEvents = parser.parseTraceString(traceStr, "test", -1);
		parser.generateDirectTemporalRelation(parsedEvents, true);
		Graph<MessageEvent> inputGraph = ((GraphBuilder) parser.builder).getRawGraph();
		PartitionGraph result = new PartitionGraph(inputGraph, true);
		return result.getInvariants();
	}

	/**
	 * Tests the sameInvariants() method.
	 */
	@Test
	public void testSameInvariants() {
		TemporalInvariantSet s1 = new TemporalInvariantSet();
		TemporalInvariantSet s2 = new TemporalInvariantSet();

		assertTrue(s1.sameInvariants(s2));
		assertTrue(s2.sameInvariants(s1));

		s1.add(new AlwaysFollowedInvariant("a", "b", defRelation));
		assertFalse(s1.sameInvariants(s2));
		assertFalse(s2.sameInvariants(s1));

		s2.add(new AlwaysFollowedInvariant("a", "b", defRelation));
		assertTrue(s1.sameInvariants(s2));
		assertTrue(s2.sameInvariants(s1));

		s1.add(new NeverFollowedInvariant("b", "a", defRelation));
		assertFalse(s1.sameInvariants(s2));
		assertFalse(s2.sameInvariants(s1));

		// Add a similar looking invariant, but different in the B/b label.
		s2.add(new NeverFollowedInvariant("B", "a", defRelation));
		assertFalse(s1.sameInvariants(s2));
		assertFalse(s2.sameInvariants(s1));
	}

	@Test
	public void mineBasicTest() throws ParseException, InternalSynopticException {
		String[] log = new String[] {"a", "b"};
		TemporalInvariantSet minedInvs = this.genInvariants(log);
		TemporalInvariantSet trueInvs = new TemporalInvariantSet();

		trueInvs.add(new AlwaysFollowedInvariant("a", "b", defRelation));
		trueInvs.add(new AlwaysPrecedesInvariant("a", "b", defRelation));
		trueInvs.add(new NeverFollowedInvariant("b", "a", defRelation));
		trueInvs.add(new NeverFollowedInvariant("b", "b", defRelation));
		trueInvs.add(new NeverFollowedInvariant("a", "a", defRelation));

		assertTrue(trueInvs.sameInvariants(minedInvs));
	}

	@Test
	public void mineAFbyTest() throws ParseException, InternalSynopticException {
		fail("TODO");
	}

	@Test
	public void mineNFbyTest() {
		fail("TODO");
	}

	@Test
	public void mineAPbyTest() {
		fail("TODO");
	}

	@Test
	public void mineMultiplePartitionsTest() {
		fail("TODO");
	}
}
