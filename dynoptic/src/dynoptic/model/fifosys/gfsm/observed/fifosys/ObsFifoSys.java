package dynoptic.model.fifosys.gfsm.observed.fifosys;

import java.util.List;
import java.util.Set;

import dynoptic.main.DynopticMain;
import dynoptic.model.fifosys.FifoSys;
import dynoptic.model.fifosys.channel.channelid.ChannelId;

/**
 * Represents a single captured/observed trace of an execution of a distributed
 * system. The actual (parsed) event/state information is maintained by
 * ObservedFifoSysState instances. This trace merely maintains a pointer to the
 * initial/terminal states.
 */
public class ObsFifoSys extends FifoSys<ObsFifoSysState> {

    private final ObsFifoSysState initState;
    private final ObsFifoSysState termState;

    public ObsFifoSys(List<ChannelId> channelIds, ObsFifoSysState initState,
            ObsFifoSysState termState, Set<ObsFifoSysState> states) {
        super(initState.getNumProcesses(), channelIds);
        assert initState.isInitial();
        assert termState.isAccept();

        assert states.contains(initState);
        assert states.contains(termState);

        if (DynopticMain.assertsOn) {
            for (ObsFifoSysState s : states) {
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

    public ObsFifoSysState getInitState() {
        return initState;
    }

    public ObsFifoSysState getTermState() {
        return termState;
    }

    public int getNumProcesses() {
        return initState.getNumProcesses();
    }

    public List<ChannelId> getChannelIds() {
        return initState.getChannelIds();
    }

}
