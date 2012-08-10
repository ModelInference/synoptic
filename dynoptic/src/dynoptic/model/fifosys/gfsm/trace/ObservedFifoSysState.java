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
import dynoptic.model.fifosys.channel.ImmutableMultiChannelState;
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

    private static final Map<ObsMultFSMState, ObservedFifoSysState> fifoSysStatesMap;

    static {
        fifoSysStatesMap = new LinkedHashMap<ObsMultFSMState, ObservedFifoSysState>();
    }

    /**
     * Returns a cached ObservedFifoSysState instance, if one was previously
     * created with the given FSM states. Otherwise, returns a new instance and
     * caches it. When looking up an existing FIFO state, the channelStates
     * argument is only used to check that the returned instance has the
     * expected channel state (channel state is determined by the local states
     * of the processes).
     * 
     * @param nextFsmStates
     * @param nextChannelStates
     * @return
     */
    public static ObservedFifoSysState getFifoSysState(
            ObsMultFSMState fsmStates, ImmutableMultiChannelState channelStates) {
        ObservedFifoSysState ret;
        if (fifoSysStatesMap.containsKey(fsmStates)) {
            ret = fifoSysStatesMap.get(fsmStates);
            assert ret.getChannelStates().equals(channelStates);
        } else {
            ret = new ObservedFifoSysState(fsmStates, channelStates);
            fifoSysStatesMap.put(fsmStates, ret);
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    // The "partition" that this observed fifo state belongs to.
    private GFSMState parent;

    // Observed FSM states.
    // private final List<ObservedFSMState> fsmStates;
    private final ObsMultFSMState fsmStates;

    // The observed state of all the channels in the system.
    private final ImmutableMultiChannelState channelStates;

    // Observed transitions for each observed following event type.
    private final Map<ObservedEvent, ObservedFifoSysState> transitions;

    private ObservedFifoSysState(ObsMultFSMState fsmStates,
            ImmutableMultiChannelState channelStates) {
        super(fsmStates.getNumProcesses());

        if (DynopticMain.assertsOn) {
            // Make sure that channelStates only reference pids that are less
            // than fsmStates.size().
            for (ChannelId chId : channelStates.getChannelIds()) {
                assert chId.getSrcPid() >= 0
                        && chId.getSrcPid() < fsmStates.getNumProcesses();
                assert chId.getDstPid() >= 0
                        && chId.getDstPid() < fsmStates.getNumProcesses();
            }
        }

        this.fsmStates = fsmStates;
        this.channelStates = channelStates;
        this.transitions = new LinkedHashMap<ObservedEvent, ObservedFifoSysState>();
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isAccept() {
        // NOTE: the assumption that we make here is that a terminal state is
        // an instance of the abstract accepting state. This assumption does not
        // hold if we our traces are lossy.
        return fsmStates.isAccept() && channelStates.isEmpty();
    }

    @Override
    public boolean isAcceptForPid(int pid) {
        assert pid >= 0 && pid < fsmStates.getNumProcesses();
        return fsmStates.isAcceptForPid(pid)
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
        return fsmStates.toString() + "; " + channelStates.toString();
    }

    @Override
    public boolean isInitForPid(int pid) {
        return fsmStates.isInitForPid(pid) && channelStates.isEmptyForPid(pid);
    }

    @Override
    public boolean isInitial() {
        return fsmStates.isInitial() && channelStates.isEmpty();
    }

    // //////////////////////////////////////////////////////////////////

    public ObservedFifoSysState getNextState(EventType event) {
        return transitions.get(event);
    }

    public void addTransition(ObservedEvent e, ObservedFifoSysState s) {
        assert !this.transitions.containsKey(e);

        if (DynopticMain.assertsOn) {
            // Make sure that the following states belongs to the same "system",
            // which is identified by number of processes and the channelIds.
            assert channelStates.getChannelIds().equals(getChannelIds());
            assert s.getNumProcesses() == getNumProcesses();
        }

        this.transitions.put(e, s);
    }

    public GFSMState getParent() {
        return parent;
    }

    public void setParent(GFSMState newParent) {
        parent = newParent;
    }

    public int getNumProcesses() {
        return super.getNumProcesses();
    }

    public List<ChannelId> getChannelIds() {
        return channelStates.getChannelIds();
    }

    public ImmutableMultiChannelState getChannelStates() {
        return channelStates;
    }

}
