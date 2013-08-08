package algorithms;

import java.io.IOException;

import main.InvariMintOptions;
import model.InvsModel;

import synoptic.algorithms.KTails;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.invariants.miners.KTailInvariantMiner;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphExporter;

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
        InvsModel model = super.runInvariMint(ktailsMiner);

        logger.info("InvariMint mined properties: "
                + minedInvs.toPrettyString());

        return model;

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

}
