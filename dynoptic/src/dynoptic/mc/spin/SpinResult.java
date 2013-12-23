package dynoptic.mc.spin;

import java.util.List;

import dynoptic.mc.MCResult;
import dynoptic.mc.MCSyntaxException;
import dynoptic.mc.mcscm.VerifyOutputParseException;

import synoptic.model.channelid.ChannelId;

/**
 * Holds the result of a Spin model checker run.
 */
public class SpinResult extends MCResult {

    public SpinResult(List<String> verifyRawLines, List<ChannelId> cids)
            throws VerifyOutputParseException {
        super(verifyRawLines, cids);
        // TODO:
        throw new MCSyntaxException("todo");
    }

}
