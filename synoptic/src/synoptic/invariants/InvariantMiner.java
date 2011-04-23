package synoptic.invariants;

import java.util.logging.Logger;

import synoptic.model.EventNode;
import synoptic.model.interfaces.IGraph;
import synoptic.util.InternalSynopticException;

/**
 * Base class for all invariant miners.
 * 
 * @author ivan
 */
public abstract class InvariantMiner {
    protected static Logger logger = Logger.getLogger("TemporalInvSet Logger");

    public TemporalInvariantSet computeInvariants(IGraph<EventNode> g) {
        throw new InternalSynopticException(
                "computeInvariants must be overridden in a derived class.");
    }
}
