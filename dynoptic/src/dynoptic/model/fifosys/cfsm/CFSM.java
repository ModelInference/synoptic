package dynoptic.model.fifosys.cfsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.fifosys.FifoSys;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.gfsm.GFSM;
import dynoptic.model.fifosys.gfsm.GFSMState;
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
     * Constructs a CFSM from a GFSM. It performs the necessary traversal of the
     * GFSM to construct/specify all the process FSMs that should be part of the
     * CFSM.
     * 
     * @param gfsm
     * @return
     */
    public static CFSM buildFromGFSM(GFSM gfsm) {
        Map<GFSMState, FSMState> stateMap = new LinkedHashMap<GFSMState, FSMState>();
        Set<FSMState> initStates = new LinkedHashSet<FSMState>();
        Set<FSMState> acceptStates = new LinkedHashSet<FSMState>();
        Set<GFSMState> visited = new LinkedHashSet<GFSMState>();

        // This is the CFSM that we will return, once we populate it with all
        // the process FSMs.
        CFSM c = new CFSM(gfsm.getNumProcesses(), gfsm.getChannelIds());

        // Create an FSM per pid.
        for (int pid = 0; pid < gfsm.getNumProcesses(); pid++) {

            // Generate the FSM states and inter-state transitions.
            for (GFSMState gInit : gfsm.getInitialStatesForPid(pid)) {
                FSMState fInit;
                if (stateMap.containsKey(gInit)) {
                    fInit = stateMap.get(gInit);
                } else {
                    fInit = new FSMState(gInit.isAcceptForPid(pid), true, pid);
                    stateMap.put(gInit, fInit);
                }
                // We might have visited the current gInit in a prior iteration,
                // from another gInit, in which case we don't need to
                // re-explore.
                if (!visited.contains(gInit)) {
                    visit(stateMap, gInit, fInit, visited, pid);
                }
            }

            // Determine the initial/accept FSM states for FSM construction
            // below.
            for (FSMState s : stateMap.values()) {
                if (s.isInitial()) {
                    initStates.add(s);
                }
                if (s.isAccept()) {
                    acceptStates.add(s);
                }
            }

            // Create the FSM for this pid, and add it to the CFSM.
            FSM f = new FSM(pid, initStates, acceptStates, stateMap.values());
            c.addFSM(f);

            stateMap.clear();
            initStates.clear();
            acceptStates.clear();
            visited.clear();
        }
        return c;
    }

    // //////////////////////////////////////////////////////////////////

    public CFSM(int numProcesses, Set<ChannelId> channelIds) {
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

        return CFSMState.CFSMStatesFromFSMListLists(inits);
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
                .getAcceptStates(), fsms.get(1).getAcceptStates());

        int i = 2;
        while (i != numProcesses) {
            // Modifies accepts in place.
            Util.get2DPermutations(accepts, fsms.get(i).getAcceptStates());
            i += 1;
        }

        return CFSMState.CFSMStatesFromFSMListLists(accepts);
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

        // Build a map from [0...numChannels-1] to channelIds as a List.
        List<ChannelId> orderedCids = new ArrayList<ChannelId>();
        Map<ChannelId, Integer> cIdsToInt = new LinkedHashMap<ChannelId, Integer>();
        int i = 0;
        for (ChannelId c : channelIds) {
            orderedCids.add(c);
            cIdsToInt.put(c, i);
            i++;
        }

        ret = "scm " + cfsmName + ":\n\n";

        // Channels:
        ret += "nb_channels = " + numChannels + " ;\n";
        ret += "/*\n";
        for (i = 0; i < numChannels; i++) {
            ret += "channel " + Integer.toString(i) + " : "
                    + orderedCids.get(i).toString() + "\n";
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
            ret += f.toScmString(cIdsToInt);
            ret += "\n";
        }

        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Depth-first recursive traversal of the GFSM state/transition graph. We
     * back-out when we reach a node that we've visited before. As we traverse,
     * we build up the FSMState states for the specific pid, which are only
     * dependent on event types that are relevant to this pid.
     * 
     * @param stateMap
     * @param gParent
     * @param fParent
     * @param visited
     * @param pid
     */
    private static void visit(Map<GFSMState, FSMState> stateMap,
            GFSMState gParent, FSMState fParent, Set<GFSMState> visited, int pid) {
        visited.add(gParent);

        // Recurse on each (e,gNext) transition from this parent.
        for (EventType e : gParent.getTransitioningEvents()) {
            for (GFSMState gNext : gParent.getNextStates(e)) {

                // In the FSM we only include transitions, and optionally create
                // new FSMStates, for events that match the pid.
                if (e.getEventPid() == pid) {
                    FSMState fNext;
                    // Look-up and optionally create the next FSMState
                    // corresponding to gNext.
                    if (stateMap.containsKey(gNext)) {
                        fNext = stateMap.get(gNext);
                    } else {
                        fNext = new FSMState(gNext.isAcceptForPid(pid),
                                gNext.isInitialForPid(pid), pid);
                        stateMap.put(gNext, fNext);
                    }
                    // Add the transition in the FSM-space.
                    fParent.addTransition(e, fNext);

                    // Recurse with next as parents and updated visited set.
                    if (!visited.contains(gNext)) {
                        visit(stateMap, gNext, fNext, visited, pid);
                    }

                } else {
                    // Because the event e does not impact this pid, we recurse
                    // with gNext as parent, but with the _old_ fParent
                    // FSMState. That is, the pid did not transition in the FSM
                    // state space, even though we did transition the GFSM state
                    // space.
                    if (!visited.contains(gNext)) {
                        visit(stateMap, gNext, fParent, visited, pid);
                    }
                }
            }
        }
    }
}
