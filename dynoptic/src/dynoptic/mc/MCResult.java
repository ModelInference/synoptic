package dynoptic.mc;

import java.util.List;

import synoptic.model.channelid.ChannelId;

/**
 * A generic model checking result class. The result, at minimum, must know if
 * the property being checked was violated, and if so then it also knows about
 * the corresponding counter-example.
 */
public abstract class MCResult {

    protected boolean modelIsSafe;
    protected MCcExample cExample = null;

    protected final List<String> verifyRawLines;
    protected final List<ChannelId> cids;

    public MCResult(List<String> verifyRawLines, List<ChannelId> cids) {
        this.verifyRawLines = verifyRawLines;
        this.cids = cids;
    }

    public boolean modelIsSafe() {
        return this.modelIsSafe;
    }

    public MCcExample getCExample() {
        return cExample;
    }

    public String toRawString() {
        String ret = "";
        for (String line : verifyRawLines) {
            ret += line + "\n";
        }
        return ret;
    }
}
