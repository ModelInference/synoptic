package tests.integration;

import main.DFAMain;

import org.junit.Test;

import tests.InvDFAMinimizationTest;

/**
 * Runs the DFAMain project end-to-end on a different log files.
 * 
 * @author ivan
 */
public class EndToEndMainTests extends InvDFAMinimizationTest {

    /**
     * Test on osx-login-example in traces/abstract/.
     * 
     * @throws Exception
     */
    @Test
    public void abstractLogFileTest() throws Exception {
        // TODO: make the path insensitive to current location.
        String tPath = "../traces/";

        String[] args = new String[] { "-c",
                tPath + "abstract/osx-login-example/args.txt",
                tPath + "abstract/osx-login-example/trace.txt" };
        DFAMain.main(args);
    }
}