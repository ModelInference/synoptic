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

public class EndToEndKTailsSimpleTest extends InvariMintTest {

    private EventType x = new StringEventType("x");
    private EventType y = new StringEventType("y");
    private EventType z = new StringEventType("z");
    private EventType c = new StringEventType("c");
    private EventType d = new StringEventType("d");
    private EventType f = new StringEventType("f");

    private EventType initial = StringEventType.newInitialStringEventType();
    private EventType terminal = StringEventType.newTerminalStringEventType();

    /**
     * Test on abstract log example in traces/abstract/ktails-test.
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
        sequence.add(initial);
        sequence.add(x);
        sequence.add(y);
        sequence.add(z);
        sequence.add(d);
        sequence.add(terminal);
        assertTrue(dfa.run(sequence));

        sequence.clear();
        sequence.add(initial);
        sequence.add(x);
        sequence.add(y);
        sequence.add(z);
        sequence.add(c);
        sequence.add(terminal);
        assertTrue(dfa.run(sequence));

        sequence.clear();
        sequence.add(initial);
        sequence.add(x);
        sequence.add(y);
        sequence.add(z);
        sequence.add(f);
        sequence.add(terminal);
        assertFalse(dfa.run(sequence));
    }
}
