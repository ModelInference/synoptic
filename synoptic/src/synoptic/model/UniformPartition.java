package synoptic.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * TODO
 */
public class UniformPartition extends Partition
        implements IUniformNode<Partition> {

    /**
     * The EventType of this partition -- all EventNode instances within this
     * partition MUST be of this type
     */
    protected EventType eType;

    protected UniformPartition(Collection<EventNode> eNodes) {
        super(eNodes);
    }

    protected UniformPartition(EventNode eNode) {
        super(eNode);
    }

    @Override
    public String eTypeStr() {
        assert initialized;
        return eType.toString();
    }

    @Override
    public List<EventType> getAllETypes() {
        return Arrays.asList(eType);
    }

    @Override
    public EventType getEType() {
        assert initialized;
        return eType;
    }

    @Override
    protected void initializeEType(EventNode eNode) {
        eType = eNode.getEType();
    }

    @Override
    protected void initializeEType(Collection<EventNode> eNodes) {
        eType = eNodes.iterator().next().getEType();
    }

    @Override
    protected boolean isLegalEType(EventType otherEType) {
        return eType.equals(otherEType);
    }

    // @Override
    // protected void checkNewENodeType(Collection<EventNode> eNodes) {
    // for (EventNode eNode : eNodes) {
    // checkNewENodeType(eNode);
    // }
    // }

    @Override
    protected void refreshETypes() {
        // Nothing to do: a uniform partition's event type changes only if it is
        // re-initialized
    }

    @Override
    protected boolean hasTerminal() {
        return eType.isTerminalEventType();
    }

    @Override
    protected boolean hasInitial() {
        return eType.isInitialEventType();
    }

    @Override
    public int compareETypes(INode<?> other) {
        // Order this partition before any more complex, non-uniform nodes
        if (!(other instanceof IUniformNode<?>)) {
            return -1;
        }

        IUniformNode<?> uniformOther = (IUniformNode<?>) other;
        return eType.compareTo(uniformOther.getEType());
    }

    @Override
    public boolean hasEType(EventType otherEType) {
        return eType.equals(otherEType);
    }
}
