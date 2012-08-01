package mcscm;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class McScM {
    /** Complete path to the McScM verify binary. */
    private String verifyPath;

    /** The started verify process. */
    private Process verifyProcess;

    /**
     * Creates a new instance of McScM that will use the specified verifyPath.
     * 
     * @param verifyPath
     */
    public McScM(String verifyPath) {
        this.verifyPath = verifyPath;
    }

    /**
     * Runs the verify commands with the scmInput string as input.
     * 
     * @param scmInput
     * @throws InterruptedException
     * @throws IOException
     */
    public void verify(String scmInput) throws IOException,
            InterruptedException {
        File currentPath = new java.io.File(".");
        verifyProcess = Util.runVerifyProcess(new String[] { verifyPath },
                scmInput, currentPath);
    }

    public VerifyResult getVerifyResult() throws IOException {
        List<String> lines = Util.getInputStreamContent(verifyProcess
                .getInputStream());
        VerifyResult ret = new VerifyResult(lines);
        return ret;
    }
}
