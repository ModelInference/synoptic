package dynoptic.model.fifosys.gfsm.trace;

import java.util.List;
import java.util.Set;

import dynoptic.main.DynopticMain;
import dynoptic.model.fifosys.FifoSys;
import dynoptic.model.fifosys.channel.ChannelId;

/**
 * Represents a single captured/observed trace of an execution of a distributed
 * system. The actual (parsed) event/state information is maintained by
 * ObservedFifoSysState instances. This trace merely maintains a pointer to the
 * initial/terminal states.
 */
public class TraceFSM extends FifoSys<ObservedFifoSysState> {

    private final ObservedFifoSysState initState;
    private final ObservedFifoSysState termState;

    public TraceFSM(List<ChannelId> channelIds, ObservedFifoSysState initState,
            ObservedFifoSysState termState, Set<ObservedFifoSysState> states) {
        super(initState.getNumProcesses(), channelIds);
        assert initState.isInitial();
        assert termState.isAccept();

        assert states.contains(initState);
        assert states.contains(termState);

        if (DynopticMain.assertsOn) {
            for (ObservedFifoSysState s : states) {
                assert s.getNumProcesses() == this.numProcesses;
                assert s.getChannelIds().equals(channelIds);
                // There can only be one initial and one accept state in a
                // trace.
                if (s.isAccept()) {
                    assert termState == s;
                }
                if (s.isInitial()) {
                    assert initState == s;
                }
            }
        }

        this.initState = initState;
        this.termState = termState;
        this.states.addAll(states);
    }

    // //////////////////////////////////////////////////////////////////

    public ObservedFifoSysState getInitState() {
        return initState;
    }

    public ObservedFifoSysState getTermState() {
        return termState;
    }

    public int getNumProcesses() {
        return initState.getNumProcesses();
    }

    public List<ChannelId> getChannelIds() {
        return initState.getChannelIds();
    }

}
