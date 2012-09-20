package synoptic.invariants.miners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import synoptic.invariants.KTailInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * Mines KTailInvariants from a totally ordered ChainsTraceGraph for values of k
 * from 1 to k.
 * 
 * @author jennyabrahamson
 */
public class KTailInvariantMiner implements ITOInvariantMiner {

    private int k;

    public KTailInvariantMiner(int k) {
        this.k = k;
    }

    /**
     * NOTE: multiple-relations mining of KTail invariants is not supported.
     */
    @Override
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            boolean multipleRelations) {
        assert multipleRelations == false;
        return computeInvariants(g);
    }

    /**
     * Returns the set of all KTail invariants for k from 1 to k for the given
     * trace graph g.
     */
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g) {

        // Set of all kTail invariants already created
        Map<List<EventType>, Set<EventType>> tails = new HashMap<List<EventType>, Set<EventType>>();

        // k equal to 0 handled by immediate invariants
        if (k > 0) {

            // Mine kTails for all values from 1 to k
            for (int i = 1; i <= k; i++) {
                computeInvariants(g, i, tails);
            }
        }

        // Construct a KTailInvariant for each tail in tails
        TemporalInvariantSet invars = new TemporalInvariantSet();
        for (Entry<List<EventType>, Set<EventType>> inv : tails.entrySet()) {
            invars.add(new KTailInvariant(inv.getKey(), inv.getValue()));
        }
        return invars;
    }

    /**
     * Adds all kTail invariants for k = kVal to the set of KTailInvariants
     * invariants.
     */
    private void computeInvariants(ChainsTraceGraph g, int kVal,
            Map<List<EventType>, Set<EventType>> tails) {

        EventNode initNode = g.getDummyInitialNode();

        List<EventType> eventWindow = new ArrayList<EventType>();

        // Iterate through all the traces -- each transition from the
        // INITIAL node connects\holds a single trace.
        for (ITransition<EventNode> initTrans : initNode.getAllTransitions()) {
            EventNode curNode = initTrans.getTarget();

            eventWindow.clear();

            // Start by initializing event window with INITIAL node and
            // the next k nodes (if there are enough events to do so).
            int count = 1;
            eventWindow.add(initNode.getEType());

            while (curNode.getAllTransitions().size() != 0 && count <= kVal) {

                // NOTE: this invariant miner only works for totally
                // ordered traces, so each node must have no more than 1
                // out-going transition.
                if (curNode.getAllTransitions().size() != 1) {
                    throw new InternalSynopticException(
                            "KTailInvariantMiner does not work on partially ordered traces.");
                }
                eventWindow.add(curNode.getEType());
                count++;
                curNode = curNode.getAllTransitions().get(0).getTarget();
            }

            if (count != kVal + 1) {
                // There were not enough events to fill the window
                continue;
            }

            // Explore the rest of this trace, iteratively creating a
            // tail and then sliding down the eventWindow by 1
            while (true) {

                // Update tails map
                if (!tails.containsKey(eventWindow)) {
                    tails.put(new ArrayList<EventType>(eventWindow),
                            new HashSet<EventType>());
                }
                Set<EventType> followingEvents = tails.get(eventWindow);
                followingEvents.add(curNode.getEType());
                if (curNode.getAllTransitions().size() == 0) {
                    break;
                }

                // Update window
                eventWindow.remove(0);
                eventWindow.add(curNode.getEType());
                curNode = curNode.getAllTransitions().get(0).getTarget();
            }
        }
    }
}
