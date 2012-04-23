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

/**
 * Runs the DFAMain project end-to-end on a different log files.
 * 
 * @author ivan
 */
public class EndToEndMainTests extends InvariMintTest {

    /**
     * Test on osx-login-example in traces/abstract/.
     * 
     * @throws Exception
     */
    @Test
    public void abstractLogFileTest() throws Exception {
        String tPath = ".." + File.separator + "traces" + File.separator;
        String loginExamplePath = tPath + "abstract" + File.separator
                + "osx-login-example" + File.separator;

        String[] args = new String[] { "-r", "(?<TYPE>.+)", "-s", "--", "-o",
                testOutputDir + "osx-login-example", "-runSynoptic=false",
                loginExamplePath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        EncodedAutomaton dfa = InvariMintMain.runInvariMint(opts);

        List<EventType> sequence = new ArrayList<EventType>();
        sequence.add(new StringEventType("INITIAL"));
        sequence.add(new StringEventType("login attempt"));
        sequence.add(new StringEventType("guest login"));
        sequence.add(new StringEventType("authorized"));
        sequence.add(new StringEventType("TERMINAL"));
        assertTrue(dfa.run(sequence));

        sequence.add(2, new StringEventType("auth failed"));
        // Now we have INITIAL --> login attempt --> auth failed --> guest login
        // --> authorized --> TERMINAL (invalid)
        assertFalse(dfa.run(sequence));

        sequence.remove(3);
        // Now we have INITIAL --> login attempt --> auth failed
        // --> authorized --> TERMINAL (valid)
        assertTrue(dfa.run(sequence));

        sequence.add(3, new StringEventType("login attempt"));
        sequence.add(4, new StringEventType("auth failed"));
        sequence.add(5, new StringEventType("login attempt"));
        // Now we have INITIAL --> login attempt --> auth failed --> login
        // attempt --> auth failed --> login attempt
        // --> authorized --> TERMINAL (valid)
        assertTrue(dfa.run(sequence));

        sequence.remove(6);
        // Now we have INITIAL --> login attempt --> auth failed --> login
        // attempt --> auth failed --> login attempt
        // --> TERMINAL (invalid)
        assertFalse(dfa.run(sequence));
    }
}