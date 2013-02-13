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

public class EndToEndOSXLoginMainTests extends InvariMintTest {
    private EventType initial = StringEventType.newInitialStringEventType();
    private EventType terminal = StringEventType.newTerminalStringEventType();

    private EventType login = new StringEventType("login attempt");
    private EventType guestLogin = new StringEventType("guest login");
    private EventType authorized = new StringEventType("authorized");
    private EventType authFailed = new StringEventType("auth failed");

    /**
     * Tests InvariMint on osx-login-example in traces/abstract/.
     */
    @Test
    public void osxLogFileTest() throws Exception {
        // String[] events = new String[] { "login attempt", "auth failed",
        // "login attempt", "authorized", "--", "login attempt",
        // "auth failed", "authorized", "--", "login attempt",
        // "authorized", "--", "login attempt", "auth failed",
        // "login attempt", "auth failed", "login attempt", "authorized",
        // "--", "login attempt", "guest login", "authorized" };

        String tPath = ".." + File.separator + "traces" + File.separator;
        String loginExamplePath = tPath + "abstract" + File.separator
                + "osx-login-example" + File.separator;

        String[] args = new String[] { "-r", "(?<TYPE>.+)", "-s", "--", "-o",
                testOutputDir + "osx-login-example", "-invMintSynoptic=true",
                loginExamplePath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        EncodedAutomaton dfa = InvariMintMain.runInvariMint(opts);

        List<EventType> sequence = new ArrayList<EventType>();
        sequence.add(initial);
        sequence.add(login);
        sequence.add(guestLogin);
        sequence.add(authorized);
        sequence.add(terminal);
        assertTrue(dfa.run(sequence));

        sequence.clear();
        sequence.add(initial);
        sequence.add(login);
        sequence.add(authFailed);
        sequence.add(guestLogin);
        sequence.add(authorized);
        sequence.add(terminal);
        assertFalse(dfa.run(sequence));

        sequence.clear();
        sequence.add(initial);
        sequence.add(login);
        sequence.add(authFailed);
        sequence.add(authorized);
        sequence.add(terminal);
        assertTrue(dfa.run(sequence));

        sequence.clear();
        sequence.add(initial);
        sequence.add(login);
        sequence.add(authFailed);
        sequence.add(login);
        sequence.add(authFailed);
        sequence.add(login);
        sequence.add(authorized);
        sequence.add(terminal);
        assertTrue(dfa.run(sequence));

        sequence.clear();
        sequence.add(initial);
        sequence.add(login);
        sequence.add(authFailed);
        sequence.add(login);
        sequence.add(authFailed);
        sequence.add(login);
        sequence.add(terminal);
        assertFalse(dfa.run(sequence));
    }

}
