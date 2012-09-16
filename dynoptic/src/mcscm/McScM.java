package mcscm;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import synoptic.model.channelid.ChannelId;

/** An interface to the McScM model checker. */
public class McScM {
    static Logger logger = Logger.getLogger("McScM");

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
        verifyProcess = ProcessUtil.runVerifyProcess(
                new String[] { verifyPath }, scmInput, currentPath);
    }

    public VerifyResult getVerifyResult(List<ChannelId> cids)
            throws IOException {
        List<String> lines = ProcessUtil.getInputStreamContent(verifyProcess
                .getInputStream());

        logger.info("Verify returned: " + lines.toString());

        VerifyResult ret = new VerifyResult(lines, cids);
        return ret;
    }
}
