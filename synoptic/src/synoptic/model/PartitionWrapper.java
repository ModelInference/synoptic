package synoptic.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.model.state.SynDaikonizer;

import daikon.inv.Invariant;

/**
 * A wrapper class of Partition. Use for storing invariants at Synoptic points,
 * i.e., outgoing edges, of this partition.
 * 
 * @author rsukkerd
 *
 */
public class PartitionWrapper {
    private final Partition partition;
    /** Maps each Synoptic point (i.e., outgoing edge of this partition)
     *  to a list of invariants */
    private final Map<Partition, List<Invariant>> invariants;
    
    public PartitionWrapper(Partition partition) {
        this.partition = partition;
        invariants = new HashMap<Partition, List<Invariant>>();
    }
    
    /**
     * Populates invariants at Synoptic points, i.e., outgoing edges,
     * of this partition.
     * 
     * @throws Exception
     */
    public void populateInvariants() throws Exception {
        for (Partition succPartition : partition.getAllSuccessors()) {
            SynDaikonizer daikonizer = new SynDaikonizer();
            
            if (partition.isInitial()) {
                // This is a dummy initial partition and its dummy event node
                // has no post-event state. Instead, we need to get
                // pre-event states of the successor events.
                for (EventNode succEvent : succPartition.getEventNodes()) {
                    daikonizer.addInstance(succEvent.getPreEventState());
                }
                continue;
            }
            
            for (EventNode event : partition.getEventNodes()) {
                Set<EventNode> succEvents = event.getAllSuccessors();
                assert succEvents.size() == 1;
                EventNode succEvent = succEvents.iterator().next();
                
                if (succEvent.getParent().compareTo(succPartition) == 0) {
                    daikonizer.addInstance(event.getPostEventState());
                }
            }
            
            List<Invariant> invs = daikonizer.getDaikonInvariants();
            invariants.put(succPartition, invs);
        }
    }
}
