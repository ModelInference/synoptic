package synoptic.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * (( TODO ))
 */
public class VariablePartition extends Partition
        implements IVariableNode<Partition> {
    /**
     * Any EventNode instances added MUST be of an EventType compatible with ALL
     * of these, and this must always contain exactly all types in
     * {@code events}
     */
    private List<EventType> eTypes;

    private boolean hasTerminal;
    private boolean hasInitial;

    protected VariablePartition(Collection<EventNode> eNodes) {
        super(eNodes);
        assert eTypes.size() > 0;
    }

    protected VariablePartition(EventNode eNode) {
        super(eNode);
        assert eTypes.size() == 1;
    }

    @Override
    public String eTypeStr() {
        assert initialized;
        return eTypes.toString();
    }

    @Override
    public List<EventType> getAllETypes() {
        return eTypes;
    }

    @Override
    protected void initializeEType(EventNode eNode) {
        refreshETypes(Arrays.asList(eNode));
    }

    @Override
    protected void initializeEType(Collection<EventNode> eNodes) {
        refreshETypes(eNodes);
    }

    @Override
    protected boolean isLegalEType(EventType otherEType) {
        // For initial and terminal, partition and type just need to match
        if ((hasInitial && otherEType.isInitialEventType())
                || (hasTerminal && otherEType.isTerminalEventType())) {
            return true;
        }
        if (hasInitial || hasTerminal || otherEType.isSpecialEventType()) {
            return false;
        }

        // The event's type must be compatible with all of this partition's
        // event types
        for (EventType eType : eTypes) {
            if (!eType.typeEquals(otherEType)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void refreshETypes() {
        refreshETypes(events);
    }

    /**
     * Reset the event types of this partition to those within {@code eNodes},
     * and set {@link #hasTerminal} and {@link #hasInitial}
     */
    private void refreshETypes(Collection<EventNode> eNodes) {
        // Reset types and terminal/initial flags
        eTypes = new LinkedList<>();
        hasTerminal = hasInitial = false;

        // Populate types and terminal/initial flags
        for (EventNode eNode : eNodes) {
            eTypes.add(eNode.getEType());

            if (eNode.isTerminal()) {
                hasTerminal = true;
            } else if (eNode.isInitial()) {
                hasInitial = true;
            }
        }
    }

    @Override
    protected boolean hasTerminal() {
        return hasTerminal;
    }

    @Override
    protected boolean hasInitial() {
        return hasInitial;
    }

    @Override
    public int compareETypes(INode<?> other) {
        // Order this partition after any less complex, non-variable nodes
        if (!(other instanceof VariablePartition)) {
            return 1;
        }

        VariablePartition varOther = (VariablePartition) other;
        Collection<EventType> otherETypes = varOther.getAllETypes();

        // Compare special partitions
        if (hasInitial || varOther.hasInitial || hasTerminal
                || varOther.hasTerminal) {
            int specialCmp = compareSpecialType(varOther);
            if (specialCmp != 0) {
                return specialCmp;
            }
        }

        // If partitions have different numbers of event types, simply order by
        // number of types
        if (eTypes.size() < otherETypes.size()) {
            return -1;
        } else if (eTypes.size() > otherETypes.size()) {
            return 1;
        }

        // Perform n-to-m comparison of all event types in both partitions
        for (EventType thisEType : eTypes) {
            for (EventType otherEType : otherETypes) {
                int eTypeCmp = thisEType.compareTo(otherEType);
                if (eTypeCmp != 0) {
                    return eTypeCmp;
                }
            }
        }

        // No differences found
        return 0;
    }

    @Override
    public boolean eTypesEqual(INode<?> other) {
        // Must be a variably-typed partition
        if (!(other instanceof VariablePartition)) {
            return false;
        }

        VariablePartition varOther = (VariablePartition) other;

        // Types of special partitions are equal if both are initial or both are
        // terminal
        if (hasInitial || varOther.hasInitial || hasTerminal
                || varOther.hasTerminal) {
            return compareSpecialType(varOther) == 0;
        }

        // Perform n-to-m check of all event types in both partitions
        for (EventType thisEType : eTypes) {
            for (EventType otherEType : varOther.getAllETypes()) {
                if (!thisEType.typeEquals(otherEType)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 
     */
    private int compareSpecialType(VariablePartition varOther) {
        // Check for matching special type
        if ((hasInitial == varOther.hasInitial) && (hasTerminal == varOther.hasTerminal)) {
            return 0;
        }
        // Sort this partition before if it is initial or other is terminal
        if (hasInitial || varOther.hasTerminal) {
            return -1;
        }
        // Sort this partition after if it is terminal or other is initial
        if (hasTerminal || varOther.hasInitial) {
            return 1;
        }
        // Reaching here means this method was illegally called when neither
        // partition was special
        assert false;
        return 0;
    }

    @Override
    public boolean hasEType(EventType otherEType) {
        return eTypes.contains(otherEType);
    }
}
