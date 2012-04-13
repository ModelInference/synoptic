package synoptic.invariants.miners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import synoptic.invariants.KTailInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.Event;
import synoptic.model.EventNode;
import synoptic.model.EventType;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * Mines tails occurring at least twice of some length k for a given
 * ChainsTraceGraph.
 * 
 * @author jennyabrahamson
 */
public class KTailInvariantMiner implements TOInvariantMiner {

    // Length of tails to mine
    private int k;

    public KTailInvariantMiner(int k) {
        this.k = k;
    }

    @Override
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g) {
        // Logger logger = Logger.getLogger("KTailInvariantMiner");

        TemporalInvariantSet invars = new TemporalInvariantSet();

        // k equal to 0 or 1 handled by immediate invariants
        if (k > 1) {

            Set<KTailInvariant> tails = new HashSet<KTailInvariant>();
            EventNode initNode = g
                    .getDummyInitialNode(Event.defaultTimeRelationString);

            List<EventType> eventWindow = new ArrayList<EventType>();

            // Iterate through all the traces -- each transition from the
            // INITIAL node connects\holds a single trace.
            for (ITransition<EventNode> initTrans : initNode.getTransitions()) {
                EventNode curNode = initTrans.getTarget();

                eventWindow.clear();

                // Start by initializing event window with INITIAL node and the
                // next k nodes (if there are enough events to do so).
                int count = 1;
                eventWindow.add(initNode.getEType());

                while (curNode.getTransitions().size() != 0 && count <= k) {

                    // NOTE: this invariant miner only works for totally ordered
                    // traces, so each node must have no more than 1 out-going
                    // transition.
                    if (curNode.getTransitions().size() != 1) {
                        throw new InternalSynopticException(
                                "KTailInvariantMiner does not work on partially ordered traces.");
                    }
                    eventWindow.add(curNode.getEType());
                    count++;
                    curNode = curNode.getTransitions().get(0).getTarget();
                }

                if (count != k + 1) {
                    // There were not enough events to fill the window
                    continue;
                }

                // logger.info(curNode.toString());

                // Explore the rest of this trace, iteratively creating a tail
                // and then sliding down the eventWindow by 1
                while (true) {
                    // Add tail to the tails set
                    tails.add(KTailInvariant.getInvariant(eventWindow,
                            curNode.getEType()));

                    if (curNode.getTransitions().size() == 0) {
                        break;
                    }

                    // Update window
                    eventWindow.remove(0);
                    eventWindow.add(curNode.getEType());
                    curNode = curNode.getTransitions().get(0).getTarget();
                    // logger.info(curNode.toString());
                }
                // logger.info("--");
            }

            // Add tails to the set of mined invariants.
            for (KTailInvariant tail : tails) {
                invars.add(tail);
            }
        }
        return invars;
    }
}
