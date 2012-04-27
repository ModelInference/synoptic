package synoptic.model.interfaces;

import java.util.Set;

import synoptic.util.time.ITime;
import synoptic.util.time.ITimeSeries;

/**
 * A generic interface for a transition. This interface provides method to
 * modify the transition.
 * 
 * @author sigurd
 * @param <NodeType>
 *            the type of the nodes which are connected by this transition
 */
public interface ITransition<NodeType> extends
        Comparable<ITransition<NodeType>> {
    /**
     * Get the target node.
     * 
     * @return the target node
     */
    NodeType getTarget();

    /**
     * Get the source node.
     * 
     * @return the source node
     */
    NodeType getSource();

    /**
     * Returns the label of the transition (i.e. the relation)
     * 
     * @return the name of the relation
     */
    Set<String> getRelation();

    /**
     * Set the target node.
     * 
     * @param target
     *            the new target node
     */
    void setTarget(NodeType target);

    /**
     * Set the source node.
     * 
     * @param source
     *            the new source node
     */
    void setSource(NodeType source);

    /**
     * Get the delta time of the transition. Cannot be called after a delta
     * series has been successfully set via the getDeltaSeries or addDelta
     * methods.
     * 
     * @return delta time
     * @throws IllegalStateException
     *             if called after a delta series has been set.
     */
    ITime getDelta();

    /**
     * Set the delta. Cannot be called after the delta series has been
     * successfully set via the getDeltaSeries or addDelta methods.
     * 
     * @param d
     *            the new delta time
     * @throws IllegalStateException
     *             if called after a delta series has been set.
     */
    void setDelta(ITime d);

    /**
     * Returns the series of times contained within the Transition. If there is
     * no series yet (and there is no single delta set), then an empty one will
     * be created. Cannot be called after a single delta has been successully
     * set via the setDelta method.
     * 
     * @return the delta time series corresponding to this transition.
     * @throws IllegalStateException
     *             if called after a single delta has been set.
     */
    ITimeSeries<ITime> getDeltaSeries();

    /**
     * Adds a delta to the series. If the series of deltas is missing, then one
     * will be created. Cannot be called after a single delta has been
     * successfully set via the setDelta method.
     * 
     * @param d
     *            The delta to be added to the series.
     * @throws IllegalStateException
     *             if called after a single delta has been set.
     */
    void addDelta(ITime d);

    /**
     * Get a short description of the transition
     * 
     * @return a short description
     */
    String toStringConcise();
}
