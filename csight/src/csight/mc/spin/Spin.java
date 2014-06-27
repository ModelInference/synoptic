package csight.mc.spin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Logger;

import csight.mc.MC;
import csight.mc.MCProcess;
import csight.mc.MCResult;

import synoptic.model.channelid.ChannelId;

/**
 * This class is a bridge for the Spin model checker. We require Spin with a
 * version of 6.3.1+ due to the -run command.
 */
public class Spin extends MC {

    static Logger logger = Logger.getLogger("Spin");

    public Spin(String mcPath) {
        super(mcPath);
    }

    /**
     * Plan on how to proceed with verification. Write the Promela input to a
     * file. Spin does not use stdin for this. The command requires a version of
     * Spin with the run command, which means 6.3.1+. Spin will return a result
     * after it finishes running. To retrieve the result, call getVerifyResult.
     */
    @Override
    public void verify(String input, int timeoutSecs) throws IOException,
            InterruptedException {

        File currentPath = new java.io.File(".");

        File promelaFile = new java.io.File("csight.pml");
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(promelaFile)));
        writer.write(input);
        writer.close();

        // Spin does not use stdin for Promela input.

        // Commands after the -run command are passed to the compiler or
        // verifier.

        // -DMEMLIM sets the memory limit in MB.
        // -DBFS sets the verify to use BFS.
        // -DREACH checks for errors up to the default depth.

        // -q makes the verifier check for empty message channels.
        // -m sets the max search depth. This may be an option later on.
        String[] command = new String[] { mcPath, "-run", "-DMEMLIM=256",
                "-DBFS", "-DREACH", "-q", "-m1000", "csight.pml" };
        mcProcess = new MCProcess(command, "", currentPath, timeoutSecs);
        mcProcess.runProcess();
    }

    /**
     * Spin's result won't be the exact thing we need. We need to take an
     * additional step to get the counterexample. This is handled by SpinResult.
     */
    @Override
    public MCResult getVerifyResult(List<ChannelId> cids) throws IOException {
        List<String> lines = mcProcess.getInputStreamContent();

        logger.info("Spin returned: " + lines.toString());

        MCResult ret = new SpinResult(lines, cids, mcPath);
        logger.info(ret.toRawString());
        return ret;
    }

}
