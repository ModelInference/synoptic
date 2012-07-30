package dynoptic.model.fifosys.gfsm;

import java.util.List;
import java.util.Set;

import dynoptic.model.fifosys.FifoSys;
import dynoptic.model.fifosys.channel.ChannelId;

/**
 * <p>
 * A GFSM captures the execution space of a CFSM. We use this model to (1)
 * maintain the observed states/event, and (2) to carry out complex operations
 * like refinement.
 * </p>
 * <p>
 * This model is easier to deal with than a CFSM because it captures all global
 * information in a single place (e.g., all enabled transitions from a single
 * global configuration). A CFSM can be thought of as an abstraction of a GFSM
 * -- a CFSM does not deal with concrete observations. The CFSM model is useful
 * for visualization and for input to the McScM model checker.
 * </p>
 * <p>
 * A GFSM can also be thought of as a representation of the operational
 * semantics, or some number of executions of some hidden/abstract CFSM. Note
 * that although it captures or describes prior executions, it cannot actually
 * be executed or maintain instantaneous execution state -- for this, use
 * FifoSysExecution.
 * </p>
 * <p>
 * A GFSM is composed of GFSMStates, which are actually _partitions_ of the
 * observed global configurations. Refinement causes a re-shuffling of the
 * observations, and new partitions to be created and added to the GFSM.
 * Therefore, a GFSM is highly mutable. Each mutation of the GFSM is a single
 * complete step of the Dynoptic algorithm.
 * </p>
 */
public class GFSM extends FifoSys<GFSMState> {

    // Per-process initial and accept states, ordered by process id. That is,
    // processInits[0] contains the set of all GFSMState instances that contain
    // at least one observation in which process id 0 was in initial state.
    List<Set<GFSMState>> processInits;
    List<Set<GFSMState>> processAccepts;

    // Note that this.initStates and this.acceptStates are global init/accept
    // states. That is, these are states that contain at least one
    // observation per process, for all processes, in which the process is in
    // initial/accept state.
    //
    // For n processes, these sets satisfy the invariants:
    // processInits[0] \intersect processInits[1] \intersect ... \intersect
    // processInits[n-1] = initStates
    //
    // And likewise for acceptStates

    public GFSM(int numProcesses, Set<ChannelId> channelIds) {
        super(numProcesses, channelIds);
    }

    // //////////////////////////////////////////////////////////////////

    public void addGFSMStates(GFSMState s) {
        assert !states.contains(s);

        states.add(s);
        recomputeAlphabet();
    }

    public void removeGFSMStates(GFSMState s) {
        assert states.contains(s);

        states.remove(s);
        recomputeAlphabet();
    }

}
