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

public class Spin extends MC {

    static Logger logger = Logger.getLogger("Spin");

    public Spin(String mcPath) {
        super(mcPath);
    }

    @Override
    public void verify(String input, int timeoutSecs) throws IOException,
            InterruptedException {
        /**
         * Plan on how to proceed with verification. Write the Promela input to
         * a file. Spin does not use stdin for this. The command requires a
         * version of Spin with the run command, which means 6.3.1+. Run spin
         * with the command "-run -a -I filename.pml". Spin will return a result
         * after it finishes running. The -I option tries to find a faster path
         * to the error.
         */
        File currentPath = new java.io.File(".");

        File promelaFile = new java.io.File("csight.pml");
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(promelaFile)));
        writer.write(input);
        writer.close();

        // Spin does not use stdin for Promela input.
        String[] command = new String[] { mcPath, "-run", "-I", "-q",
                "csight.pml" };
        mcProcess = new MCProcess(command, "", currentPath, timeoutSecs);
        mcProcess.runProcess();
    }

    @Override
    public MCResult getVerifyResult(List<ChannelId> cids) throws IOException {
        List<String> lines = mcProcess.getInputStreamContent();

        /**
         * Spin's result won't be the exact thing we need. We need to take an
         * additional step. We parse the result for the line
         * "pan: wrote filename.pml.trail". This will indicate that we violated
         * the never claim. Unfortunately, we can't read the trail file
         * directly. We can run spin with "-t -T -B filename.pml" to grab a
         * brief version of the trail. We've placed trace statements in the
         * Promela so we just need to parse all the lines that say
         * CSightTrace[event_here]
         */
        logger.info("Spin returned: " + lines.toString());

        MCResult ret = new SpinResult(lines, cids, mcPath);
        logger.info(ret.toRawString());
        return ret;
    }

}
