package synoptic.model.channelid;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * This class is used solely by Dynoptic and is included in Synoptic to simplify
 * compatibility and to keep Synoptic independent of Dynoptic.
 * </p>
 * <p>
 * Immutable class.
 * </p>
 * A ChannelId names a channel, or queue, without maintaining any state. It only
 * records the two processes that use the channel and an optional name for the
 * channel.
 */
public class ChannelId {
    private static Logger logger = Logger.getLogger("ChannelId");

    // Sender pid.
    final int srcPid;

    // Receiver pid.
    final int dstPid;

    // The name of the channel. If none is specified then this is set to
    // "srcPid->dstPid"
    //
    // NOTE: chName is not part of the hashCode/equals
    final String chName;

    // The id used by this channel in scm output (input to the model checker).
    private final int scmId;

    public ChannelId(int srcPid, int dstPid, int scmId) {
        this(srcPid, dstPid, scmId, Integer.toString(srcPid) + "->"
                + Integer.toString(dstPid));
    }

    public ChannelId(int srcPid, int dstPid, int scmId, String chName) {
        assert srcPid >= 0 && dstPid >= 0;
        assert chName != null;

        this.srcPid = srcPid;
        this.dstPid = dstPid;
        this.chName = chName;
        this.scmId = scmId;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return Integer.toString(scmId) + "-" + chName + ":"
                + Integer.toString(srcPid) + "->" + Integer.toString(dstPid);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + srcPid;
        result = 31 * result + dstPid;
        result = 31 * result + scmId;
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof ChannelId)) {
            return false;

        }
        ChannelId oCid = (ChannelId) other;
        if (oCid.srcPid != srcPid) {
            return false;
        }
        if (oCid.scmId != scmId) {
            return false;
        }
        if (!oCid.chName.equals(chName)) {
            return false;
        }
        return oCid.dstPid == dstPid;
    }

    // //////////////////////////////////////////////////////////////////

    public int getSrcPid() {
        return srcPid;
    }

    public int getDstPid() {
        return dstPid;
    }

    public int getScmId() {
        return scmId;
    }

    public String getName() {
        return chName;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Parses the channelSpec command line option value and returns a list of
     * corresponding channelId instances.
     * 
     * @throws Exception
     */
    public static List<ChannelId> parseChannelSpec(String channelSpec)
            throws Exception {
        // Parse the channelSpec option value into channelId instances.
        int scmId = 0, srcPid, dstPid;
        String chName;

        // This pattern matcher will match strings like "M:0->1;A:1->0", which
        // defines two channels -- 'M' and 'A'. The M channel has pid 0 as
        // sender and pid 1 as receiver, and the A channel has pid 1 as sender
        // and pid 0 as receiver.
        Pattern pattern = Pattern.compile("(.*?):(\\d+)\\-\\>(\\d+);*");
        Matcher matcher = pattern.matcher(channelSpec);

        ChannelId cid;
        List<ChannelId> cids = new ArrayList<ChannelId>();
        Set<String> chNames = new LinkedHashSet<String>();
        int lastEnd = 0;
        while (matcher.find()) {
            // logger.info("Found text" + " \"" + matcher.group()
            // + "\" starting at " + "index " + matcher.start()
            // + "  and ending at index " + matcher.end());
            chName = matcher.group(1);
            srcPid = Integer.parseInt(matcher.group(2));
            dstPid = Integer.parseInt(matcher.group(3));
            lastEnd = matcher.end();
            if (chNames.contains(chName)) {
                // Channel names should be unique since in the log channels are
                // identified solely by the channel name.
                throw new Exception(
                        "Channel spec contains multiple entries for channel '"
                                + chName + "'.");
            }
            chNames.add(chName);

            cid = new ChannelId(srcPid, dstPid, scmId, chName);
            logger.info("Parsed ChannelId : " + cid.toString());
            cids.add(cid);
            scmId += 1;
        }

        if (lastEnd != channelSpec.length()) {
            throw new Exception(
                    "Failed to completely parse the channel spec. Parsed up to char position "
                            + lastEnd);
        }
        return cids;
    }

}
