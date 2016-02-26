package synoptic.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import synoptic.model.event.EventType;

/**
 * TODO
 */
public class UniformPartition extends Partition
        implements IUniformNode<Partition> {

    /**
     * The EventType of this partition -- all EventNode instances within this
     * partition MUST be of this type
     */
    protected EventType eType = null;

    protected UniformPartition(Collection<EventNode> eNodes) {
        super(eNodes);
    }

    protected UniformPartition(EventNode eNode) {
        super(eNode);
    }

    @Override
    public List<EventType> getAllETypes() {
        assert initialized;
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
    protected int compareETypes(Partition other) {
        // Order this partition before any that are more complex, non-uniform
        // node types
        if (!(other instanceof IUniformNode<?>)) {
            return -1;
        }

        IUniformNode<?> uniformOther = (IUniformNode<?>) other;
        return eType.compareTo(uniformOther.getEType());
    }
}
