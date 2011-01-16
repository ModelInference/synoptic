package synoptic.tests.units;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
  
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.main.TraceParser.Occurrence;


public class TraceParserTests {
	/**
	 * Parse a log with implicit time that increments on each log line.
	 * @throws ParseException 
	 */
	@Test
	public void parseImplicitTimeTest() throws ParseException {
		TraceParser parser = new TraceParser();
		String trace = "a\nb\nc\n";
		parser.addRegex("^(?<TYPE>)$");
		// Purposefully don't handle the ParseException because the parse is broken
		List<Occurrence> occs = parser.parseTraceString(trace, new String("test"), -1);
		// TODO: test occs here.
	}
	
	/**
	 * Parse a log with explicit integer time values.
	 */
	@Test
	public void parseExplicitIntegerTimeTest() {
		// TODO
	}
	
	/**
	 * Parse a log with explicit vector time values.
	 */
	@Test
	public void parseExplicitVectorTimeTest() {
		// TODO
	}
	
	/**
	 * Check that we can parse a log by splitting its lines into partitions.
	 */
	@Test
	public void parseWithSplitPartitionsTest() {
		// TODO
	}
	
	/**
	 * Check that we can parse a log by mapping log lines to partitions.
	 */
	@Test
	public void parseWithMappedPartitionsTest() {
		// TODO
	}
}
