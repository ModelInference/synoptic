package synoptic.tests.units;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import synoptic.main.SynopticMain;
import synoptic.main.parser.ParseException;
import synoptic.model.EventNode;
import synoptic.tests.PynopticTest;
import synoptic.util.InternalSynopticException;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.ITime;

public class NodeNormalizationTests extends PynopticTest {

    private String[] events = { "a 2", "b 3", "c 5", "d 6", "--", "a 10",
            "b 11", "c 14", "d 16" };

    /**
     * Verifies that, after node normalization, there is one node with time
     * stamp 0 and one with time stamp 1, and that all other time stamps are in
     * between
     * 
     * @throws InternalSynopticException
     * @throws ParseException
     */
    @Test
    public void nodeNormalizationTest() throws InternalSynopticException,
            ParseException {
        ArrayList<EventNode> nodes = parseLogEvents(events, genITimeParser());

        SynopticMain.normalizeEventNodes(nodes);

        // Exactly one time stamp with value zero should appear in the list
        boolean zeroFound = false;
        ITime zeroTime = new DTotalTime(0);
        
        // Exactly one time stamp with value one should appear in the list
        boolean oneFound = false;
        ITime oneTime = new DTotalTime(1);

        for (EventNode node : nodes) {
            // Verifies that every time stamp is greater than or equal zero
            assertEquals(true, zeroTime.compareTo(node.getTime()) <= 0);
            
            // Verifies that every time stamp is lower than or equal one
            assertEquals(true, oneTime.compareTo(node.getTime()) >= 0);

            if (zeroTime.compareTo(node.getTime()) == 0) {
                zeroFound = true;
            } else if (oneTime.compareTo(node.getTime()) == 0) {
                oneFound = true;
            }
        }

        assert (zeroFound);
        assert (oneFound);
    }
}
