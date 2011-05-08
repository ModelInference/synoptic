package synoptic.model;

public abstract class EventType implements Comparable<EventType> {
    // The INITIAL node event type.
    boolean isInitialEventType;
    // The TERMINAL node event type.
    boolean isTerminalEventType;

    public EventType(boolean isInitialEventType, boolean isTerminalEventType) {
        assert (!(isInitialEventType && isTerminalEventType));
        this.isInitialEventType = isInitialEventType;
        this.isTerminalEventType = isTerminalEventType;
    }

    public boolean isInitialEventType() {
        return isInitialEventType;
    }

    public boolean isTerminalEventType() {
        return isTerminalEventType;
    }

    public boolean isSpecialEventType() {
        return isInitialEventType || isTerminalEventType;
    }

}
