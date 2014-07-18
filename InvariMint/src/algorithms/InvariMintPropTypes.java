package algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
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
import synoptic.model.event.EventType;

/**
 * Represents an InvariMint algorithm using specified standard property types:
 * AFby, NFby, AP, NIFby
 */
public class InvariMintPropTypes {

    final String invMintAlgName = "InvariMintPropTypes";

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
        minedInvs = new TemporalInvariantSet();
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

        }

        // Since ChainWalkingTOInvMiner() mines AFby, AP and NFby Property
        // types, we create the list of property types not chosen in the options
        // to delete.
        ArrayList<String> invTypesToDelete = new ArrayList<String>();
        if (!opts.alwaysFollowedBy) {
            invTypesToDelete.add("AFby");
        }
        if (!opts.alwaysPrecedes) {
            invTypesToDelete.add("AP");
        }
        if (!opts.neverFollowedBy) {
            invTypesToDelete.add("NFby");
        }

        // If none of AFby, AP and NFby were chosen, invTypesToDelete.size() = 3
        // and this is not run. Else we mine all the property types.
        if (invTypesToDelete.size() < 3) {
            ITOInvariantMiner synMiner = new ChainWalkingTOInvMiner();

            long startTime = System.currentTimeMillis();

            // mine AFby, AP and NFby invariants
            logger.info("Mining invariants [" + synMiner.getClass().getName()
                    + "]..");
            TemporalInvariantSet invs = synMiner.computeInvariants(traceGraph,
                    false, false);
            long endTime = System.currentTimeMillis();
            logger.info("Mining took " + (endTime - startTime) + "ms");
            logger.fine("Mined " + invs.numInvariants()
                    + " AFby, AP, NFby invariant(s).");

            // Iterate through the set of mined invariants to remove those that
            // were not in opts -- i.e. those whose names are in invsToDelete
            logger.fine("Removing unspecified invariants");
            Iterator<ITemporalInvariant> i = invs.iterator();
            while (i.hasNext()) {
                ITemporalInvariant inv = i.next();
                if (invTypesToDelete.contains(inv.getShortName()))
                    i.remove();
            }

            // Add the remaining invariants -- those specified in opts.
            minedInvs.add(invs);
            logger.fine("There remain " + minedInvs.numInvariants()
                    + " mined invariant(s) after removing invariants of type "
                    + invTypesToDelete.toString() + ".");
        }

        // Finally, remove invariants with INITIAL/TERMINAL
        // TODO: certain options will not delete these, figure out why this is
        // necessary

        logger.fine("Removing invariants involving initial/terminal events");
        Iterator<ITemporalInvariant> i = minedInvs.iterator();
        while (i.hasNext()) {
            Set<EventType> events = i.next().getPredicates();
            for (EventType event : events)
                if (event.isSpecialEventType()) {
                    i.remove();
                    break;
                }
        }

        logger.fine("There remain " + minedInvs.numInvariants()
                + " mined invariant(s) after removing init/term invariants.");
        System.out.println(minedInvs.toString());

    }

}
