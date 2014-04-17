package synoptic.model;

import daikonizer.DaikonInvariants;

import synoptic.util.time.ITime;
import synoptic.util.time.TimeSeries;

/**
 * The set of labels that can be associated with a transition in Synoptic
 * models. This enum defines the set, and records a description and a data type
 * associated with each label.
 */
public enum TransitionLabelType {
    /**
     * The number of events that take this transition from the source node
     */
    COUNT_LABEL("count", Integer.class),
    /**
     * The fraction of all events that take this transition from the source
     * node. The fraction is the count divided by the number of events belonging
     * to the source node, which can also be considered as a transition
     * probability.
     */
    PROBABILITY_LABEL("probability", Double.class),
    /**
     * The amount of time that elapses between the two events connected by this
     * transition.
     */
    TIME_DELTA_LABEL("time-delta", ITime.class),
    /**
     * The set of all recorded times, each of which was observed to elapse
     * between the two events connected by this transition. This is like
     * TIME_DELTA_LABEL, but aggregated over multiple edges.
     */
    TIME_DELTA_SERIES_LABEL("time-delta series", TimeSeries.class),
    /**
     * The set of relations associated with this transition.
     */
    RELATIONS_LABEL("relations", RelationsSet.class),
    /**
     * The list of Daikon invariants.
     */
    DAIKON_INVARIANTS_LABEL("daikon-invariants", DaikonInvariants.class);

    /**
     * A string description of this label type.
     */
    String desc;
    Class<?> cls;

    TransitionLabelType(String desc, Class<?> cls) {
        this.desc = desc;
        this.cls = cls;
    }

    @Override
    public String toString() {
        return desc;
    }
}
