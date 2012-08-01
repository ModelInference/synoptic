package dynoptic.model.fifosys.gfsm.trace;

import java.util.List;

import dynoptic.model.fifosys.channel.ChannelId;

/**
 * Represents a single captured/observed trace of an execution of a distributed
 * system. Most state is maintained by ObservedFifoSysState instances. This
 * trace merely maintains a pointer to the initial/terminal states.
 */
public class Trace {
    private final ObservedFifoSysState initState;

    private final ObservedFifoSysState termState;

    public Trace(ObservedFifoSysState initState, ObservedFifoSysState termState) {
        assert initState.isInitial();
        assert termState.isAccept();

        this.initState = initState;
        this.termState = termState;
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
