package dynoptic.model.fifosys.gfsm.observed;

import java.util.Set;

import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;
import synoptic.model.event.IDistEventType;

public class ObsDistEventType implements IDistEventType {

    private final DistEventType etype;

    /**
     * The set of traces where this etype was observed. The reason this is a set
     * is because transitions of a FifoSysState are aggregated across
     * transitions.
     */
    private final Set<Integer> traceIds;

    public ObsDistEventType(DistEventType etype, int traceId) {
        this.etype = etype;

        this.traceIds = Util.newSet();
        traceIds.add(traceId);
    }

    public DistEventType getDistEType() {
        return etype;
    }

    public Set<Integer> getTraceIds() {
        return traceIds;
    }

    /** Adds a single trace Id to this observed transition. */
    public void addTraceId(int traceId) {
        this.traceIds.add(traceId);
    }

    public boolean equalsIgnoringTraceIds(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }

        // Note -- other might be an ObsDistEventType, or a DistEventType, and
        // we have to handle both options correctly.
        if (other instanceof ObsDistEventType) {
            if (!this.etype.equals(((ObsDistEventType) other).etype)) {
                return false;
            }

        } else if (other instanceof DistEventType) {
            if (!this.etype.equals((DistEventType) other)) {
                return false;
            }

        } else {
            return false;
        }

        return true;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        int result = etype.hashCode();
        result = 31 * result + traceIds.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (!equalsIgnoringTraceIds(other)) {
            return false;
        }

        if (!(other instanceof ObsDistEventType)) {
            return false;
        }

        if (!this.traceIds.equals(((ObsDistEventType) other).traceIds)) {
            return false;
        }
        return true;
    }

    @Override
    public int getPid() {
        return etype.getPid();
    }

    @Override
    public ChannelId getChannelId() {
        return etype.getChannelId();
    }

    @Override
    public boolean isCommEvent() {
        return etype.isCommEvent();
    }

    @Override
    public boolean isRecvEvent() {
        return etype.isRecvEvent();
    }

    @Override
    public boolean isSendEvent() {
        return etype.isSendEvent();
    }

    @Override
    public boolean isSynthSendEvent() {
        return etype.isSynthSendEvent();
    }

    @Override
    public boolean isLocalEvent() {
        return etype.isLocalEvent();
    }

    @Override
    public String getScmEventString() {
        return etype.getScmEventString();
    }

    @Override
    public String getScmEventFullString() {
        return etype.getScmEventFullString();
    }

    @Override
    public String toDotString() {
        return etype.toDotString();
    }

    @Override
    public String toString(String cidString, char separator) {
        return etype.toString(cidString, separator);
    }

    @Override
    public String toString() {
        String ret;
        if (etype.getChannelId() != null) {
            ret = toString(etype.getChannelId().toString(), ' ');
        }
        ret = toString("", ' ');
        return ret + "; tids: " + traceIds.toString();
    }

    @Override
    public String getEType() {
        return etype.getEType();
    }
}
