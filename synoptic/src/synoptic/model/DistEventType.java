package synoptic.model;

/**
 * Implements an EventType for a partially ordered log. Here, the event type is
 * a unique string, that is also associated with an abstract "process" via some
 * identifier. The pid doesn't have to be a physical process id. For example, it
 * could also be interpreted as a role that a host performs in the system (e.g.
 * replica role id).
 */
public class DistEventType extends EventType {
    private final String eType;
    private String processId;
    private final static String syntheticEventPID = "-1";

    private DistEventType(String eType, String pid, boolean isInitialEventType,
            boolean isTerminalEventType) {
        super(isInitialEventType, isTerminalEventType);
        this.eType = eType;
        processId = pid;
    }

    /**
     * Creates a new DistEventType that is a non-INITIAL and non-TERMINAL with a
     * pid.
     */
    public DistEventType(String type, String pid) {
        this(type, pid, false, false);
    }

    /**
     * Creates a new DistEventType that is a non-INITIAL and non-TERMINAL
     * without a pid.
     */
    public DistEventType(String type) {
        // We set pid to "-1" to indicate that it is not initialized.
        this(type, "-1", false, false);
    }

    /**
     * Creates a new DistEventType that is an INITIAL.
     */
    static public DistEventType NewInitialDistEventType() {
        return new DistEventType(EventType.initialNodeLabel, syntheticEventPID,
                true, false);
    }

    /**
     * Creates a new DistEventType that is an TERMINAL.
     */
    static public DistEventType NewTerminalDistEventType() {
        return new DistEventType(EventType.terminalNodeLabel,
                syntheticEventPID, false, true);
    }

    // ///////////////////////////////////////////////////////////////////////

    public String getPID() {
        return processId;
    }

    public String setPID(String pid) {
        return processId = pid;
    }

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
        return processId.compareTo(dother.processId);
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
                && (processId.equals(((DistEventType) other).processId));
    }

    @Override
    public String toString() {
        return eType + "_" + processId;
    }

    @Override
    public int hashCode() {
        return (31 * eType.hashCode() + 73 * processId.hashCode() + 127 * super
                .hashCode());
    }
}
