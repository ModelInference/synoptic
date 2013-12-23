package dynoptic.model.fifosys.cfsm;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.AlwaysPrecedes;
import dynoptic.invariants.BinaryInvariant;
import dynoptic.invariants.EventuallyHappens;
import dynoptic.invariants.NeverFollowedBy;
import dynoptic.main.DynopticMain;
import dynoptic.model.AbsFSM;
import dynoptic.model.AbsFSMState;
import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.fifosys.FifoSys;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.model.fifosys.channel.channelid.InvChannelId;
import dynoptic.model.fifosys.channel.channelid.LocalEventsChannelId;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

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
public class CFSM extends FifoSys<CFSMState, DistEventType> {

    // FSM f -> states returned by eval(f)
    protected interface IFSMToStateSetFn<T extends AbsFSMState<T, ?>> {
        Set<T> eval(AbsFSM<T, ?> s);
    }

    // Fn: (FSM f) -> initial states of f.
    static private IFSMToStateSetFn<FSMState> fnGetInitialStates = new IFSMToStateSetFn<FSMState>() {
        @Override
        public Set<FSMState> eval(AbsFSM<FSMState, ?> f) {
            return f.getInitStates();
        }
    };

    // Fn: (FSM f) -> accept states of f.
    static private IFSMToStateSetFn<FSMState> fnGetAcceptStates = new IFSMToStateSetFn<FSMState>() {
        @Override
        public Set<FSMState> eval(AbsFSM<FSMState, ?> f) {
            return f.getAcceptStates();
        }
    };

    // FSMs participating in this CFSM, ordered according to process ID.
    private final List<FSM> fsms;

    // A count of the number of processes/FSMs that still remain to be
    // added/specified.
    private int unSpecifiedPids;

    // Maintains the index in the channelIds list of the first synthetic
    // channelId. All channelIds at or above this index are synthetic.
    // Synthetic channels are used for invariant checking, and are
    // not part of the true model.
    private int firstSyntheticChIndex;

    // The set of invariants that are encoded in this CFSM. Each invariant
    // at index i in invs corresponds to the synthetic channel at index
    // (firstSyntheticChIndex + i) in channelIds list.
    private final List<BinaryInvariant> invs;

    // Index of the local events channel in the channelIds list. Note, that this
    // channel only exists once the CFSM has been converted to an SCM string
    // using toScmString.
    private int localEventsChIndex;

    // //////////////////////////////////////////////////////////////////

    public CFSM(int numProcesses, List<ChannelId> channelIds) {
        super(numProcesses, channelIds);
        fsms = Util.newList(Collections.nCopies(numProcesses, (FSM) null));
        unSpecifiedPids = numProcesses;
        firstSyntheticChIndex = Integer.MAX_VALUE;
        localEventsChIndex = Integer.MAX_VALUE;
        invs = Util.newList();
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public FSMAlphabet<DistEventType> getAlphabet() {
        assert unSpecifiedPids == 0;

        // Return the union of the alphabets of all of the FSMs.
        return super.getAlphabet();
    }

    @Override
    public Set<CFSMState> getInitStates() {
        assert unSpecifiedPids == 0;

        return deriveAllPermsOfStates(fnGetInitialStates);
    }

    @Override
    public Set<CFSMState> getAcceptStates() {
        assert unSpecifiedPids == 0;

        return deriveAllPermsOfStates(fnGetAcceptStates);
    }

    public List<FSM> getFSMs() {
        return fsms;
    }

    @Override
    public String toString() {
        String ret = "CFSM: \n";
        for (FSM f : fsms) {
            ret += "\t" + f.toString() + "\n\n";
        }
        return ret.substring(0, ret.length() - 1);
    }

    @Override
    public int hashCode() {
        int ret = 31;
        ret = ret * 31 + fsms.hashCode();
        return ret;
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }
        if (!(other instanceof CFSM)) {
            return false;
        }
        CFSM cOther = (CFSM) other;

        return fsms.equals(cOther.fsms);
    }

    // //////////////////////////////////////////////////////////////////

    /** Returns the bad states for all invariants that augment this CFSM. */
    public List<BadState> getBadStates() {
        assert !invs.isEmpty();

        // TODO: Sub-optimality -- we are needlessly creating many lists by
        // calling getBadState(inv) repeatedly.
        List<BadState> ret = Util.newList();
        for (BinaryInvariant inv : invs) {
            ret.addAll(getBadStates(inv));
        }
        return ret;
    }

    /**
     * Returns a set of bad states that correspond to a specific invariant that
     * is augmenting this CFSM. Each invariant corresponds to (possibly
     * multiple) bad states. A bad states is a combination of FSM states and a
     * sequence of regular expressions that describe the contents of each of the
     * queues in the CFSM. For an invariant I, a bad state B has the property
     * that if B is reachable in the CFSM then I is falsified. That is, the path
     * to reach B is the counter-example for I.
     */
    public List<BadState> getBadStates(BinaryInvariant inv) {
        assert invs.contains(inv);

        // Without invariants there are no bad states.
        if (invs.isEmpty()) {
            return Collections.emptyList();
        }
        List<BadState> badStates = Util.newList();

        Set<CFSMState> accepts = this.getAcceptStates();
        if (accepts.isEmpty()) {
            assert !accepts.isEmpty();
        }

        List<String> qReList = Util.newList(channelIds.size());

        // Set non-synthetic queues reg-exps to accept the empty string.
        for (int i = 0; i < firstSyntheticChIndex; i++) {
            qReList.add("_");
        }

        int invIndex = invs.indexOf(inv);

        // Set the synthetic queue reg-exps.
        for (int i = firstSyntheticChIndex; i < channelIds.size(); i++) {
            if (i == firstSyntheticChIndex + invIndex) {

                // The invariant we care about checking.
                qReList.add(inv.scmBadStateQRe());

            } else if (i == localEventsChIndex) {

                // Add an RE for the local events queue.
                Set<String> localEvents = this.alphabet
                        .getLocalEventScmStrings();
                if (!localEvents.isEmpty()) {
                    String localEventsQueueRe = "(";
                    for (String eLocal : localEvents) {
                        localEventsQueueRe += eLocal + " | ";
                    }
                    // Remove the last occurrence of the "|" character.
                    localEventsQueueRe = localEventsQueueRe.substring(0,
                            localEventsQueueRe.length() - 3);
                    localEventsQueueRe += ")^*";
                    qReList.add(localEventsQueueRe);
                } else {
                    // If there are no local events then the queue RE is the
                    // empty string, since no corresponding local event messages
                    // will be generated.
                    qReList.add("_");
                }

            } else {
                // Initialize non-inv invariant synthetic queues to accept
                // everything that their alphabet permits.
                qReList.add(inv.someSynthEventsQRe());
            }
        }

        // For each accept, generate a bad state <accept, qReList>.
        for (CFSMState accept : accepts) {
            badStates.add(new BadState(accept, qReList));
        }

        return badStates;
    }

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

        if (DynopticMain.assertsOn) {
            // Must be a valid pid (in the right range).
            assert (pid >= 0 && pid < numProcesses);
            // Only allow to set the FSM for a pid once.
            assert (fsms.get(pid) == null);

            // Check that the FSM alphabet conforms to the expected number of
            // processes.
            int ppid;
            for (DistEventType e : fsm.getAlphabet()) {
                if (e.isCommEvent()) {
                    ppid = e.getChannelId().getDstPid();
                    assert ppid >= 0 && ppid < numProcesses;

                    ppid = e.getChannelId().getSrcPid();
                    assert ppid >= 0 && ppid < numProcesses;
                } else {
                    ppid = e.getPid();
                    assert ppid >= 0 && ppid < numProcesses;
                }
            }
        }

        fsms.set(pid, fsm);
        alphabet.addAll(fsm.getAlphabet());

        unSpecifiedPids -= 1;
    }

    /**
     * Augments the CFSM with synthetic events for model checking binv.
     * <p>
     * The basic strategy, regardless of invariant, is to create a separate FIFO
     * queue that will be used to record the sequence of executed events that
     * are relevant to the invariant.
     * </p>
     * <p>
     * For instance, for a AFby b invariant, create a queue Q_ab. Modify any
     * state p that has an outgoing "a" transition, add a synthetic state
     * p_synth, and redirect the "a" transition from p to p_synth. Then, add
     * just one outgoing transition on "Q_ab ! a" from p_synth to the original
     * state target of "a" in state p. That is, whenever "a" occurs, we will add
     * "a" to Q_ab. Do the same for event "b".
     * </p>
     * <p>
     * For a AFby b bad state pairs within the modified GFSM (per above
     * procedure) are all initial state and all states where all queues except
     * Q_ab are empty, and where Q_ab = [*a], and where the process states are
     * terminal. In a sense, we've added Q_ab to track "a" and "b" executions,
     * and have not interfered with the normal execution of the FIFO system.
     * </p>
     * <p>
     * For a AP b, the procedure is identical, but the second bad state in every
     * pair would have Q_ab = [b*]. For a NFby b, Q_ab = [*a*b*]. In a sense,
     * we've expressed LTL properties as regular expressions of Q_ab queue
     * contents.
     * </p>
     */
    public void augmentWithInvTracing(BinaryInvariant binv) throws Exception {
        if (binv instanceof EventuallyHappens) {
            augmentWithInvTracing((EventuallyHappens) binv);
        } else if (binv instanceof AlwaysPrecedes
                || binv instanceof AlwaysFollowedBy
                || binv instanceof NeverFollowedBy) {
            augmentWithBinInvTracing(binv);
        } else {
            throw new Exception("Unrecognized binary invarianr type: "
                    + binv.toString());
        }
    }

    /**
     * Generate a Promela representation of this CFSM, to be used with SPIN.
     * This representation includes an appropriate LTL formula corresponding to
     * any invariants that augment this CFSM.
     */
    public String toPromelaString(String cfsmName, int chanCapacity) {
        assert unSpecifiedPids == 0;

        String ret = "/* Spin-promela " + cfsmName + " */\n\n";

        // Message types:
        ret += "/* Message types: */\n";
        for (int i = 0; i < channelIds.size(); i++) {
            String iStr = Integer.toString(i);
            ret += "mtypesChan" + iStr + " = {";
            //
            // TODO: output channel event types here.
            //
            ret += "};";
        }
        ret += "\n\n";

        // Channels:
        ret += "/* Channels: */\n";
        for (int i = 0; i < channelIds.size(); i++) {
            String iStr = Integer.toString(i);
            ret += "chan chan" + iStr + " = [" + Integer.toString(chanCapacity)
                    + "] of { mtypesChan" + iStr + "}\n";
        }
        ret += "\n\n";

        // FSM state vars declaration, one per FSM:
        ret += "/* FSM state vars: */\n";
        for (int pid = 0; pid < numProcesses; pid++) {
            ret += "byte state" + Integer.toString(pid) + " = 0;\n";
        }
        ret += "\n\n";

        // Each of the FSMs in the CFSM:
        for (int pid = 0; pid < numProcesses; pid++) {
            String stateVar = "state" + Integer.toString(pid);
            FSM f = fsms.get(pid);
            ret += "active proctype p" + Integer.toString(pid) + "()\n";
            ret += "{\n";
            f.toPromelaString(stateVar);
            ret += "}\n\n";
        }
        ret += "\n\n";

        return ret;
    }

    /**
     * Generate SCM representation of this CFSM, with bad states if this CFSM
     * was augmented with any invariants.
     */
    public String toScmString(String cfsmName) {
        assert unSpecifiedPids == 0;

        String ret = "scm " + cfsmName + ":\n\n";

        // Channels:
        // Add a special channel for handling local events, represented as
        // messages on this channel.
        LocalEventsChannelId localChId;
        if (localEventsChIndex == Integer.MAX_VALUE) {
            localChId = new LocalEventsChannelId(this.channelIds.size());
            localEventsChIndex = localChId.getScmId();
            this.channelIds.add(localChId);
        } else {
            localChId = (LocalEventsChannelId) this.channelIds
                    .get(localEventsChIndex);
        }

        ret += "nb_channels = " + channelIds.size() + " ;\n";
        ret += "/*\n";
        for (int i = 0; i < channelIds.size(); i++) {
            ret += "channel " + Integer.toString(i) + " : "
                    + channelIds.get(i).toString() + "\n";
        }
        ret += "*/\n\n";

        // Whether or not any channels are lossy:
        // TODO: add lossy field to ChannelId and list all channel ids that
        // are lossy here.

        // Parameters/Alphabet:
        ret += "parameters :\n";
        ret += alphabet.toScmParametersString();
        ret += "\n";

        // FSMS:
        for (int pid = 0; pid < numProcesses; pid++) {
            FSM f = fsms.get(pid);
            ret += "automaton p" + Integer.toString(pid) + " :\n";
            ret += f.toScmString(localChId);
            ret += "\n";
        }

        // Bad states:
        if (!invs.isEmpty()) {
            ret += "\nbad_states:\n";
            for (BadState b : getBadStates()) {
                ret += b.toScmString() + "\n";
            }
        }

        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    private Set<CFSMState> deriveAllPermsOfStates(IFSMToStateSetFn<FSMState> fn) {
        if (numProcesses == 1) {
            Set<CFSMState> ret = Util.newSet();
            for (FSMState s : fn.eval(fsms.get(0))) {
                ret.add(new CFSMState(s));
            }
            return ret;
        }

        assert numProcesses > 1;

        // Permutations for processes 0 and 1.
        List<List<FSMState>> perms = Util.get2DPermutations(
                fn.eval(fsms.get(0)), fn.eval(fsms.get(1)));

        // Permutations for process with pid >= 2.
        int i = 2;
        while (i != numProcesses) {
            // Modifies perms in place.
            perms = Util.get2DPermutations(perms, fn.eval(fsms.get(i)));
            i += 1;
        }

        return CFSMState.CFSMStatesFromFSMListLists(perms);
    }

    /**
     * Augment this CFSM with an "eventually happens e" invariant for model
     * checking. This procedure is slightly different from binary invariants. In
     * particular, we do not trace an 'initial' event and instead just trace the
     * event e.
     */
    private void augmentWithInvTracing(EventuallyHappens inv) {
        DistEventType e1 = inv.getEvent();

        assert alphabet.contains(e1);
        assert e1.getPid() < fsms.size();
        assert !invs.contains(invs);

        invs.add(inv);

        int scmId = this.channelIds.size();

        if (firstSyntheticChIndex > scmId) {
            firstSyntheticChIndex = scmId;
        }

        // Create and add a new invariant-specific channel.
        ChannelId invCid = new InvChannelId(inv, scmId);
        this.channelIds.add(invCid);

        // Update the FSM corresponding to e1.
        Set<FSMState> visited = Util.newSet();
        FSM f1 = this.fsms.get(e1.getPid());
        DistEventType e1Tracer1 = DistEventType
                .SynthSendEvent(e1, invCid, true);
        DistEventType e1Tracer2 = DistEventType.SynthSendEvent(e1, invCid,
                false);
        addSendToEventTx(f1, e1, e1Tracer1, e1Tracer2, visited);
        this.alphabet.add(e1Tracer1);
        this.alphabet.add(e1Tracer2);

        inv.setFirstSynthTracers(e1Tracer1, e1Tracer2);
        inv.setSecondSynthTracers(null, null);
    }

    /**
     * Augments the CFSM with a tracking channel and send synthetic messages on
     * this channel to keep track of the two events that are part of the binary
     * invariant inv. Adds the synthetic events to the CFSM alphabet.
     */
    private void augmentWithBinInvTracing(BinaryInvariant inv) {
        DistEventType e1 = inv.getFirst();
        DistEventType e2 = inv.getSecond();

        assert alphabet.contains(e1);
        assert alphabet.contains(e2);
        assert e1.getPid() < fsms.size();
        assert e2.getPid() < fsms.size();
        assert !invs.contains(invs);

        invs.add(inv);

        int scmId = this.channelIds.size();

        if (firstSyntheticChIndex > scmId) {
            firstSyntheticChIndex = scmId;
        }

        // Create and add a new invariant-specific channel.
        ChannelId invCid = new InvChannelId(inv, scmId);
        this.channelIds.add(invCid);

        // Update the FSM corresponding to e1.
        Set<FSMState> visited = Util.newSet();
        FSM f1 = this.fsms.get(e1.getPid());
        DistEventType e1Tracer1 = DistEventType
                .SynthSendEvent(e1, invCid, true);
        DistEventType e1Tracer2 = DistEventType.SynthSendEvent(e1, invCid,
                false);
        addSendToEventTx(f1, e1, e1Tracer1, e1Tracer2, visited);
        this.alphabet.add(e1Tracer1);
        this.alphabet.add(e1Tracer2);

        // Update the FSM corresponding to e2.
        visited.clear();
        FSM f2 = this.fsms.get(e2.getPid());
        DistEventType e2Tracer1 = DistEventType
                .SynthSendEvent(e2, invCid, true);
        DistEventType e2Tracer2 = DistEventType.SynthSendEvent(e2, invCid,
                false);
        addSendToEventTx(f2, e2, e2Tracer1, e2Tracer2, visited);
        this.alphabet.add(e2Tracer1);
        this.alphabet.add(e2Tracer2);

        inv.setFirstSynthTracers(e1Tracer1, e1Tracer2);
        inv.setSecondSynthTracers(e2Tracer1, e2Tracer2);
    }

    /**
     * Traverse the graph of FSM f and replace all transitions on e to some
     * state s to transition to a new state X, and add a transition from X to s
     * that enqueues event e on channel identified by invCid.
     * 
     * @param visited
     * @param f1
     * @param e1
     * @param invCid
     */
    private void addSendToEventTx(FSM f, DistEventType eToTrace,
            DistEventType eTracer1, DistEventType eTracer2,
            Set<FSMState> visited) {
        for (FSMState init : f.getInitStates()) {
            recurseAddSendToEventTx(f, init, eToTrace, eTracer1, eTracer2,
                    visited);
        }
    }

    /**
     * Recursive call to perform DFA exploration of FSM f. Helper to
     * addSendToEventTx
     * 
     * @param visited
     */
    private void recurseAddSendToEventTx(FSM f, FSMState parent,
            DistEventType eToTrace, DistEventType eTracer1,
            DistEventType eTracer2, Set<FSMState> visited) {

        if (visited.contains(parent)) {
            return;
        }

        visited.add(parent);

        // If there is a transition on to-trace event, then perform the
        // re-writing.
        if (parent.getTransitioningEvents().contains(eToTrace)) {
            for (FSMState child : parent.getNextStates(eToTrace)) {
                f.addSyntheticState(parent, child, eToTrace, eTracer1, eTracer2);

                if (!visited.contains(child)) {
                    // If we haven't visited the child yet, then recurse to it.
                    recurseAddSendToEventTx(f, child, eToTrace, eTracer1,
                            eTracer2, visited);
                }
            }
        }

        for (DistEventType e : parent.getTransitioningEvents()) {
            // Now handle all the non-to-trace events. Note, however, that these
            // have been re-written above with eTracer1 events, so this is what
            // we check for.
            if (!e.equals(eTracer1)) {
                for (FSMState nextF : parent.getNextStates(e)) {
                    if (visited.contains(nextF)) {
                        continue;
                    }

                    recurseAddSendToEventTx(f, nextF, eToTrace, eTracer1,
                            eTracer2, visited);
                }
            }
        }
    }

}
