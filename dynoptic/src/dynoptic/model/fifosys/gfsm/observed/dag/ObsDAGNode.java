package dynoptic.model.fifosys.gfsm.observed.dag;

import java.util.Set;

import dynoptic.model.fifosys.gfsm.observed.ObsEvent;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;

/**
 * Represents an observed state, or a state that could have been instantaneously
 * observed, based on the DAG of observed events.
 */
public class ObsDAGNode {

    // The local state corresponding to this DAG node. These ObservedFSMState
    // instances are re-used and long-lived, while DAG states are used only to
    // construct Traces.
    private ObsFSMState obsState = null;

    // The event that was observed to follow this state _locally_ (i.e., at this
    // process). For terminal states, this is null.
    private ObsEvent nextEvent = null;

    // The state that was observed to follow this state locally.
    private ObsDAGNode nextState = null;

    // The state that preceded this state locally.
    private ObsDAGNode prevState = null;

    // The set of states that this node depends on (i.e., if these states have
    // not occurred yet in the trace then this state cannot occur. This set only
    // includes remote dependencies -- states at other processes (since locally,
    // this state trivially depends only on the preceding state).
    private Set<ObsDAGNode> remoteDependencies;

    // The states for which _this_ state is a dependency (i.e., this state
    // appears in these states' remoteDependencies set).
    private Set<ObsDAGNode> remoteEnabledStates;

    // Whether or not this state has occurred in a trace simulation.
    private boolean occurredInSym = false;

    public ObsDAGNode(ObsFSMState state) {
        this.obsState = state;
    }

    /** Use this method to set the event and state that followed this state. */
    public void addTransition(ObsEvent event, ObsDAGNode followState) {
        assert event != null;
        assert followState != null;
        assert event.getEventPid() == getPid();
        assert followState.getPid() == getPid();

        this.nextEvent = event;
        this.nextState = followState;
        followState.setPrevState(this);
    }

    public ObsFSMState getObsState() {
        return obsState;
    }

    public int getPid() {
        return obsState.getPid();
    }

    public ObsEvent getNextEvent() {
        return nextEvent;
    }

    public ObsDAGNode getNextState() {
        return nextState;
    }

    public ObsDAGNode getPrevState() {
        return prevState;
    }

    public void setPrevState(ObsDAGNode prevState) {
        assert prevState != null;
        assert prevState.getPid() == getPid();
        this.prevState = prevState;
    }

    public void addDependency(ObsDAGNode newDep) {
        assert !remoteDependencies.contains(newDep);
        remoteDependencies.add(newDep);
    }

    public Set<ObsDAGNode> getRemoteEnabledStates() {
        return remoteEnabledStates;
    }

    public boolean hasOccurred() {
        return occurredInSym;
    }

    public void setOccurred(boolean newOccurred) {
        occurredInSym = newOccurred;
    }

    /**
     * Whether or not this state has _not_ occurred and is enabled -- i.e.,
     * whether this state's remote dependencies have been satisfied (occurred),
     * and if the local preceding state has also occurred.
     */
    public boolean isEnabled() {
        if (hasOccurred()) {
            return false;
        }

        // Previous local state has not occurred -- therefore the local
        // dependency is not satisfied.
        if (prevState != null && prevState.hasOccurred()) {
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
        return prevState == null;
    }

    public boolean isTermState() {
        return nextState == null;
    }
}
