package synoptic.model.event;

/**
 * Implements an EventType of a generic object type for a totally ordered log
 */
public class GenericEventType<T extends Comparable<T>> extends EventType {
    private final T eType;

    /**
     * Most expressive constructor that is used internally.
     */
    private GenericEventType(T type, boolean isInitialEventType, boolean isTerminalEventType) {
        super(isInitialEventType, isTerminalEventType);
        eType = type;
    }

    /**
     * Creates a new GenericEventType that is a non-INITIAL and non-TERMINAL.
     */
    public GenericEventType(T type) {
        this(type, false, false);
    }

    /**
     * Creates a new GenericEventType that is an INITIAL.
     */

    static public <U extends Comparable<U>> GenericEventType<U> newInitialGenericEventType() {
        return new GenericEventType<U>(null, true, false);
    }

    /**
     * Creates a new GenericEventType that is a TERMINAL.
     */
    static public <U extends Comparable<U>> GenericEventType<U> newTerminalGenericEventType() {
        return new GenericEventType<U>(null, false, true);
    }

    // ///////////////////////////////////////////////////////////////////////

    @Override
    public T getETypeLabel() {
        return eType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(EventType eOther) {
        int baseCmp = super.compareTo(eOther);
        if (baseCmp != 0) {
            return baseCmp;
        }
        GenericEventType<T> eOtherCast = null;
        try {
            eOtherCast = (GenericEventType<T>) eOther;
        } catch (ClassCastException cce) {
            return -1;
        }
        return eType.compareTo(eOtherCast.eType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }
        GenericEventType<T> otherCast = null;
        try {
            otherCast = (GenericEventType<T>) other;
        } catch (ClassCastException cce) {
            return false;
        }
        return eType.equals(otherCast.eType);
    }

    @Override
    public String toString() {
        return eType.toString();
    }

    @Override
    public int hashCode() {
        return eType.hashCode() + super.hashCode();
    }
}
