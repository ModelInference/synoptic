package csight.mc.spin;

import java.util.List;

import csight.mc.MCResult;
import csight.mc.MCSyntaxException;
import csight.mc.mcscm.VerifyOutputParseException;

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
