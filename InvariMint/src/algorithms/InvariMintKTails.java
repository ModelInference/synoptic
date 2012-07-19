package algorithms;

import main.InvariMintOptions;
import model.InvsModel;

import synoptic.algorithms.KTails;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.invariants.miners.KTailInvariantMiner;

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
        return super.runInvariMint(ktailsMiner);
    }

    @Override
    protected void runStdAlg() {
        logger.info("Running Standard KTails");
        stdAlgPGraph = KTails.performKTails(traceGraph, opts.kTailLength);
    }

}
