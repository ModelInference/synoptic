package algorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import main.InvariMintOptions;
import model.EncodedAutomaton;
import model.EventTypeEncodings;
import model.InvModel;
import model.InvsModel;
import model.PartitionGraphAutomaton;

import synoptic.algorithms.Bisimulation;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TOInitialTerminalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.invariants.miners.ImmediateInvariantMiner;
import synoptic.main.SynopticMain;
import synoptic.model.PartitionGraph;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;

/**
 * Implements a Synoptic-style InvariMint algorithm. Note that this algorithm is
 * not identical to Synoptic, though the produced models will sometimes be
 * identical to those generated with Synoptic.
 */
public class InvariMintSynoptic extends PGraphInvariMint {

    // Determines if we compute and output declarative and procedural synoptic
    // comparison metrics (used in the InvariMint TSE'14 paper). Set to TRUE to
    // reproduce comparison/results.
    public static boolean tseEval = false;

    public InvariMintSynoptic(InvariMintOptions opts) throws Exception {
        super(opts, "Synoptic");
    }

    private TemporalInvariantSet filterInvs(TemporalInvariantSet invs) {
        // Filter invariants to just include AFby, AP, and NFby types.
        List<String> okTypes = new ArrayList<String>();
        okTypes.add("AFby");
        okTypes.add("NFby");
        okTypes.add("AP");
        TemporalInvariantSet filteredInvs = new TemporalInvariantSet();
        for (ITemporalInvariant inv : invs) {
            if (okTypes.contains(inv.getShortName())) {
                filteredInvs.add(inv);
            }
        }
        return filteredInvs;
    }

    /**
     * Runs InvariMint-Synoptic (NFby, AFby, AP invariant types) and returns the
     * DFA model.
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

        logger.info("Mined invariants:\n" + minedInvs);

        logger.info("Intersecting current model with mined invs with minimize intersections="
                + opts.minimizeIntersections);

        TemporalInvariantSet filteredInvs = filterInvs(minedInvs);
        // Intersect current model with filtered set of invariants.
        invMintModel = InvComposition.intersectModelWithInvs(// minedInvs,
                filteredInvs, opts.minimizeIntersections, invMintModel);

        return invMintModel;
    }

    /**
     * Runs standard Synoptic 10 times using random seeds 0,...,9 and returns a
     * model that is the union of the resulting models.
     */
    public EncodedAutomaton runStdAlgRobust() {
        // assert minedInvs != null;
        ITOInvariantMiner synMiner = new ChainWalkingTOInvMiner();
        minedInvs = this.mineInvariants(synMiner);
        TemporalInvariantSet filteredInvs = filterInvs(minedInvs);

        stdAlgPGraph = null;
        PartitionGraph pGraph;
        EncodedAutomaton dfaUnion = null;
        // Run standard Synoptic using a random seed 0,...,9.
        for (int i = 0; i < 10; i++) {
            logger.info("Running Standard Synoptic with random seed " + i);
            SynopticMain.getInstance().random = new Random(i);
            SynopticMain.getInstance().options.logLvlQuiet = true;
            SynopticMain.getInstance().options.logLvlVerbose = false;
            SynopticMain.getInstance().options.logLvlExtraVerbose = false;

            PartitionGraph synModel = new PartitionGraph(traceGraph, true,
                    filteredInvs);
            Bisimulation.splitUntilAllInvsSatisfied(synModel);
            Bisimulation.mergePartitions(synModel);
            pGraph = synModel;
            EncodedAutomaton dfaNew = new PartitionGraphAutomaton(pGraph,
                    encodings);
            if (dfaUnion == null) {
                dfaUnion = dfaNew;
            } else {
                // Union in the newly generated model.
                // Optimization opportunity: minimize the result.
                dfaUnion.union(dfaNew);
            }
        }
        return dfaUnion;
    }

    /**
     * Runs standard Synoptic once.
     */
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

    /**
     * Helper function to take a log of a number w.r.t a specific base.
     */
    public double logOfBase(int base, double num) {
        return Math.log(num) / Math.log(base);
    }

    /**
     * Compares the InvariMint-derived model to the model derived using the
     * Standard algorithm.
     */
    @SuppressWarnings("null")
    @Override
    public boolean compareToStandardAlg() {
        if (tseEval) {
            // Standard synoptic model, that is the union of 10 Synoptic
            // models, each one generated using a different random seed.
            EncodedAutomaton synProceduralModel = runStdAlgRobust();

            // Minimize both models.
            synProceduralModel.minimize();
            invMintModel.minimize();

            /*
             * logger.info("\n"); logger.info("# states(dfaUnion) = " +
             * dfaUnion.model.getNumberOfStates());
             * logger.info("# states(invMintModel) = " +
             * invMintModel.model.getNumberOfStates()); logger.info("\n");
             * logger.info("# txns(dfaUnion) = " +
             * dfaUnion.model.getNumberOfTransitions());
             * logger.info("# txns(invMintModel) = " +
             * invMintModel.model.getNumberOfTransitions()); logger.info("\n");
             */

            String exportUnionName = opts.outputPathPrefix + "." + "synUnion"
                    + ".dfa.dot";
            try {
                synProceduralModel.exportDotAndPng(exportUnionName);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }

            exportUnionName = opts.outputPathPrefix + "." + "invMint"
                    + ".dfa.dot";
            try {
                invMintModel.exportDotAndPng(exportUnionName);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }

            // Number of declarative synoptic strings < strLenBound.
            int invMintStringCount = 0;

            // Number of procedural synoptic strings < strLenBound.
            int synStringCount = 0;

            // A key number that determines the granularity at which the
            // procedural and declarative Synoptic models are compared. This
            // length bound allows us to approximately compare the language sets
            // of two models that are both infinite.
            int strLenBound = 7;

            // Iterate over string lengths < strLenBound and see what strings of
            // this length exist in the two models.
            for (int i = 1; i < strLenBound; i++) {
                logger.info("str-len = " + i);
                Set<String> newSynStrings = synProceduralModel.getStrings(i);
                logger.info("|L(synDFA)| = " + newSynStrings.size());
                synStringCount += newSynStrings.size();

                Set<String> newInvStrings = invMintModel.getStrings(i);
                logger.info("|L(invMintDFA)| = " + newInvStrings.size() + "\n");
                invMintStringCount += newInvStrings.size();
            }
            logger.info("|L(synDFA).strings(<" + strLenBound + ")| = "
                    + synStringCount);
            logger.info("|L(invMintDFA.strings(<" + strLenBound + ")| = "
                    + invMintStringCount + "\n");

            logger.info("|synDfa| / |invMintDFA| = "
                    + ((synStringCount * 1.0) / invMintStringCount));
            logger.info("|invMintDFA| / |synDfa| = "
                    + (invMintStringCount / (synStringCount * 1.0)));
            logger.info("lg(|invMintDFA| / |synDfa|) = "
                    + logOfBase(
                            2,
                            ((invMintStringCount * 1.0) / (synStringCount * 1.0))));
            return false;
        }

        stdAlgpGraphToDFA();
        assert stdAlgDFA != null;

        boolean stdSubset = stdAlgDFA.subsetOf(invMintModel);
        boolean invSubset = invMintModel.subsetOf(stdAlgDFA);

        logger.info("L(stdAlgDFA) subsetOf L(invMintDFA): " + stdSubset);
        logger.info("L(invMintDFA) subsetOf L(stdAlgDFA): " + invSubset);

        if (opts.outputModelDiff) {
            logger.info("Exporting the invMintDFS and stdAlgDFA model difference");
            EncodedAutomaton modelDiff = null;
            String exportDiffFname = "";
            if (stdSubset && !invSubset) {
                // Output traces in invMintDFA that are not in stdAlgDFA:
                modelDiff = invMintModel.differenceWith(stdAlgDFA);
                exportDiffFname = opts.outputPathPrefix + "." + "InvMint-Std"
                        + ".dfa.dot";
            } else if (!stdSubset && invSubset) {
                // Output traces in stdAlgDFA that are not in invMintDFA:
                modelDiff = invMintModel.differenceWith(stdAlgDFA);
                exportDiffFname = opts.outputPathPrefix + "." + "Std-InvMint"
                        + ".dfa.dot";
            }

            if (modelDiff != null) {
                assert (exportDiffFname != "");
                try {
                    modelDiff.exportDotAndPng(exportDiffFname);
                } catch (IOException e) {
                    logger.info("Unable to export model difference.");
                }
            }
        }

        return stdSubset && invSubset;
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
        logger.fine(NIFbys.toPrettyString());

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
