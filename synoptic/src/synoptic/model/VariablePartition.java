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
     * Any EventNode instances added MUST be of an EventType considered equal to
     * ALL of these, and this must always contain exactly all types in
     * {@code events}
     */
    private final List<EventType> eTypes = new LinkedList<>();

    private boolean hasTerminal = false;
    private boolean hasInitial = false;

    protected VariablePartition(Collection<EventNode> eNodes) {
        super(eNodes);
    }

    protected VariablePartition(EventNode eNode) {
        super(eNode);
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
        // The event's type must be considered equal to all of this partition's
        // event types
        for (EventType eType : eTypes) {
            if (!eType.equals(otherEType)) {
                return false;
            }
        }
        return true;
    }

    // @Override
    // protected void checkNewENodeType(Collection<EventNode> eNodes) {
    // //
    // Set<EventType> distinctETypes = new HashSet<>();
    // }

    @Override
    protected void refreshETypes() {
        refreshETypes(events);
    }

    /**
     * Reset the event types of this partition to those within {@code eNodes},
     * and sets {@link #hasTerminal} and {@link #hasInitial}
     */
    private void refreshETypes(Collection<EventNode> eNodes) {
        // Reset types and terminal/initial flags
        eTypes.clear();
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
        if (!(other instanceof IVariableNode<?>)) {
            return 1;
        }
        
        IVariableNode<?> varOther = (IVariableNode<?>) other;
        Collection<EventType> otherETypes = varOther.getAllETypes();
        
        // If partitions have different numbers of event types, simply order by
        // number of types
        if (eTypes.size() < otherETypes.size()) {
            return -1;
        } else if (eTypes.size() > otherETypes.size()) {
            return 1;
        }

        // Perform n-to-m comparison of all event types in both partitions
        for (EventType otherEType : otherETypes) {
            if (!isLegalEType(otherEType)) {
                return -1;
            }
        }

        // No differences found
        return 0;
    }
}
