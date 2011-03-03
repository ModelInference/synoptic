package synoptic.invariants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import synoptic.main.Main;
import synoptic.main.TraceParser;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.interfaces.IGraph;

/**
 * Implements a temporal invariant mining algorithm whose running time is linear
 * in the number events in the log, and whose space usage is quadratic in the
 * number of event types and running time also depends on partition sizes. A
 * more detailed complexity break-down is given below. <br/>
 * <br/>
 * This algorithm has lower space usage than the transitive-closure-based
 * algorithms.
 * 
 * @author ivan
 */
public class SpecializedInvariantMiner extends InvariantMiner {

    /**
     * Compute invariants of a graph g by mining invariants directly from the
     * partitions associated with the graph.
     * 
     * @param g
     *            the graph of nodes of type LogEvent
     * @return the set of temporal invariants that g satisfies
     */
    @Override
    public TemporalInvariantSet computeInvariants(IGraph<LogEvent> g) {
        if (g instanceof Graph) {
            return computeInvariants(((Graph<LogEvent>) g).getPartitions(),
                    TraceParser.defaultRelation);
        }
        return new TemporalInvariantSet();
    }

    /**
     * <p>
     * Mines AFby, AP, NFby invariants from a list of partitions -- each of
     * which is a list of LogEvents. It does this by leveraging the following
     * three observations:
     * </p>
     * <p>
     * (1) To check a AFby b it is sufficient to count the number of times a
     * transitive a->b was seen across all partitions and check if it equals the
     * number of a's seen across all partitions. If the two are equal then AFby
     * is true.
     * </p>
     * <p>
     * (2) To check a NFby b it is sufficient to count the number of times a
     * transitive a->b was seen across all partitions and declare a NFby b true
     * iff the count is 0.
     * </p>
     * <p>
     * (3) To check a AP b it is sufficient to count across all partitions the
     * number of times an b instance in a partition was preceded transitively by
     * an a in the same partition, and declare a AP b true iff this count equals
     * the number of b's seen across all partitions.
     * </p>
     * </p>
     * <p>
     * NOTE1: This code only works for events that are totally ordered in each
     * partition -- they have to be sorted beforehand!
     * </p>
     * <p>
     * NOTE2: This code also mines invariants of the form "INITIAL AFby x", i.e.
     * "eventually x" invariants.
     * </p>
     * <p>
     * TODO: Extend this algorithm to partially ordered events -- we would need
     * to consider
     * </p>
     */
    private TemporalInvariantSet computeInvariants(
            LinkedHashMap<String, ArrayList<LogEvent>> partitions,
            String relation) {

        // TODO: we can set the initial capacity of the following HashMaps more
        // optimally, e.g. (N / 0.75) + 1 where N is the total number of event
        // types. See:
        // http://stackoverflow.com/questions/434989/hashmap-intialization-parameters-load-initialcapacity
        LinkedHashMap<String, Integer> globalEventOccurrenceCounts = new LinkedHashMap<String, Integer>();

        LinkedHashMap<String, LinkedHashMap<String, Integer>> globalEventFollowedByEventCounts = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

        LinkedHashMap<String, LinkedHashMap<String, Integer>> globalEventPrecedesEventCounts = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

        LinkedHashSet<String> AlwaysFollowsINITIALSet = null;

        boolean firstPartition = true;
        for (List<LogEvent> partition : partitions.values()) {

            LinkedHashSet<String> inPartitionPrecedesSet = new LinkedHashSet<String>();

            // Forward pass: for each event compute the precedes set, and update
            // its globalEventPrecedesEventCounts map based on this precedes set
            for (LogEvent e : partition) {
                String label = e.getLabel();

                // For each preceding label we update the precedingLabel ->
                // label count in globalEventPrecedesEventCounts
                for (String precedingLabel : inPartitionPrecedesSet) {
                    LinkedHashMap<String, Integer> tmp;
                    if (!globalEventPrecedesEventCounts
                            .containsKey(precedingLabel)) {
                        tmp = new LinkedHashMap<String, Integer>();
                        globalEventPrecedesEventCounts.put(precedingLabel, tmp);
                    } else {
                        tmp = globalEventPrecedesEventCounts
                                .get(precedingLabel);
                    }
                    if (!tmp.containsKey(label)) {
                        tmp.put(label, 1);
                    } else {
                        tmp.put(label, tmp.get(label) + 1);
                    }
                }

                inPartitionPrecedesSet.add(label);

                // In this forward pass also update the global event occurrence
                // count for this event
                if (!globalEventOccurrenceCounts.containsKey(label)) {
                    globalEventOccurrenceCounts.put(label, 1);
                } else {
                    globalEventOccurrenceCounts.put(label,
                            globalEventOccurrenceCounts.get(label) + 1);
                }
            }

            LinkedHashSet<String> inPartitionFollowedBySet = new LinkedHashSet<String>();

            // Reverse pass: for each event compute the followed by set, and
            // update the events' globalEventFollowedByEventCounts map based on
            // this set.
            for (int i = partition.size() - 1; i >= 0; i--) {
                String label = partition.get(i).getLabel();

                // For each followedByLabel we update the label ->
                // followedByLabel label count in
                // globalEventFollwedByEventCounts
                for (String followedByLabel : inPartitionFollowedBySet) {
                    LinkedHashMap<String, Integer> tmp;
                    if (!globalEventFollowedByEventCounts.containsKey(label)) {
                        tmp = new LinkedHashMap<String, Integer>();
                        globalEventFollowedByEventCounts.put(label, tmp);
                    } else {
                        tmp = globalEventFollowedByEventCounts.get(label);
                    }
                    if (!tmp.containsKey(followedByLabel)) {
                        tmp.put(followedByLabel, 1);
                    } else {
                        tmp.put(followedByLabel, tmp.get(followedByLabel) + 1);
                    }
                }
                inPartitionFollowedBySet.add(label);
            }
            // Update the set of events e such that INITIAL AFby e by taking the
            // intersection with the set of all events in this partition.
            logger.fine("inPartitionFollowedBySet: "
                    + inPartitionFollowedBySet.toString());
            if (firstPartition) {
                firstPartition = false;
                AlwaysFollowsINITIALSet = new LinkedHashSet<String>(
                        inPartitionFollowedBySet);
            } else {
                AlwaysFollowsINITIALSet.retainAll(inPartitionFollowedBySet);
            }
        }

        // Now build the invariants list based on the following observations:
        // a AFby b <=> #_F(a->b) == #a
        // a AP b <=> #_P(a->b) == #b
        // a NFby b <=> #_F(a->b) == 0
        // Where:
        // #_F(a->b) means globalEventFollowedByEventCounts[a][b]
        // #_P(a->b) means globalEventPrecedesEventCounts[b][a]
        // #(x) means globalEventOccurrenceCounts[a]

        Set<ITemporalInvariant> invariants = new LinkedHashSet<ITemporalInvariant>();

        for (String label1 : globalEventOccurrenceCounts.keySet()) {
            for (String label2 : globalEventOccurrenceCounts.keySet()) {
                if (!globalEventFollowedByEventCounts.containsKey(label1)) {
                    // label1 appeared only as the last event, therefore
                    // nothing could follow it, therefore label1 NFby label2

                    invariants.add(new NeverFollowedInvariant(label1, label2,
                            relation));

                } else {
                    if (!globalEventFollowedByEventCounts.get(label1)
                            .containsKey(label2)) {
                        // label1 was never followed by label2, therefore label1
                        // NFby label2 (i.e. #_F(label1->label2) == 0)
                        invariants.add(new NeverFollowedInvariant(label1,
                                label2, relation));
                    } else {
                        // label1 was sometimes followed by label2
                        if (globalEventFollowedByEventCounts.get(label1).get(
                                label2) == globalEventOccurrenceCounts
                                .get(label1)) {
                            // #_F(label1->label2) == #label1 therefore label1
                            // AFby label2
                            invariants.add(new AlwaysFollowedInvariant(label1,
                                    label2, relation));
                        }
                    }
                }

                if (!globalEventPrecedesEventCounts.containsKey(label1)) {
                    // label1 only appeared as the first event, therefore
                    // nothing could precede it.
                } else {
                    if (globalEventPrecedesEventCounts.get(label1).containsKey(
                            label2)) {
                        // label1 was sometimes preceded by label2
                        if (globalEventPrecedesEventCounts.get(label1).get(
                                label2) == globalEventOccurrenceCounts
                                .get(label2)) {
                            // #_P(label1->label2) == #label2 therefore label1
                            // AP label2
                            invariants.add(new AlwaysPrecedesInvariant(label1,
                                    label2, relation));
                        }
                    }
                }
            }
        }

        // Record all the INITIAL AFby x invariants to represent
        // "eventually x"
        for (String label : AlwaysFollowsINITIALSet) {
            invariants.add(new AlwaysFollowedInvariant(Main.initialNodeLabel,
                    label, relation));
        }

        return new TemporalInvariantSet(invariants);
    }
}
