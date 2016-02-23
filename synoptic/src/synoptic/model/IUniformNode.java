package synoptic.model;

import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * 
 */
public interface IUniformNode<NodeType> extends INode<NodeType> {
    /**
     * Returns the label of the node. NOTE: This call is used by the LTLChecker
     * to retrieve the canonical representation of the event type.
     * 
     * @return the node's label
     */
    EventType getEType();
}
