package synoptic.invariants.miners;

import java.util.ArrayList;
import java.util.List;

import synoptic.invariants.KTailInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * Mines k tail invariants for values of k from 1 to k for a given
 * ChainsTraceGraph.
 * 
 * @author jennyabrahamson
 */
public class KTailInvariantMiner {

    /**
     * NOTE: multiple-relations mining of KTail invariants is not supported.
     */
    public static TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            boolean multipleRelations, int k) {
        assert multipleRelations == false;
        return computeInvariants(g, k);
    }

    /**
     * Returns the set of all KTail invariants for k from 1 to k for the given
     * trace graph g.
     */
    public static TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            int k) {

        TemporalInvariantSet invars = new TemporalInvariantSet();

        // k equal to 1 handled by immediate invariants
        if (k > 1) {

            // Mine kTails for all values from 1 to k
            for (int i = 2; i <= k; i++) {
                computeInvariants(invars, g, i);
            }
        }
        return invars;
    }

    /*
     * Adds all kTail invariants for k = kVal to the set of KTailInvariants
     * invars.
     */
    private static void computeInvariants(TemporalInvariantSet invars,
            ChainsTraceGraph g, int kVal) {

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
                // Add tail to the set of invariants
                invars.add(KTailInvariant.getInvariant(eventWindow,
                        curNode.getEType()));

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
