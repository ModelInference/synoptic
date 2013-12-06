package dynoptic.model.fifosys.gfsm.observed.dag;

import java.util.Set;

import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.util.Util;

import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;

/**
 * Represents an observed state, or a state that could have been instantaneously
 * observed, based on the DAG of observed events.
 */
public class ObsDAGNode {

    // The local state corresponding to this DAG node. These ObservedFSMState
    // instances are re-used and long-lived, while DAG nodes are used only to
    // construct Traces.
    private ObsFSMState obsState = null;

    // The event that was observed to follow this state _locally_ (i.e., at this
    // process). For terminal states, this is null.
    private Event nextEvent = null;

    // The state that was observed to follow this state locally.
    private ObsDAGNode nextState = null;

    // The state that preceded this state locally.
    private ObsDAGNode prevState = null;

    // The set of states that this node depends on (i.e., if these states have
    // not occurred yet in the trace then this state cannot occur. This set only
    // includes remote dependencies -- states at other processes (since locally,
    // this state trivially depends only on the preceding state).
    private Set<ObsDAGNode> remoteDependencies = Util.newSet();

    // Whether or not this state has occurred in a trace simulation.
    private boolean occurredInSym = false;

    public ObsDAGNode(ObsFSMState obsState) {
        this.obsState = obsState;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Sets the event and the state (local to the same process) that followed
     * this state.
     */
    public void addTransition(Event event, ObsDAGNode nextState_) {
        assert event != null;
        assert nextState_ != null;
        assert this.nextEvent == null;
        assert this.nextState == null;
        assert ((DistEventType) event.getEType()).getPid() == getPid();
        assert nextState_.getPid() == getPid();

        // Note, even if obsState is terminal, we can still add DAGNode
        // transition because since it is merely a container on top of states
        // that might exist from prior traces (and might be terminal in those
        // other traces).

        this.nextEvent = event;
        this.nextState = nextState_;
        nextState_.setPrevState(this);
    }

    /**
     * Sets the state (local to the same process) that occurred immediately
     * before this state (set by addTransition).
     */
    protected void setPrevState(ObsDAGNode prevState) {
        assert !obsState.isInitial();
        assert this.prevState == null;
        assert prevState != null;
        assert prevState.getPid() == getPid();

        this.prevState = prevState;
    }

    /**
     * Records a dependency from a remote state -- a state at some other process
     * that must occur before this state can occur.
     */
    public void addRemoteDependency(ObsDAGNode newDep) {
        assert !remoteDependencies.contains(newDep);
        assert newDep.getPid() != getPid();

        remoteDependencies.add(newDep);
    }

    /** Used during execution simulation to mark this state as having occurred. */
    public void setOccurred(boolean hasOccurred) {
        assert isInitialized();

        occurredInSym = hasOccurred;
    }

    // //////////////////////////////////////////////////////////////////

    public ObsFSMState getObsState() {
        return obsState;
    }

    public int getPid() {
        return obsState.getPid();
    }

    public Event getNextEvent() {
        assert isInitialized();

        return nextEvent;
    }

    public ObsDAGNode getNextState() {
        assert isInitialized();

        return nextState;
    }

    public ObsDAGNode getPrevState() {
        assert isInitialized();

        return prevState;
    }

    public boolean hasOccurred() {
        assert isInitialized();

        return occurredInSym;
    }

    /**
     * Whether or not this state has _not_ occurred and is enabled -- i.e.,
     * whether this state's remote dependencies have been satisfied (occurred),
     * and if the local preceding state has also occurred.
     */
    public boolean isEnabled() {
        assert isInitialized();

        if (hasOccurred()) {
            return false;
        }

        // Previous local state has not occurred -- therefore the local
        // dependency is not satisfied.
        if (prevState != null && !prevState.hasOccurred()) {
            return false;
        }

        for (ObsDAGNode rNode : remoteDependencies) {
            // A remote state that this state depends on has not occurred.
            if (!rNode.hasOccurred()) {
                return false;
            }
        }

        return true;
    }

    public boolean isInitState() {
        return obsState.isInitial();
    }

    public boolean isTermState() {
        return obsState.isTerminal();
    }

    @Override
    public String toString() {
        return "ObsDAGNode[" + obsState.toString() + "]";
    }

    // //////////////////////////////////////////////////////////////////

    // Performs a few consistency checks.
    private boolean isInitialized() {
        if (!isTermState()) {
            // This would indicate that this node is not yet initialized.
            return (nextEvent != null) || (nextState != null);
        }

        if (!isInitState()) {
            // This would indicate that this node is not yet initialized.
            return prevState != null;
        }
        return true;
    }
}
