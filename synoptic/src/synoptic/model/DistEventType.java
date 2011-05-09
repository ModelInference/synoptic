package synoptic.model;

/**
 * Implements an EventType for a partially ordered log. Here, the event type is
 * a unique string, that is also associated with an abstract "host" via some
 * integer identifier. The host id doesn't have to be a physical host id. For
 * example, it could also be interpreted as a role that the host performs in the
 * system (e.g. replica role id).
 */
public class DistEventType extends EventType {
    private final String eType;
    private final int hostId;
    private final static int syntheticEventHostId = -1;

    private DistEventType(String eType, int hostId, boolean isInitialEventType,
            boolean isTerminalEventType) {
        super(isInitialEventType, isTerminalEventType);
        this.eType = eType;
        this.hostId = hostId;
    }

    /**
     * Creates a new DistEventType that is a non-INITIAL and non-TERMINAL.
     */
    public DistEventType(String type, int hostId) {
        this(type, hostId, false, false);
    }

    /**
     * Creates a new DistEventType that is an INITIAL.
     */
    static public DistEventType NewInitialDistEventType() {
        return new DistEventType(EventType.initialNodeLabel,
                syntheticEventHostId, true, false);
    }

    /**
     * Creates a new DistEventType that is an TERMINAL.
     */
    static public DistEventType NewTerminalDistEventType() {
        return new DistEventType(EventType.terminalNodeLabel,
                syntheticEventHostId, false, true);
    }

    // ///////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(EventType eother) {
        int baseCmp = super.compareTo(eother);
        if (baseCmp != 0) {
            return baseCmp;
        }
        DistEventType dother = (DistEventType) eother;
        int eTypeCmp = eType.compareTo(dother.eType);
        if (eTypeCmp != 0) {
            return eTypeCmp;
        }
        return ((Integer) hostId).compareTo(dother.hostId);
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return eType.equals(((DistEventType) other).eType)
                && (hostId == ((DistEventType) other).hostId);
    }

    @Override
    public String toString() {
        return super.toString() + eType + "_" + hostId;
    }

    @Override
    public int hashCode() {
        return eType.hashCode() + hostId + super.hashCode();
    }
}
