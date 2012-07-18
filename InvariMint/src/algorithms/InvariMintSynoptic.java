package algorithms;

import main.InvariMintOptions;
import model.InvsModel;

import synoptic.algorithms.Bisimulation;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.model.PartitionGraph;

public class InvariMintSynoptic extends PGraphInvariMint {

    public InvariMintSynoptic(InvariMintOptions opts) throws Exception {
        super(opts);
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
        return super.runInvariMint(synMiner);
    }

    @Override
    protected void runStdAlg() {
        assert minedInvs != null;

        logger.info("Running Standard Synoptic");
        PartitionGraph synModel = new PartitionGraph(traceGraph, true,
                minedInvs);
        Bisimulation.splitUntilAllInvsSatisfied(synModel);
        Bisimulation.mergePartitions(synModel);
    }

}
