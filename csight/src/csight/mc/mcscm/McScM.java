package csight.mc.mcscm;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import csight.mc.MC;
import csight.mc.MCProcess;
import csight.mc.MCResult;

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
     * Runs the verify commands in parallel with the scmInput string as input.
     * 
     * @param scmInput
     * @throws IOException
     */
    public void verifyParallel(String scmInput) throws IOException {
        File currentPath = new java.io.File(".");

        // TODO: use .... &  to run parallel processes in Linux
        //       use -n .... to run .................. in OSX
        if (Os.isLinux()) {
            mcProcess = new MCProcess(new String[] { mcPath, "-no-validation",
                    "-quiet", "&" }, scmInput, currentPath, Integer.MAX_VALUE);
        } else if (Os.isMac()) {
            mcProcess = new MCProcess(new String[] { "-n " + mcPath, "-no-validation",
                    "-quiet" }, scmInput, currentPath, Integer.MAX_VALUE);
        } else {
            throw new RuntimeException(
                    "Running on an unsupported OS (not Linux, and not Mac).");
        }

        try {
            mcProcess.runProcess();
        } catch (InterruptedException e) {
            // We don't need the timeout from MCProcess
            // TODO: remove timeout from MCProcess when approved
        }
    }

    /**
     * Interprets the output of running verify and returns a VerifyResult
     * instance that includes a counter-example (if any) from the model checking
     * run. Uses cids to interpret event instances in the counter-example --
     * each of these is associated with some channel (a synthetic
     * invariant/local channel, or an actual channel that is part of the
     * system).
     */
    public MCResult getVerifyResult(List<ChannelId> cids) throws IOException {
        List<String> lines = mcProcess.getInputStreamContent();

        logger.info("McScM returned: " + lines.toString());
        MCResult ret = new McScMResult(lines, cids);
        return ret;
    }
}
