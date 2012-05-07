package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ConstrainedInvMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.main.parser.ParseException;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.event.Event;
import synoptic.model.event.StringEventType;
import synoptic.tests.SynopticTest;

public class ConstrainedInvMinerTests extends SynopticTest {
	ITOInvariantMiner miner;
	
	 @Override
	 public void setUp() throws ParseException {
		 super.setUp();
		 miner = new ChainWalkingTOInvMiner();
	 }
	 
  /**
     * Generates a TemporalInvariantSet based on a sequence of log events -- a
     * set of invariants that are mined from the log, and hold true for the
     * initial graph of the log.
     * 
     * @param events
     *            log of events, each one in the format: (?<TYPE>)
     * @return an invariant set for the input log
     * @throws Exception
     */
    public TemporalInvariantSet genInvariants(String[] events,
            boolean multipleRelations) throws Exception {
        ChainsTraceGraph inputGraph = genInitialLinearGraph(events);
        TemporalInvariantSet invs = miner.computeInvariants(inputGraph, false);
        return ConstrainedInvMiner.computeInvariants(inputGraph, multipleRelations, invs);
    }
	 
   /**
     * Compose a log in which "a AP b" is the only true invariant. Except that
     * we making it the only invariant complicates the trace too much so we
     * settle for also including the INITIAL AFby a invariant.
     * 
     * @throws Exception
     */
    @Test
    public void mineAPTest() throws Exception {
        String[] log = new String[] { "a", "a", "b", "--", "a", "--", "a", "b",
                "a", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log, false);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "a",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("a", "b",
                Event.defTimeRelationStr));
        logger.info("minedInvs: " + minedInvs.toString());
        assertTrue(trueInvs.sameInvariants(minedInvs));
    }
 
 
}
