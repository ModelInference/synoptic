package tests.integration;

import java.io.File;

import main.KTailInvariMintMain;

import org.junit.Test;

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

        String[] args = new String[] { "--kTailLength", "2", "-r",
                "^(?<DTIME>)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktails-example", tPath + "trace.txt" };
        KTailInvariMintMain.main(args);
    }
}
