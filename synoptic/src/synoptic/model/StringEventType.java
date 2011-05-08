package synoptic.model;

public class StringEventType extends EventType {
    String type;

    public StringEventType(String type, boolean isInitialEventType,
            boolean isTerminalEventType) {
        super(isInitialEventType, isTerminalEventType);
        this.type = type;
    }

    public StringEventType(String type) {
        this(type, false, false);
    }

    @Override
    public int compareTo(EventType other) {
        if (!(other instanceof StringEventType)) {
            throw new ClassCastException();
        }
        return type.compareTo(((StringEventType) other).type);
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
        return type.equals(((StringEventType) other).type);
    }

    @Override
    public String toString() {
        return type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

}
