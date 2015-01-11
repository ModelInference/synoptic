package synoptic.model;

import java.util.LinkedHashMap;
import java.util.Map;

import daikonizer.DaikonInvariants;

import synoptic.util.time.ITime;
import synoptic.util.time.TimeSeries;

/**
 * Maintains a map of labels that are associated with some transition. The types
 * of these labels are dictated by the enum TransitionLabelType.
 */
public class TransitionLabelsMap implements Comparable<TransitionLabelsMap> {

    protected final Map<TransitionLabelType, Object> labels;

    public TransitionLabelsMap() {
        labels = new LinkedHashMap<TransitionLabelType, Object>();
    }

    /**
     * Returns the value for a particular label type. If none exist, then
     * returns null.
     */
    private Object getLabel(TransitionLabelType t) {
        if (!(labels.containsKey(t))) {
            return null;
        }
        return labels.get(t);
    }

    /**
     * Associates an arbitrary label of type t with the transition.
     * 
     * @param t
     *            The type of label.
     * @param label
     *            An object representing the label value.
     */
    public void setLabel(TransitionLabelType t, Object label) {
        if (!t.cls.isAssignableFrom(label.getClass())) {
            throw new IllegalStateException(
                    "Inappropriate type of label passed for a label of type: "
                            + t.toString() + ". Expected: " + t.cls.getName()
                            + ", Got: " + label.getClass().getName());
        }
        labels.put(t, label);
    }

    /**
     * Returns the count associated with the transition, or null if none exist
     */
    public Integer getCount() {
        Object o = getLabel(TransitionLabelType.COUNT_LABEL);
        if (o == null) {
            return null;
        }
        assert (o instanceof Integer);
        return (Integer) o;
    }

    /**
     * Returns the probability associated with the transition, or null if none
     * exist.
     */
    public Double getProbability() {
        Object o = getLabel(TransitionLabelType.PROBABILITY_LABEL);
        if (o == null) {
            return null;
        }
        assert (o instanceof Double);
        return (Double) o;
    }

    /**
     * Returns the time delta associated with the transition, or null if none
     * exist.
     */
    public ITime getTimeDelta() {
        Object o = getLabel(TransitionLabelType.TIME_DELTA_LABEL);
        if (o == null) {
            return null;
        }
        assert (o instanceof ITime);
        return (ITime) o;
    }

    /**
     * Returns the time series associated with the transition, or null if none
     * exist.
     */
    @SuppressWarnings("unchecked")
    public TimeSeries<ITime> getTimeDeltaSeries() {
        Object o = getLabel(TransitionLabelType.TIME_DELTA_SERIES_LABEL);
        if (o == null) {
            return null;
        }
        assert (o instanceof TimeSeries);
        return (TimeSeries<ITime>) o;
    }

    /**
     * Returns relations associated with the transition, or null if none exist.
     */
    public RelationsSet getRelations() {
        Object o = getLabel(TransitionLabelType.RELATIONS_LABEL);
        if (o == null) {
            return null;
        }
        assert (o instanceof RelationsSet);
        return (RelationsSet) o;
    }
    
    /**
     * Returns Daikon invariants associated with the transition, or null if
     * none exist.
     */
    public DaikonInvariants getDaikonInvariants() {
        Object o = getLabel(TransitionLabelType.DAIKON_INVARIANTS_LABEL);
        if (o == null) {
            return null;
        }
        assert (o instanceof DaikonInvariants);
        return (DaikonInvariants) o;
    }

    @Override
    public int compareTo(TransitionLabelsMap o) {
        int cmp;

        cmp = ((Integer) labels.size()).compareTo(o.labels.size());
        if (cmp != 0) {
            return cmp;
        }

        // TODO: reflection would help shrink the next section.

        // Compare counts.
        if (comparePossiblyNullObjects(this.getCount(), o.getCount()) == -2) {
            cmp = this.getCount().compareTo(o.getCount());
            if (cmp != 0) {
                return cmp;
            }
        }

        // Compare probabilities.
        if (comparePossiblyNullObjects(this.getProbability(),
                o.getProbability()) == -2) {
            cmp = Double.compare(this.getProbability(), o.getProbability());
            if (cmp != 0) {
                return cmp;
            }
        }

        // Compare time deltas.
        if (comparePossiblyNullObjects(this.getTimeDelta(), o.getTimeDelta()) == -2) {
            cmp = this.getTimeDelta().compareTo(o.getTimeDelta());
            if (cmp != 0) {
                return cmp;
            }
        }

        // Compare time delta series.
        if (comparePossiblyNullObjects(this.getTimeDeltaSeries(),
                o.getTimeDeltaSeries()) == -2) {
            cmp = this.getTimeDeltaSeries().compareTo(o.getTimeDeltaSeries());
            if (cmp != 0) {
                return cmp;
            }
        }

        // Compare relations.
        if (comparePossiblyNullObjects(this.getRelations(), o.getRelations()) == -2) {
            cmp = this.getRelations().compareTo(o.getRelations());
            if (cmp != 0) {
                return cmp;
            }
        }
        
        // Compare Daikon invariants.
        if (comparePossiblyNullObjects(this.getDaikonInvariants(),
                o.getDaikonInvariants()) == -2) {
            cmp = this.getDaikonInvariants().compareTo(o.getDaikonInvariants());
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }

    /**
     * Helper method for compareTo(). Returns -2 if both objects are non-null.
     * Otherwise, returns 1, 0, or -1 if respectively just o1 is null, both are
     * null, or just o2 is null.
     */
    private int comparePossiblyNullObjects(Object o1, Object o2) {
        if (o1 != null && o2 != null) {
            return -2;
        }
        if (o1 == null && o2 != null) {
            return 1;
        } else if (o2 == null && o1 != null) {
            return -1;
        } else {
            return 0;
        }
    }
}
