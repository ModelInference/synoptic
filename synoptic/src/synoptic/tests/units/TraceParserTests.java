package synoptic.tests.units;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import synoptic.main.Main;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.main.TraceParser.Occurrence;
import synoptic.model.MessageEvent;
import synoptic.model.input.VectorTime;
import synoptic.util.InternalSynopticException;

/**
 * Tests for the synoptic.main.TraceParser class.
 *
 * @author ivan
 */
public class TraceParserTests {
	/**
	 * The parser instance we use for testing.
	 */
	TraceParser parser = null;

	@Before
	public void setUp() {
		this.parser = new TraceParser();
		Main.recoverFromParseErrors = false;
		Main.ignoreNonMatchingLines = false;
		Main.debugParse = false;
	}

	////////////////////////////////////////////////////////////////////////////
	// addRegex() tests
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Parse a log using a non-default TIME -- expect a ParseException.
	 *
	 * @throws ParseException
	 */
	@Test(expected=ParseException.class)
	public void addRegexCustomTimeRegExpExceptionTest() throws ParseException {
		// This should throw a ParseException because custom TIME fields are not allowed
		parser.addRegex("^(?<TIME>.+)\\s(?<TYPE>)$");
	}

	/**
	 * Parse a log using a non-default VTIME -- expect a ParseException.
	 *
	 * @throws ParseException
	 */
	@Test(expected=ParseException.class)
	public void addRegexCustomVTimeRegExpExceptionTest() throws ParseException {
		// This should throw a ParseException because custom VTIME fields are not allowed
		parser.addRegex("^(?<VTIME>\\d|\\d|\\d)\\s(?<TYPE>)$");
	}

	////////////////////////////////////////////////////////////////////////////
	// parseTraceString() tests
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Checks that the type and the time of each occurrence in a list is correct.
	 *
	 * @param occs List of occurrences to check
	 * @param vtimeStrs Array of corresponding occurrence vector times
	 * @param types Array of corresponding occurrence types
	 */
	public void checkOccsTypesTimes(List<Occurrence> occs, String[] vtimeStrs, String[] types) {
		assertSame(occs.size(), vtimeStrs.length);
		assertSame(vtimeStrs.length, types.length);
		for (int i = 0; i < occs.size(); i++) {
			Occurrence occ = occs.get(i);
			MessageEvent msg = occ.message;
			VectorTime eventTime = occ.getTime();
			// Check that the type and the time of the occurrence are correct
			assertTrue(msg.getLabel().equals(types[i]));
			assertTrue(new VectorTime(vtimeStrs[i]).equals(eventTime));
		}
	}

	/**
	 * Parse a log with implicit time that increments on each log line.
	 * (Purposefully doesn't handle the ParseException and InternalSynopticException
	 * as these exceptions imply that the parse has a bug).
	 *
	 * @throws ParseException
	 * @throws InternalSynopticException
	 */
	@Test
	public void parseImplicitTimeTest() throws ParseException, InternalSynopticException {
		String traceStr = "a\nb\nc\n";
		parser.addRegex("^(?<TYPE>)$");
		checkOccsTypesTimes(
				parser.parseTraceString(traceStr, "test", -1),
				new String[] {"1", "2", "3"}, // NOTE: implicit time starts with 1
				new String[] {"a", "b", "c"});

	}

	/**
	 * Parse a log with explicit integer time values.
	 */
	@Test
	public void parseExplicitIntegerTimeTest() throws ParseException, InternalSynopticException {
		String traceStr = "2 a\n3 b\n4 c\n";
		parser.addRegex("^(?<TIME>)(?<TYPE>)$");
		checkOccsTypesTimes(
				parser.parseTraceString(traceStr, "test", -1),
				new String[] {"2", "3", "4"},
				new String[] {"a", "b", "c"});
	}

	/**
	 * Parse a log with explicit vector time values.
	 */
	@Test
	public void parseExplicitVTimeTest() throws ParseException, InternalSynopticException {
		String traceStr = "1,1,1 a\n2,2,2 b\n3,3,4 c\n";
		parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
		checkOccsTypesTimes(
				parser.parseTraceString(traceStr, "test", -1),
				new String[] {"1,1,1", "2,2,2", "3,3,4"},
				new String[] {"a", "b", "c"});
	}

	/**
	 * Parse a log with two records with the same integer time -- expect a ParseException.
	 */
	@Test(expected=ParseException.class)
	public void parseSameTimeExceptionTest() throws ParseException, InternalSynopticException {
		String traceStr = "1 a\n2 b\n2 c\n";
		try {
			parser.addRegex("^(?<TIME>)(?<TYPE>)$");
		} catch (Exception e) {
			fail("addRegex should not have raised an exception");
		}
		// This should throw a ParseException because two events have the same time
		parser.parseTraceString(traceStr, "test", -1);
	}

	/**
	 * Parse a log with two records with the same vector time -- expect a ParseException.
	 */
	@Test(expected=ParseException.class)
	public void parseSameVTimeExceptionTest() throws ParseException, InternalSynopticException {
		String traceStr = "1,1,2 a\n1,1,2 b\n2,2,2 c\n";
		try {
			parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
		} catch (Exception e) {
			fail("addRegex should not have raised an exception");
		}
		// This should throw a ParseException because two events have the same vector time
		parser.parseTraceString(traceStr, "test", -1);
	}

	/**
	 * Parse a log using wrong time named group (should be TIME) -- expect a ParseException.
	 */
	@Test(expected=ParseException.class)
	public void parseNonVTimeExceptionTest() throws ParseException, InternalSynopticException {
		String traceStr = "1 a\n2 b\n3 c\n";
		try {
			parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
		} catch (Exception e) {
			fail("addRegex should not have raised an exception");
		}
		// This should throw a ParseException because VTIME expects a vector time
		parser.parseTraceString(traceStr, "test", -1);
	}

	/**
	 * Parse a log using wrong time named group (should be VTIME) -- expect a ParseException.
	 */
	@Test(expected=ParseException.class)
	public void parseNonTimeExceptionTest() throws ParseException, InternalSynopticException {
		String traceStr = "1,1 a\n2,2 b\n3,3 c\n";
		try {
			parser.addRegex("^(?<TIME>)(?<TYPE>)$");
		} catch (Exception e) {
			fail("addRegex should not have raised an exception");
		}
		// This should throw a ParseException because TIME cannot process a VTIME field
		parser.parseTraceString(traceStr, "test", -1);
	}

	/**
	 * Parse a log with records that have different length vector times -- expect a ParseException.
	 */
	@Test(expected=ParseException.class)
	public void parseDiffLengthVTimesExceptionTest() throws ParseException, InternalSynopticException {
		String traceStr = "1,1,2 a\n1,1,2,3 b\n2,2,2 c\n";
		try {
			parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
		} catch (Exception e) {
			fail("addRegex should not have raised an exception");
		}
		// This should throw a ParseException because of different length vector times in the log
		parser.parseTraceString(traceStr, "test", -1);
	}

	/**
	 * Parse a log using a non-default TYPE.
	 */
	@Test
	public void parseCustomTypeRegExpTest() throws ParseException, InternalSynopticException {
		String traceStr = "1 a a\n2 b b\n3 c c\n";
		parser.addRegex("^(?<TIME>)(?<TYPE>.+)$");
		checkOccsTypesTimes(
				parser.parseTraceString(traceStr, "test", -1),
				new String[] {"1", "2", "3"},
				new String[] {"a a", "b b", "c c"});
	}

	/**
	 * Parses a prefix of lines, instead of all lines.
	 */
	@Test
	public void parsePrefixOfLines() throws ParseException, InternalSynopticException {
		String traceStr = "1 a\n2 b\n3 c\n";
		parser.addRegex("^(?<TIME>)(?<TYPE>)$");
		checkOccsTypesTimes(
				parser.parseTraceString(traceStr, "test", 2),
				new String[] {"1", "2"},
				new String[] {"a", "b"});
	}

	/**
	 * Check that we can parse a log by splitting its lines into partitions.
	 */
	@Test
	public void parseWithSplitPartitionsTest() {
		fail("TODO");
	}

	/**
	 * Check that we can parse a log by mapping log lines to partitions.
	 */
	@Test
	public void parseWithMappedPartitionsTest() {
		fail("TODO");
	}
}
