package csight.mc.spin;

import java.io.File;
import java.io.IOException;
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

        File currentPath = new java.io.File(".");

        mcProcess = new MCProcess(new String[] { mcPath }, input, currentPath,
                timeoutSecs);
        mcProcess.runProcess();
    }

    @Override
    public MCResult getVerifyResult(List<ChannelId> cids) throws IOException {
        List<String> lines = mcProcess.getInputStreamContent();

        logger.info("Spin returned: " + lines.toString());
        MCResult ret = new SpinResult(lines, cids);
        return ret;
    }

}
