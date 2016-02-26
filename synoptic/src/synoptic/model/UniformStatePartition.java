package synoptic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import synoptic.main.AbstractMain;
import synoptic.model.interfaces.ITransition;
import synoptic.model.state.State;
import synoptic.model.state.SynDaikonizer;

import daikonizer.DaikonInvariants;

/**
 * TODO
 */
public class UniformStatePartition extends UniformPartition {
    /**
     * Cached transitions with Daikon invariants. We need to cache these
     * transitions because Daikon invariants are expensive to compute, and we
     * may revisit the same transition many times while deriving abstract tests
     * from a model (i.e., a PartitionGraph).
     */
    private final List<Transition<UniformStatePartition>> cachedTransitionsWithInvs;

    protected UniformStatePartition(Collection<EventNode> eNodes) {
        super(eNodes);
        cachedTransitionsWithInvs = new ArrayList<>();
    }

    protected UniformStatePartition(EventNode eNode) {
        super(eNode);
        cachedTransitionsWithInvs = new ArrayList<>();
    }

    /**
     * Generates and caches outgoing transitions of this partition, each of
     * which has DaikonInvariants labeled on it. NOTE: 1) This method must be
     * called only when state processing logic is enabled. 2) Since this method
     * caches transitions, it must be called only after the final model is yield
     * (i.e., no more changes to this Partition).
     * 
     * @return transitions with Daikon invariants
     * @throws Exception
     */
    @SuppressWarnings("null")
    public List<? extends ITransition<UniformStatePartition>> getTransitionsWithDaikonInvariants() {
        assert (AbstractMain.getInstance().options.stateProcessing);

        if (!cachedTransitionsWithInvs.isEmpty() || isTerminal()) {
            return cachedTransitionsWithInvs;
        }

        for (Partition childP : getAllSuccessors()) {
            Transition<UniformStatePartition> tx = null;
            SynDaikonizer daikonizer = new SynDaikonizer();

            if (isInitial()) {
                // This is a dummy initial partition and its dummy event node
                // has no post-event state. Instead, we need to get
                // pre-event states of the successor event nodes.
                // This partition contains only a single dummy event node.
                assert events.size() == 1;
                EventNode dummyInitEvent = events.iterator().next();

                for (ITransition<EventNode> tr : dummyInitEvent
                        .getAllTransitions()) {
                    // Add only states that are on transitions to childP
                    // to daikonizer.
                    boolean stateAdded = addStateToDaikonizer(tr, childP,
                            daikonizer, false);
                    // Create transition to childP iff it doesn't already exist
                    // and tr's destination is childP.
                    if (stateAdded && tx == null) {
                        tx = createDaikonInvTransition(tr);
                        cachedTransitionsWithInvs.add(tx);
                    }
                }
            } else {
                // This is NOT a dummy initial partition. Its event nodes have
                // post-event states.
                for (EventNode event : events) {
                    List<Transition<EventNode>> transitions = event
                            .getAllTransitions();
                    // Events are totally ordered.
                    assert transitions.size() == 1;
                    ITransition<EventNode> tr = transitions.iterator().next();
                    boolean stateAdded = addStateToDaikonizer(tr, childP,
                            daikonizer, true);
                    if (stateAdded && tx == null) {
                        tx = createDaikonInvTransition(tr);
                        cachedTransitionsWithInvs.add(tx);
                    }
                }
            }
            assert (tx != null);
            // Generate invariants of tx.
            DaikonInvariants daikonInvs = daikonizer.getDaikonEnterInvariants();
            // Label tx with Daikon invariants.
            tx.labels.setLabel(TransitionLabelType.DAIKON_INVARIANTS_LABEL,
                    daikonInvs);
        }
        return cachedTransitionsWithInvs;
    }

    /**
     * Adds a state that is on eventTrans to daikonizer iff the target of
     * eventTrans is in targetPartition.
     * 
     * @return true iff the state is added to daikonizer.
     * @throws Exception
     */
    private static boolean addStateToDaikonizer(
            ITransition<EventNode> eventTrans, Partition targetPartition,
            SynDaikonizer daikonizer, boolean post) {
        EventNode srcEvent = eventTrans.getSource();
        EventNode dstEvent = eventTrans.getTarget();
        Partition dstPartition = dstEvent.getParent();

        if (dstPartition.compareTo(targetPartition) == 0) {
            State state = post ? srcEvent.getPostEventState()
                    : dstEvent.getPreEventState();
            daikonizer.addInstance(state);
            return true;
        }
        return false;
    }

    /**
     * Creates Partition transition from the given EventNode transition. This
     * transition will be used to store Daikon invariants.
     */
    private static Transition<UniformStatePartition> createDaikonInvTransition(
            ITransition<EventNode> eventTrans) {
        EventNode srcNode = eventTrans.getSource();
        EventNode targetNode = eventTrans.getTarget();

        UniformStatePartition srcPart = (UniformStatePartition) srcNode
                .getParent();
        UniformStatePartition targetPart = (UniformStatePartition) targetNode
                .getParent();

        Transition<UniformStatePartition> tx = new Transition<>(srcPart,
                targetPart, eventTrans.getRelation());
        updateTransitionDeltas(srcNode, targetNode, tx);
        // But, tx has no invariants associated with it yet.
        return tx;
    }
}
