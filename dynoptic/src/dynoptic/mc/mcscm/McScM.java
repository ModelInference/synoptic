package dynoptic.mc.mcscm;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import dynoptic.mc.MC;
import dynoptic.mc.MCProcess;

import synoptic.model.channelid.ChannelId;

/** An interface to the McScM model checker. */
public class McScM extends MC {

    static Logger logger = Logger.getLogger("McScM");

    public McScM(String mcPath) {
        super(mcPath);
    }

    /**
     * Runs the verify commands with the scmInput string as input.
     * 
     * @param scmInput
     * @param timeoutSecs
     * @throws InterruptedException
     * @throws IOException
     */
    public void verify(String scmInput, int timeoutSecs) throws IOException,
            InterruptedException {
        File currentPath = new java.io.File(".");

        mcProcess = new MCProcess(new String[] { mcPath, "-no-validation",
                "-quiet" }, scmInput, currentPath, timeoutSecs);
        mcProcess.runProcess();
    }

    /**
     * Interprets the output of running verify and returns a VerifyResult
     * instance that includes a counter-example (if any) from the model checking
     * run. Uses cids to interpret event instances in the counter-example --
     * each of these is associated with some channel (a synthetic
     * invariant/local channel, or an actual channel that is part of the
     * system).
     */
    public McScMResult getVerifyResult(List<ChannelId> cids)
            throws IOException {
        List<String> lines = mcProcess.getInputStreamContent();

        logger.info("Verify returned: " + lines.toString());
        McScMResult ret = new McScMResult(lines, cids);
        return ret;
    }
}
