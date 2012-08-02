package dynoptic.model.fifosys.gfsm.trace;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dynoptic.main.DynopticMain;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.AbsMultiFSMState;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.channel.MultiChannelState;
import dynoptic.model.fifosys.gfsm.GFSMState;

/**
 * <p>
 * Represents a state that was observed or mined from a log from an execution of
 * a FIFO system. Note that because the execution is possible partially ordered
 * (concurrent), there are multiple possible _next_ ObservedFifoSysState
 * instances. That is, the observed state is a cross product of states, and we
 * can have as many states possible as the number of events observed following
 * each of the individual FSM states that comprise this ObservedFifoSysState.
 * </p>
 * <p>
 * Also, note that the channelStates are often implicit -- we do not have a
 * record of the actual channel states, but we can reconstruct the channel
 * states based on the sequence of send/receive operations.
 * </p>
 */
public class ObservedFifoSysState extends
        AbsMultiFSMState<ObservedFifoSysState> {

    // The "partition" that this observed fifo state belongs to.
    private GFSMState parent;

    // A list of observed FSM states, the list is ordered according to process
    // IDs.
    private final List<ObservedFSMState> fsmStates;

    // The observed state of all the channels in the system.
    private final MultiChannelState channelStates;

    // Observed following event types across all FSM states that make up
    // fsmStates.
    // private final Set<EventType> events;

    // Observed transitions for each observed following event type.
    private final Map<ObservedEvent, ObservedFifoSysState> transitions;

    // A count of the number of transitions that still remain to be
    // added/specified (based on the number of following events above).
    // private int unSpecifiedTxns;

    public ObservedFifoSysState(List<ObservedFSMState> fsmStates,
            MultiChannelState channelStates) {
        super(fsmStates.size());

        if (DynopticMain.assertsOn) {
            // Make sure that channelStates only reference pids that are less
            // than fsmStates.size().
            for (ChannelId chId : channelStates.getChannelIds()) {
                assert chId.getSrcPid() >= 0
                        && chId.getSrcPid() < fsmStates.size();
                assert chId.getDstPid() >= 0
                        && chId.getDstPid() < fsmStates.size();
            }
        }

        this.fsmStates = fsmStates;
        this.channelStates = channelStates;
        // this.events = new LinkedHashSet<EventType>();
        this.transitions = new LinkedHashMap<ObservedEvent, ObservedFifoSysState>();

        // for (ObservedFSMState s : fsmStates) {
        // Terminal observed states have no events for any pid.
        // if (!s.isTerminal()) {
        // events.add(s.getNextEvent().getType());
        // }
        // }

        // unSpecifiedTxns = events.size();
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
    public Set<? extends EventType> getTransitioningEvents() {
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
    public boolean isInitForPid(int pid) {
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

    public void addTransition(ObservedEvent e, ObservedFifoSysState s) {
        // assert unSpecifiedTxns > 0;
        // assert this.events.contains(e);
        assert !this.transitions.containsKey(e);

        if (DynopticMain.assertsOn) {
            // Make sure that the following states belongs to the same "system",
            // which is identified by number of processes and the channelIds.
            assert channelStates.getChannelIds().equals(getChannelIds());
            assert s.getNumProcesses() == getNumProcesses();
        }

        this.transitions.put(e, s);
        // unSpecifiedTxns--;
    }

    public GFSMState getParent() {
        return parent;
    }

    public void setParent(GFSMState newParent) {
        parent = newParent;
    }

    public int getNumProcesses() {
        return fsmStates.size();
    }

    public List<ChannelId> getChannelIds() {
        return channelStates.getChannelIds();
    }

    public MultiChannelState getChannelStates() {
        return channelStates;
    }
}
