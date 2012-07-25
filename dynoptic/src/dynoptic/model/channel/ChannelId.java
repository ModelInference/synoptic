package dynoptic.model.channel;

/**
 * <p>
 * This is an immutable class.
 * </p>
 * A ChannelId represents a uni-directional channel, without maintaining any
 * channel state. It only records the two processes that use the channel and an
 * optional name for the channel.
 */
public final class ChannelId {
    final int srcPid;
    final int dstPid;
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

    public int getSrcPid() {
        return srcPid;
    }

    public int getDstPid() {
        return dstPid;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return chName;
    }
}
