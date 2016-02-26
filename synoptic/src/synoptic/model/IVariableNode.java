package synoptic.model;

import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * (( TODO )) support event types defining their own equality even if the types
 * are not identical
 */
public interface IVariableNode<NodeType> extends INode<NodeType> {
    /**
     * Get all event types associated with this variably-typed node
     */
    List<EventType> getAllETypes();
}
