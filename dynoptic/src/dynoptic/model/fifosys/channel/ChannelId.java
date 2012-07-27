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
    final String chName;

    public ChannelId(int srcPid, int dstPid) {
        this(srcPid, dstPid, Integer.toString(srcPid) + "->"
                + Integer.toString(dstPid));
    }

    public ChannelId(int srcPid, int dstPid, String chName) {
        this.srcPid = srcPid;
        this.dstPid = dstPid;
        this.chName = chName;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return chName;
    }

    // //////////////////////////////////////////////////////////////////

    public int getSrcPid() {
        return srcPid;
    }

    public int getDstPid() {
        return dstPid;
    }
}
