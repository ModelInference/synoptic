package dynoptic.model.fifosys.cfsm;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.fifosys.FifoSys;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.gfsm.GFSM;
import dynoptic.util.Util;

/**
 * <p>
 * Represents a CFSM that consists of some number of FSM processes, which
 * communicate through some number of channels with each other. Channels are
 * uni-directional, involve just two end-points, and do not have to exist
 * between every two processes.
 * </p>
 * <p>
 * The number of processes, channels, and the channels configurations are fixed
 * when a CFSM is constructed. Once constructed, to specify/add an FSM instance
 * for a process id (pid), use the addFSM() method. Once all the FSMs have been
 * specified the CFSM is considered initialized. Before the FSM is initialized,
 * none of the other public methods will work.
 * </p>
 * <p>
 * Note that a CFSM is merely a representation of a machine, and does not
 * maintain execution instance state. FifoSysExecution does this.
 * </p>
 */
public class CFSM extends FifoSys<CFSMState> {

    // FSMs participating in this CFSM, ordered according to process ID.
    final List<FSM> fsms;

    // A count of the number of processes/FSMs that still remain to be
    // added/specified.
    int unSpecifiedPids;

    /**
     * This method constructs an initialized CFSM from a GFSM. It performs the
     * necessary traversal of the GFSM to construct/specify all the FSMs that
     * should be part of the CFSM.
     * 
     * @param gfsm
     * @return
     */
    public static CFSM buildFromGFSM(GFSM gfsm) {

        // TODO:
        // 1. For each FSM that is participating in the FIFO system, do:
        //
        // 1.1 Traverse the GFSM starting from its initial state, recording
        // states that are relevant to the FSM (i.e., the transitions are on
        // events that are either local to the FSM or the FSM participates in
        // the comm event).
        //
        // 1.2 The traversal must be transitive -- we must cover all possible
        // paths that are relevant to this FSM.
        //
        // 1.3 The states that contain any concrete state instances where this
        // FSM was in in terminal state should be marked as accepting (for this
        // FSM).

        return null;
    }

    // //////////////////////////////////////////////////////////////////

    public CFSM(int numProcesses, Set<ChannelId> channelIds) {
        super(numProcesses, channelIds);
        fsms = new ArrayList<FSM>(numProcesses);
        unSpecifiedPids = numProcesses;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public FSMAlphabet getAlphabet() {
        assert unSpecifiedPids == 0;

        // Return the union of the alphabets of all of the FSMs.
        return super.getAlphabet();
    }

    @Override
    public Set<CFSMState> getInitStates() {
        assert unSpecifiedPids == 0;

        if (numProcesses == 1) {
            Set<CFSMState> ret = new LinkedHashSet<CFSMState>();
            for (FSMState i : fsms.get(0).getInitStates()) {
                ret.add(new CFSMState(i));
            }
            return ret;
        }

        assert numProcesses > 1;

        List<List<FSMState>> inits = Util.get2DPermutations(fsms.get(0)
                .getInitStates(), fsms.get(1).getInitStates());

        int i = 2;
        while (i != numProcesses) {
            // Modifies inits in place.
            Util.get2DPermutations(inits, fsms.get(i).getInitStates());
            i += 1;
        }

        return createCFSMStatesFromFSMLists(inits);
    }

    // TODO: getAcceptStates and getInitStates are very similar. Find a way to
    // refactor these two methods.
    @Override
    public Set<CFSMState> getAcceptStates() {
        assert unSpecifiedPids == 0;

        if (numProcesses == 1) {
            Set<CFSMState> ret = new LinkedHashSet<CFSMState>();
            for (FSMState i : fsms.get(0).getAcceptStates()) {
                ret.add(new CFSMState(i));
            }
            return ret;
        }

        assert numProcesses > 1;

        List<List<FSMState>> accepts = Util.get2DPermutations(fsms.get(0)
                .getInitStates(), fsms.get(1).getAcceptStates());

        int i = 2;
        while (i != numProcesses) {
            // Modifies accepts in place.
            Util.get2DPermutations(accepts, fsms.get(i).getAcceptStates());
            i += 1;
        }

        return createCFSMStatesFromFSMLists(accepts);
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Adds a new FSM instance to the CFSM. Once all the FSMs have been added,
     * the CFSM is considered initialized.
     * 
     * @param fsm
     */
    public void addFSM(FSM fsm) {
        assert unSpecifiedPids > 0;
        assert fsm != null;
        int pid = fsm.getPid();

        // Must be a valid pid (in the right range).
        assert (pid >= 0 && pid < numProcesses);
        // Only allow to set the FSM for a pid once.
        assert (fsms.get(pid) == null);

        fsms.set(pid, fsm);
        alphabet.addAll(fsm.getAlphabet());

        unSpecifiedPids -= 1;
    }

    // //////////////////////////////////////////////////////////////////

    private Set<CFSMState> createCFSMStatesFromFSMLists(
            List<List<FSMState>> list) {
        Set<CFSMState> ret = new LinkedHashSet<CFSMState>();
        // Create a CFSMState instance from each init instance.
        for (List<FSMState> item : list) {
            ret.add(new CFSMState(item));
        }
        return ret;
    }
}
