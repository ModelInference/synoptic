package algorithms;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import main.InvariMintOptions;
import model.EventTypeEncodings;
import model.InvModel;
import model.InvsModel;

import synoptic.algorithms.KTails;
import synoptic.invariants.TOInitialTerminalInvariant;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.invariants.miners.KTailInvariantMiner;
import synoptic.model.EventNode;
import synoptic.model.PartitionGraph;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.export.GraphExporter;
import synoptic.model.interfaces.ITransition;

public class InvariMintKTails extends PGraphInvariMint {

    public InvariMintKTails(InvariMintOptions opts) throws Exception {
        super(opts, "KTails");
    }

    /**
     * Runs InvariMint-kTails and returns the DFA model.
     * 
     * @param opts
     * @param traceGraph
     * @return
     * @throws Exception
     */
    @Override
    public InvsModel runInvariMint() throws Exception {
        assert opts.invMintKTails;

        ITOInvariantMiner ktailsMiner = new KTailInvariantMiner(
                opts.kTailLength);

        logger.info("Initializing with an all-accepting model.");
        this.initializeModel();

        logger.info("\n\nApplying 'Traces start with Init' inv.");

        // Add the "^Initial[^Terminal]*Terminal$" invariant
        InvModel initialTerminalInv = new InvModel(
                new TOInitialTerminalInvariant(initialEvent, terminalEvent,
                        Event.defTimeRelationStr), encodings);
        invMintModel.intersectWith(initialTerminalInv);

        logger.info("Mining Invs.");

        // Mine invariants using the specialized invMiner.
        minedInvs = this.mineInvariants(ktailsMiner);

        logger.info("Intersecting current model with mined invs with minimize intersections="
                + opts.minimizeIntersections);

        // Intersect current model with mined invariants.
        invMintModel = InvComposition.intersectModelWithInvs(minedInvs,
                opts.minimizeIntersections, invMintModel);

        logger.info("InvariMint mined properties: "
                + minedInvs.toPrettyString());

        return invMintModel;

    }

    @Override
    public void runStdAlg() {
        logger.info("Running Standard KTails");

        // ////////////
        // Export the initial trace graph (as a partition graph):
        PartitionGraph initPGraph = new PartitionGraph(traceGraph, false, null);
        String exportPrefix = opts.outputPathPrefix + "." + stdAlgName
                + ".pGraph-initial.dot";
        try {
            GraphExporter.exportGraph(exportPrefix, initPGraph, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        GraphExporter.generatePngFileFromDotFile(exportPrefix);
        // ////////////

        stdAlgPGraph = KTails.performKTails(traceGraph, opts.kTailLength);
    }

    // //////////////////////////////////////////////////////////////////////

    /**
     * Initialize an all-accepting InvariMint DFA.
     * 
     * @throws IOException
     */
    private void initializeModel() throws IOException {
        assert invMintModel == null;

        logger.fine("Creating EventType encoding.");
        encodings = new EventTypeEncodings(getAllEventTypes());

        // Initial model will accept all Strings.
        logger.fine("Creating an initial, all-accepting, model.");
        invMintModel = new InvsModel(encodings);
    }

    /**
     * Returns all event types present in the traceGraph (including INITIAL and
     * TERMINAL).
     */
    private Set<EventType> getAllEventTypes() {
        EventNode initNode = traceGraph.getDummyInitialNode();
        Set<EventType> eTypes = new LinkedHashSet<EventType>();

        // Iterate through all the traces -- each transition from the
        // INITIAL node connects\holds a single trace.
        for (ITransition<EventNode> initTrans : initNode.getAllTransitions()) {

            EventNode cur = initTrans.getTarget();
            EventType first = initNode.getEType();
            EventType second = cur.getEType();

            while (true) {
                if (!eTypes.contains(first)) {
                    eTypes.add(first);
                }
                if (cur.getAllTransitions().size() == 0) {
                    // Add terminal event to the set.
                    if (!eTypes.contains(second)) {
                        eTypes.add(second);
                    }
                    break;
                }
                cur = cur.getAllTransitions().get(0).getTarget();
                first = second;
                second = cur.getEType();
            }
        }
        return eTypes;
    }
}
