package tests.integration;

import java.io.File;

import main.InvariMintMain;

import org.junit.Test;

import synoptic.main.SynopticMain;

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
                testOutputDir + "osx-login-example",
                loginExamplePath + "trace.txt" };
        SynopticMain.instance = null;
        InvariMintMain.main(args);
    }
}