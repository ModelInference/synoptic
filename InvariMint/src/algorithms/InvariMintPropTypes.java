package algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import main.InvariMintOptions;
import model.EventTypeEncodings;
import model.InvsModel;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.invariants.miners.ImmediateInvariantMiner;
import synoptic.model.ChainsTraceGraph;

/**
 * Represents an InvariMint algorithm using specified standard property types:
 * AFby, NFby, AP, NIFby
 */
public class InvariMintPropTypes {

    String invMintAlgName = "InvariMintPropTypes";

    public static Logger logger;
    InvariMintOptions opts;
    ChainsTraceGraph traceGraph;

    EventTypeEncodings encodings;
    InvsModel invMintModel;
    TemporalInvariantSet minedInvs;

    /**
     * Creates an InvariMint algorithm using specified standard property types:
     * AFby, NFby, AP, NIFby
     * 
     * @param opts
     *            InvariMint options passed
     */
    public InvariMintPropTypes(InvariMintOptions opts) {
        logger = Logger.getLogger(invMintAlgName);
        this.opts = opts;
    }

    /**
     * Runs InvariMint with specified property types.
     * 
     * @return the InvariMint made up of specified property types.
     * @throws Exception
     */
    public InvsModel runInvariMint() throws Exception {
        assert invMintModel == null;

        // Mine invariants using the specialized invMiner.
        this.mineInvariants();

        // Intersect current model with mined invariants.
        invMintModel = InvComposition.intersectModelWithInvs(minedInvs,
                opts.minimizeIntersections, invMintModel);

        return invMintModel;

    }

    /**
     * Mines invariants specified in opts into minedInvs, first by creating an
     * all-accepting model, then using ImmediateInvariantMiner to mine NIFby if
     * specified, and using ChainWalkingTOInvMiner to mine AFby, AP or NFby,
     * removing the properties not specified in opts.
     * 
     * @return specified invariants
     * @throws Exception
     */
    public void mineInvariants() throws Exception {

        // Creates the initial, all-accepting model as well as the
        // ChainsTraceGraph to be used by the miners.
        InvariMintInitializer initializer = new InvariMintInitializer(opts);
        traceGraph = initializer.getChainsTraceGraph();
        invMintModel = initializer.createAllAcceptingModel();

        // If NIFby was chosen as a property type, we mine it and add it to the
        // model
        if (opts.neverImmediatelyFollowedBy) {
            logger.fine("Mining NIFby invariant(s).");
            ImmediateInvariantMiner miner = new ImmediateInvariantMiner(
                    traceGraph);
            TemporalInvariantSet NIFbys = miner.getNIFbyInvariants();
            logger.fine("Mined " + NIFbys.numInvariants()
                    + " NIFby invariant(s).");
            logger.fine("Adding NIfby invariants to mined invariants");
            minedInvs.add(NIFbys);
            logger.fine("Intersecting model with mined NIFby invariants (minimizeIntersections="
                    + opts.minimizeIntersections + ")");
            invMintModel = InvComposition.intersectModelWithInvs(NIFbys,
                    opts.minimizeIntersections, invMintModel);

        }

        // Since ChainWalkingTOInvMiner() mines AFby, AP and NFby Property
        // types, we create the list of property types not chosen in the options
        // to delete after
        ArrayList<String> invsToDelete = new ArrayList<String>();
        if (!opts.alwaysFollowedBy) {
            invsToDelete.add("AFby");
        }
        if (!opts.alwaysPrecedes) {
            invsToDelete.add("AP");
        }
        if (!opts.neverFollowedBy) {
            invsToDelete.add("NFby");
        }

        // If none of AFby, AP and NFby were chosen, invsToDelete.size() = 3 and
        // this is not run. Else we mine all the property types
        if (invsToDelete.size() < 3) {
            ITOInvariantMiner synMiner = new ChainWalkingTOInvMiner();

            long startTime = System.currentTimeMillis();
            logger.info("Mining invariants [" + synMiner.getClass().getName()
                    + "]..");

            TemporalInvariantSet invs = synMiner.computeInvariants(traceGraph,
                    false);

            long endTime = System.currentTimeMillis();
            logger.info("Mining took " + (endTime - startTime) + "ms");

            logger.fine("Mined " + invs.numInvariants()
                    + "AFby, AP, NFby invariant(s).");

            logger.fine("Removing unspecified invariants");
            Iterator<ITemporalInvariant> i = invs.iterator();
            // IB: add comment(s) to explain what this does.
            while (i.hasNext()) {
                ITemporalInvariant inv = i.next();
                if (invsToDelete.contains(inv.getShortName()))
                    i.remove();
            }

            minedInvs.add(invs);
            // IB: maybe change this to log
            // "remain X invariants after removing invariants of type [a,b,c]"
            logger.fine("There remain " + minedInvs.numInvariants()
                    + "mined invariant(s).");
        }

    }

}
