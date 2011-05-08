package synoptic.model;

public class DistEventType extends EventType {
    String eType;
    int hostId;

    public DistEventType(String eType, int hostId, boolean isInitialEventType,
            boolean isTerminalEventType) {
        super(isInitialEventType, isTerminalEventType);
        this.eType = eType;
        this.hostId = hostId;
    }

    @Override
    public int compareTo(EventType other) {
        if (!(other instanceof DistEventType)) {
            throw new ClassCastException();
        }
        int eTypeCmp = eType.compareTo(((DistEventType) other).eType);
        if (eTypeCmp == 0) {
            return ((Integer) hostId).compareTo(((DistEventType) other).hostId);
        }
        return eTypeCmp;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        return eType.equals(((DistEventType) other).eType)
                && (hostId == ((DistEventType) other).hostId);
    }

    @Override
    public String toString() {
        return eType + "_" + hostId;
    }

    @Override
    public int hashCode() {
        return eType.hashCode() + hostId;
    }
}
