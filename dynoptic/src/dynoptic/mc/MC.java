package dynoptic.mc;

import java.io.IOException;
import java.util.List;

import synoptic.model.channelid.ChannelId;

/**
 * Represents a CFSM model checker.
 */
public abstract class MC {
    /** Complete path to the model checker binary (e.g., McScM verify). */
    protected String mcPath;

    /**
     * The started model checker process instance (e.g., of the McScM verify
     * process).
     */
    protected MCProcess mcProcess;

    /**
     * Creates a new instance of McScM that will use the specified verifyPath.
     * 
     * @param verifyPath
     */
    public MC(String mcPath) {
        this.mcPath = mcPath;
    }

    /**
     * Runs the model checker on the input string representation of the CFSM.
     * Times out the process after timeoutSecs.
     */
    public abstract void verify(String input, int timeoutSecs)
            throws IOException, InterruptedException;

    /**
     * 
     */
    public abstract MCResult getVerifyResult(List<ChannelId> cids)
            throws IOException;

}
