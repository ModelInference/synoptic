package dynoptic.model.fifosys.channel;

/**
 * <p>
 * This is an immutable class.
 * </p>
 * A ChannelId represents a uni-directional channel, without maintaining any
 * channel state. It only records the two processes that use the channel and an
 * optional name for the channel.
 */
public final class ChannelId {
    // Sender pid.
    final int srcPid;

    // Receiver pid.
    final int dstPid;

    // The name of the channel. If none is specified then this is set to
    // "srcPid->dstPid"
    //
    // NOTE: chName is not part of the hashCode/equals
    final String chName;

    public ChannelId(int srcPid, int dstPid) {
        this(srcPid, dstPid, Integer.toString(srcPid) + "->"
                + Integer.toString(dstPid));
    }

    public ChannelId(int srcPid, int dstPid, String chName) {
        assert srcPid >= 0 && dstPid >= 0;
        this.srcPid = srcPid;
        this.dstPid = dstPid;
        this.chName = chName;
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
        return oCid.dstPid == srcPid;
    }

    // //////////////////////////////////////////////////////////////////

    public int getSrcPid() {
        return srcPid;
    }

    public int getDstPid() {
        return dstPid;
    }

}
