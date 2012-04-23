package tests.integration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import main.InvariMintMain;
import main.InvariMintOptions;
import model.EncodedAutomaton;

import org.junit.Test;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;

import tests.InvariMintTest;

public class EndToEndKTailsTest extends InvariMintTest {
    /**
     * Test on osx-login-example in traces/abstract/.
     * 
     * @throws Exception
     */
    @Test
    public void simpleLogTest() throws Exception {
        String tPath = ".." + File.separator + "traces" + File.separator
                + "abstract" + File.separator + "ktails-test" + File.separator;

        String[] args = new String[] { "--performKTails=true", "--kTailLength",
                "2", "--runSynoptic=false", "-r",
                "^(?<DTIME>)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktails-example", tPath + "trace.txt" };

        InvariMintOptions opts = new InvariMintOptions(args);
        EncodedAutomaton dfa = InvariMintMain.runInvariMint(opts);

        List<EventType> sequence = new ArrayList<EventType>();
        sequence.add(new StringEventType("INITIAL"));
        sequence.add(new StringEventType("x"));
        sequence.add(new StringEventType("y"));
        sequence.add(new StringEventType("z"));
        sequence.add(new StringEventType("d"));
        sequence.add(new StringEventType("TERMINAL"));
        assertTrue(dfa.run(sequence));

        sequence.remove(4);
        sequence.add(4, new StringEventType("c"));
        assertTrue(dfa.run(sequence));

        sequence.remove(4);
        sequence.add(4, new StringEventType("f"));
        assertFalse(dfa.run(sequence));

    }
}
