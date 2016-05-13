package synoptic.model.event;

import synoptic.model.interfaces.ISynType;

/**
 * Implements an EventType of a generic object type for a totally ordered log
 */
public class GenericEventType<T extends Comparable<T> & ISynType<T>>
        extends EventType {
    private final T eType;

    /**
     * Most expressive constructor that is used internally.
     */
    private GenericEventType(T type, boolean isInitialEventType,
            boolean isTerminalEventType) {
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

    static public <U extends Comparable<U> & ISynType<U>> GenericEventType<U> newInitialGenericEventType() {
        return new GenericEventType<U>(null, true, false);
    }

    /**
     * Creates a new GenericEventType that is a TERMINAL.
     */
    static public <U extends Comparable<U> & ISynType<U>> GenericEventType<U> newTerminalGenericEventType() {
        return new GenericEventType<U>(null, false, true);
    }

    // ///////////////////////////////////////////////////////////////////////

    @Override
    public T getETypeLabel() {
        return eType;
    }

    @Override
    public boolean typeEquals(EventType other) {
        assert other instanceof GenericEventType<?>;
        GenericEventType<?> oGenEType = (GenericEventType<?>) other;
        if (eType == null && oGenEType == null) {
            return true;
        }
        if (eType == null || oGenEType == null) {
            return false;
        }
        assert oGenEType.eType.getClass() == eType.getClass();
        return eType.typeEquals((T) oGenEType.eType);
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

        // Handle null eType of this and/or other
        if (eType == null) {
            if (eOtherCast.eType == null) {
                return 0;
            }
            return -1;
        } else if (eOtherCast.eType == null) {
            return 1;
        }

        return eType.compareTo(eOtherCast.eType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        boolean baseEql = super.equals(other);
        if (!baseEql) {
            return false;
        }
        GenericEventType<T> otherCast = null;
        try {
            otherCast = (GenericEventType<T>) other;
        } catch (ClassCastException cce) {
            return false;
        }

        //
        if (eType == null && otherCast.eType == null) {
            return baseEql;
        } else if (eType == null || otherCast.eType == null) {
            return false;
        }

        return eType.equals(otherCast.eType);
    }

    @Override
    public String toString() {
        if (eType == null) {
            if (isInitialEventType) {
                return "INITIAL";
            } else if (isTerminalEventType) {
                return "TERMINAL";
            }
        }
        return eType.toString();
    }

    @Override
    public int hashCode() {
        int hashCode = (eType == null ? 0 : eType.hashCode());
        return hashCode + super.hashCode();
    }
}
