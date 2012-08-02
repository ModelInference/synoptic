package dynoptic.model.fifosys.cfsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dynoptic.model.AbsFSM;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.fifosys.FifoSys;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.model.fifosys.channel.ChannelId;
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

    // Fn: (FSM f) -> initial states of f.
    static private IStateToStateSetFn<FSMState> fnGetInitialStates = new IStateToStateSetFn<FSMState>() {
        @Override
        public Set<FSMState> eval(AbsFSM<FSMState> f) {
            return f.getInitStates();
        }
    };

    // Fn: (FSM f) -> accept states of f.
    static private IStateToStateSetFn<FSMState> fnGetAcceptStates = new IStateToStateSetFn<FSMState>() {
        @Override
        public Set<FSMState> eval(AbsFSM<FSMState> f) {
            return f.getAcceptStates();
        }
    };

    // FSMs participating in this CFSM, ordered according to process ID.
    final List<FSM> fsms;

    // A count of the number of processes/FSMs that still remain to be
    // added/specified.
    int unSpecifiedPids;

    // //////////////////////////////////////////////////////////////////

    public CFSM(int numProcesses, List<ChannelId> channelIds) {
        super(numProcesses, channelIds);
        fsms = new ArrayList<FSM>(Collections.nCopies(numProcesses, (FSM) null));
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
        return deriveAllPermsOfStates(fnGetInitialStates);
    }

    @Override
    public Set<CFSMState> getAcceptStates() {
        return deriveAllPermsOfStates(fnGetAcceptStates);
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

        // Check that the FSM alphabet conforms to the expected number of
        // processes.
        for (EventType e : fsm.getAlphabet()) {
            if (e.isCommEvent()) {
                pid = e.getChannelId().getDstPid();
                assert pid >= 0 && pid < numProcesses;
                pid = e.getChannelId().getSrcPid();
                assert pid >= 0 && pid < numProcesses;
            } else {
                pid = e.getEventPid();
                assert pid >= 0 && pid < numProcesses;
            }
        }

        alphabet.addAll(fsm.getAlphabet());

        unSpecifiedPids -= 1;
    }

    /** Generate SCM representation of this CFSM (without bad_states). */
    public String toScmString() {
        assert unSpecifiedPids == 0;

        String ret;

        // Parameters to the SCM representation of this CFSM.
        String cfsmName = "blah";
        boolean lossy = false;

        ret = "scm " + cfsmName + ":\n\n";

        // Channels:
        ret += "nb_channels = " + numChannels + " ;\n";
        ret += "/*\n";
        for (int i = 0; i < numChannels; i++) {
            ret += "channel " + Integer.toString(i) + " : "
                    + channelIds.get(i).toString() + "\n";
        }
        ret += "*/\n\n";

        // Parameters/Alphabet:
        ret += "parameters :\n";
        ret += alphabet.toScmString();
        ret += "\n";

        // Whether or not the channels are lossy:
        if (lossy) {
            ret += "lossy: 1\n\n";
        } else {
            ret += "lossy: 0\n\n";
        }

        // FSMS:
        for (int pid = 0; pid < numProcesses; pid++) {
            FSM f = fsms.get(pid);
            ret += "automaton p" + Integer.toString(pid) + " :\n";
            ret += f.toScmString();
            ret += "\n";
        }

        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    private Set<CFSMState> deriveAllPermsOfStates(
            IStateToStateSetFn<FSMState> fn) {
        assert unSpecifiedPids == 0;

        if (numProcesses == 1) {
            Set<CFSMState> ret = new LinkedHashSet<CFSMState>();
            for (FSMState i : fn.eval(fsms.get(0))) {
                ret.add(new CFSMState(i));
            }
            return ret;
        }

        assert numProcesses > 1;

        List<List<FSMState>> perms = Util.get2DPermutations(
                fn.eval(fsms.get(0)), fn.eval(fsms.get(1)));

        int i = 2;
        while (i != numProcesses) {
            // Modifies perms in place.
            Util.get2DPermutations(perms, fn.eval(fsms.get(i)));
            i += 1;
        }

        return CFSMState.CFSMStatesFromFSMListLists(perms);
    }

}
