package dynoptic.model.fifosys.gfsm.observed.fifosys;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import dynoptic.main.DynopticMain;
import dynoptic.model.fifosys.AbsMultiFSMState;
import dynoptic.model.fifosys.channel.channelstate.ImmutableMultiChState;
import dynoptic.model.fifosys.gfsm.GFSMState;
import dynoptic.model.fifosys.gfsm.observed.ObsDistEventType;
import dynoptic.model.fifosys.gfsm.observed.ObsMultFSMState;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

/**
 * <p>
 * Represents an instantaneous state that is predicted by partially-ordered
 * event observation from an input log for an execution of a FIFO system. Note
 * that because the execution is possibly concurrent, there are multiple
 * possible _next_ ObservedFifoSysState instances to this state. That is, the
 * observed state is a cross product of states, and we can have as many states
 * possible as the number of events observed following each of the individual
 * FSM states that comprise this ObservedFifoSysState.
 * </p>
 * <p>
 * Queue state (channelStates) are often implicit -- we do not have a record of
 * the actual channel states, but we can reconstruct the channel states based on
 * the sequence of send/receive operations.
 * </p>
 * <p>
 * We use DistEventType as the base transition type instead of ObsDistEventType,
 * as ObsDistEventType instances are mutable.
 * </p>
 */
public class ObsFifoSysState extends
        AbsMultiFSMState<ObsFifoSysState, DistEventType> {
    static Logger logger = Logger.getLogger("ObsFifoSysState");

    // A global cache of previously created ObsFifoSysState instances. This is
    // an optimization to forego duplicating identical fifo states that might be
    // created because of different interleavings of concurrent events.
    private static final Map<ObsMultFSMState, ObsFifoSysState> fifoSysStatesMap;

    // The stateId value to give to the next ObsFifoSysState instance.
    private static int nextFifoSysStateId;

    static {
        fifoSysStatesMap = Util.newMap();
        nextFifoSysStateId = 0;
    }

    // Used by tests and DynopticMain to clear the states cache.
    public static void clearCache() {
        fifoSysStatesMap.clear();
    }

    /**
     * Returns a cached ObservedFifoSysState instance, if one was previously
     * created with the given FSM states. Otherwise, returns a new instance and
     * caches it. When looking up an existing FIFO state, the channelStates
     * argument is used to check that the returned instance has the expected
     * channel state (channel state is determined by the local states of the
     * processes).
     * 
     * @param nextFsmStates
     * @param nextChannelStates
     * @return
     */
    public static ObsFifoSysState getFifoSysState(ObsMultFSMState fsmStates,
            ImmutableMultiChState channelStates) {
        assert fsmStates != null;
        assert channelStates != null;

        // Check if we've already created a fifo sys state with this
        // MultiFSMState.
        if (fifoSysStatesMap.containsKey(fsmStates)) {
            ObsFifoSysState ret = fifoSysStatesMap.get(fsmStates);
            // Check that the returned state has the expected channels state.
            if (!ret.getChannelStates().equals(channelStates)) {
                assert ret.getChannelStates().equals(channelStates);
            }

            return ret;
        }

        ObsFifoSysState ret = new ObsFifoSysState(fsmStates, channelStates,
                nextFifoSysStateId);
        nextFifoSysStateId += 1;
        fifoSysStatesMap.put(fsmStates, ret);
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    // The "partition" that this observed fifo state belongs to.
    private GFSMState parent;

    // Observed FSM states.
    private final ObsMultFSMState fsmStates;

    // The observed state of all the channels in the system.
    private final ImmutableMultiChState channelStates;

    // Abstract transitions, based on the abstract dist event types.
    private final Map<DistEventType, ObsFifoSysState> absTransitions;

    // Concrete/observed transitions for each abstract dist event type.
    private final Map<DistEventType, ObsDistEventType> concTransitions;

    // A unique int identifier for this ObsFifoSysState. Also used by
    // hashCode().
    private final int stateId;

    private ObsFifoSysState(ObsMultFSMState fsmStates,
            ImmutableMultiChState channelStates, int stateId) {
        super(fsmStates.getNumProcesses());
        this.stateId = stateId;

        if (DynopticMain.assertsOn) {
            // Make sure that channelStates only reference pids that are less
            // than fsmStates.size().
            for (ChannelId chId : channelStates.getChannelIds()) {
                assert chId.getSrcPid() >= 0
                        && chId.getSrcPid() < fsmStates.getNumProcesses();
                assert chId.getDstPid() >= 0
                        && chId.getDstPid() < fsmStates.getNumProcesses();
            }

            // Since these are observed states, by definition, if we are in all
            // accepting states, then the queues must be empty.
            if (fsmStates.isAccept()) {
                assert channelStates.isEmpty();
            }
        }

        this.fsmStates = fsmStates;
        this.channelStates = channelStates;
        this.absTransitions = Util.newMap();
        this.concTransitions = Util.newMap();
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
    public Set<DistEventType> getTransitioningEvents() {
        return absTransitions.keySet();
    }

    @Override
    public Set<ObsFifoSysState> getNextStates(DistEventType event) {
        return Collections.singleton(absTransitions.get(event));
    }

    public String toLongString() {
        return "ObsFifoSysState[" + fsmStates.toString() + "; "
                + channelStates.toString() + "]";
    }

    public String toShortIntStr() {
        return String.valueOf(stateId);
    }

    @Override
    public String toString() {
        return toShortIntStr();
    }

    @Override
    public boolean isInitForPid(int pid) {
        return fsmStates.isInitForPid(pid) && channelStates.isEmptyForPid(pid);
    }

    @Override
    public boolean isInitial() {
        return fsmStates.isInitial() && channelStates.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof ObsFifoSysState)) {
            return false;

        }
        ObsFifoSysState s = (ObsFifoSysState) other;

        if (!channelStates.equals(s.getChannelStates())) {
            return false;
        }
        if (!fsmStates.equals(s.getFSMStates())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return stateId;
    }

    // //////////////////////////////////////////////////////////////////

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

    public ImmutableMultiChState getChannelStates() {
        return channelStates;
    }

    public ObsMultFSMState getFSMStates() {
        return fsmStates;
    }

    // //////////////////////////////////////////////////////////////////

    /** Whether or not there is a transition on an abstract event type. */
    public boolean hasTransitionOn(DistEventType event) {
        return absTransitions.containsKey(event);
    }

    /** Looks up the next state on an ABSTRACT dist event type. */
    public ObsFifoSysState getNextState(DistEventType event) {
        return absTransitions.get(event);
    }

    /** Looks up the next state on an OBSERVED dist event type. */
    public ObsFifoSysState getNextState(ObsDistEventType event) {
        return absTransitions.get(event.getDistEType());
    }

    /**
     * Returns the OBSERVED event type correspond to an input ABSTRACT event
     * type. We guarantee that there is just _one_ just OBSERVED event type for
     * an ABSTRACT event type.
     */
    public ObsDistEventType getObsTransitionByEType(DistEventType eType) {
        return concTransitions.get(eType);
    }

    /** Adds a transition on e from this to s. */
    public void addTransition(ObsDistEventType e, ObsFifoSysState s) {
        DistEventType eType = e.getDistEType();
        if (DynopticMain.assertsOn) {
            // The Fifo system is a DAG, and cannot have self-loops.
            // TODO: check that there are no loops in general.
            assert !this.equals(s);

            assert !this.absTransitions.containsKey(eType);
            assert !this.concTransitions.containsKey(eType);

            // Make sure that the following states belongs to the same "system",
            // which is identified by number of processes and the channelIds.
            assert channelStates.getChannelIds().equals(s.getChannelIds());
            assert s.getNumProcesses() == getNumProcesses();

            // TODO: check that transforming this with e results in s -- e.g.,
            // check that if e = c!m then s.c = [m + this.c]
        }

        this.absTransitions.put(eType, s);
        this.concTransitions.put(eType, e);
    }

}
