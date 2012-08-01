package dynoptic.model.fifosys.gfsm.trace;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.AbsMultiFSMState;
import dynoptic.model.fifosys.channel.MultiChannelState;
import dynoptic.model.fifosys.gfsm.GFSMState;

/**
 * Represents a state that was observed or mined from a log from an execution of
 * a FIFO system. Note that because the execution is possible partially ordered
 * (concurrent), there are multiple possible _next_ ObservedFifoSysState
 * instances. That is, the observed state is a cross product of states, and we
 * can have as many states possible as the number of events observed following
 * each of the individual FSM states that comprise this ObservedFifoSysState.
 */
public class ObservedFifoSysState extends
        AbsMultiFSMState<ObservedFifoSysState> {

    // The "partition" that this observed fifo state belongs to.
    GFSMState parent;

    // A list of observed FSM states, the list is ordered according to process
    // IDs.
    final List<ObservedFSMState> fsmStates;

    // The observed state of all the channels in the system.
    final MultiChannelState channelStates;

    // Observed following event types.
    final Set<EventType> events;

    // Observed transitions for each observed following event type.
    final Map<EventType, ObservedFifoSysState> transitions;

    // A count of the number of transitions that still remain to be
    // added/specified (based on the number of following events).
    int unSpecifiedTxns;

    public ObservedFifoSysState(List<ObservedFSMState> fsmStates,
            MultiChannelState channelStates) {
        super(fsmStates.size());
        this.fsmStates = fsmStates;
        this.channelStates = channelStates;

        // TODO: check that channelStates only reference pids that are less than
        // fsmStates.size().

        this.events = new LinkedHashSet<EventType>();
        this.transitions = new LinkedHashMap<EventType, ObservedFifoSysState>();

        for (ObservedFSMState s : fsmStates) {
            // Terminal observed states have no events for any pid.
            if (!s.isTerminal()) {
                events.add(s.getNextEvent().getType());
            }
        }

        unSpecifiedTxns = events.size();
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isAccept() {
        // NOTE: the assumption that we make here is that a terminal state is
        // an instance of the abstract accepting state. This assumption does not
        // hold if we our traces are lossy.
        for (ObservedFSMState s : fsmStates) {
            if (!s.isTerminal()) {
                return false;
            }
        }
        return channelStates.isEmpty();
    }

    @Override
    public boolean isAcceptForPid(int pid) {
        assert pid >= 0 && pid < fsmStates.size();
        return fsmStates.get(pid).isTerminal()
                && channelStates.isEmptyForPid(pid);
    }

    @Override
    public Set<EventType> getTransitioningEvents() {
        return transitions.keySet();
    }

    @Override
    public Set<ObservedFifoSysState> getNextStates(EventType event) {
        return Collections.singleton(transitions.get(event));
    }

    @Override
    public String toString() {
        String ret = "[";
        for (ObservedFSMState s : fsmStates) {
            ret = ret + "," + s.toString();
        }
        ret = ret + "]; " + channelStates.toString();
        return ret;
    }

    @Override
    public boolean isInitialForPid(int pid) {
        return fsmStates.get(pid).isInitial()
                && channelStates.isEmptyForPid(pid);
    }

    @Override
    public boolean isInitial() {
        for (ObservedFSMState s : fsmStates) {
            if (!s.isInitial()) {
                return false;
            }
        }
        return channelStates.isEmpty();
    }

    // //////////////////////////////////////////////////////////////////

    public ObservedFifoSysState getNextState(EventType event) {
        return transitions.get(event);
    }

    public void addTransition(EventType e, ObservedFifoSysState s) {
        assert unSpecifiedTxns > 0;
        assert this.events.contains(e);
        assert !this.transitions.containsKey(e);

        this.transitions.put(e, s);
        unSpecifiedTxns--;
    }

    public GFSMState getParent() {
        return parent;
    }

    public void setParent(GFSMState newParent) {
        parent = newParent;
    }
}
