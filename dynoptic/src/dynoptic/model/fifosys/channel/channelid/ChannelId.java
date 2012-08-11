package dynoptic.model.fifosys.channel.channelid;

/**
 * <p>
 * This is an immutable class.
 * </p>
 * A ChannelId represents a uni-directional channel, without maintaining any
 * channel state. It only records the two processes that use the channel and an
 * optional name for the channel.
 */
public class ChannelId {
    // Sender pid.
    final int srcPid;

    // Receiver pid.
    final int dstPid;

    // The name of the channel. If none is specified then this is set to
    // "srcPid->dstPid"
    //
    // NOTE: chName is not part of the hashCode/equals
    final String chName;

    // The id used by this channel in scm output.
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
        return chName;
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

}
