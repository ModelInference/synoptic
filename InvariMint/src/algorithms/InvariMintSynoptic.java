package algorithms;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import main.InvariMintOptions;
import model.EventTypeEncodings;
import model.InvModel;
import model.InvsModel;

import synoptic.algorithms.Bisimulation;
import synoptic.invariants.TOInitialTerminalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.invariants.miners.ImmediateInvariantMiner;
import synoptic.model.PartitionGraph;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;

public class InvariMintSynoptic extends PGraphInvariMint {

    public InvariMintSynoptic(InvariMintOptions opts) throws Exception {
        super(opts, "Synoptic");
    }

    /**
     * Runs InvariMint-Synoptic and returns the DFA model.
     * 
     * @param opts
     * @param traceGraph
     * @return
     * @throws Exception
     */
    @Override
    public InvsModel runInvariMint() throws Exception {
        assert opts.invMintSynoptic;

        ITOInvariantMiner synMiner = new ChainWalkingTOInvMiner();

        logger.info("Initializing with NIFby invs.");

        // NIFby invariants.
        this.initializeModel();

        logger.info("\n\nApplying Init AFby Term inv.");

        // TODO: It is unclear that InvariMint-Synoptic needs such a strong
        // Init/Term invariant.

        // Add the "^Initial[^Terminal]*Terminal$" invariant
        InvModel initialTerminalInv = new InvModel(
                new TOInitialTerminalInvariant(initialEvent, terminalEvent,
                        Event.defTimeRelationStr), encodings);
        invMintModel.intersectWith(initialTerminalInv);

        logger.info("Mining Invs.");

        // Mine invariants using the specialized invMiner.
        minedInvs = this.mineInvariants(synMiner);

        logger.info("Intersecting current model with mined invs with minimize intersections="
                + opts.minimizeIntersections);

        // Intersect current model with mined invariants.
        invMintModel = InvComposition.intersectModelWithInvs(minedInvs,
                opts.minimizeIntersections, invMintModel);

        return invMintModel;
    }

    @Override
    public void runStdAlg() {
        // assert minedInvs != null;
        ITOInvariantMiner synMiner = new ChainWalkingTOInvMiner();
        minedInvs = this.mineInvariants(synMiner);

        logger.info("Running Standard Synoptic");
        PartitionGraph synModel = new PartitionGraph(traceGraph, true,
                minedInvs);
        Bisimulation.splitUntilAllInvsSatisfied(synModel);
        Bisimulation.mergePartitions(synModel);
        stdAlgPGraph = synModel;
    }

    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Initialize the InvariMint DFA from mined NIFby invariants.
     * 
     * @throws IOException
     */
    private void initializeModel() throws IOException {
        assert invMintModel == null;

        logger.fine("Mining NIFby invariant(s).");
        ImmediateInvariantMiner miner = new ImmediateInvariantMiner(traceGraph);
        TemporalInvariantSet NIFbys = miner.getNIFbyInvariants();
        logger.fine("Mined " + NIFbys.numInvariants() + " NIFby invariant(s).");
        // logger.fine(NIFbys.toPrettyString());

        logger.fine("Creating EvenType encoding.");
        Set<EventType> allEvents = new HashSet<EventType>(miner.getEventTypes());
        encodings = new EventTypeEncodings(allEvents);

        // Initial model will accept all Strings.
        logger.fine("Creating an initial, all-accepting, model.");
        invMintModel = new InvsModel(encodings);

        logger.fine("Intersecting model with mined NIFby invariants (minimizeIntersections="
                + opts.minimizeIntersections + ")");
        invMintModel = InvComposition.intersectModelWithInvs(NIFbys,
                opts.minimizeIntersections, invMintModel);
    }

}
