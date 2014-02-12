package algorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import main.InvariMintOptions;
import model.EventTypeEncodings;
import model.InvModel;
import model.InvsModel;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TOInitialTerminalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.invariants.miners.ImmediateInvariantMiner;
import synoptic.main.SynopticMain;
import synoptic.main.options.SynopticOptions;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.export.DotExportFormatter;

/**
 * 
 * Represents an InvariMint algorithm using specified standard property types: AFby, NFby, AP, NIFby
 *
 */
public class InvariMintPropTypes {
    
    String invMintAlgName;

    public static Logger logger;
    InvariMintOptions opts;
    ChainsTraceGraph traceGraph;

    EventTypeEncodings encodings;
    InvsModel invMintModel;
    TemporalInvariantSet minedInvs;
    
    /**
     * Creates an InvariMint algorithm using specified standard property types: AFby, NFby, AP, NIFby
     * @param opts InvariMint options passed
     */
    public InvariMintPropTypes(InvariMintOptions opts){
    	invMintAlgName = "InvariMintPropTypes";
    	logger =Logger.getLogger(invMintAlgName);
    	this.opts = opts;
    }
    
    /**
     * Runs InvariMint with specified property types.
     * @return the InvariMint made up of specified property types. 
     * @throws Exception
     */
    public InvsModel runInvariMint() throws Exception{
    	assert invMintModel == null;

        // Mine invariants using the specialized invMiner.
        this.mineInvariants();

        // Intersect current model with mined invariants.
        invMintModel = InvComposition.intersectModelWithInvs(minedInvs,
                opts.minimizeIntersections, invMintModel);

        return invMintModel;

    }
    
    
    /**
     * Mines invariants specified in opts into minedInvs
     * @return specified invariants
     * @throws Exception
     */
    public void mineInvariants() throws Exception{
    	
    	// This will add NIFby invariants to minedInvs if specified
    	initializeModel();
    	
    	ArrayList<String> invsToDelete = new ArrayList<String>();
    	
		if (!opts.alwaysFollowedBy){	
			invsToDelete.add("AFby");
		} 
		if (!opts.alwaysPrecedes){
			invsToDelete.add("AP");
		}
		if (!opts.neverFollowedBy){
			invsToDelete.add("NFby");
		}
		
    	// This adds other specified invariants
    	if (invsToDelete.size()<3){
    		ITOInvariantMiner synMiner = new ChainWalkingTOInvMiner(); 
        
    		long startTime = System.currentTimeMillis();
    		logger.info("Mining invariants [" + synMiner.getClass().getName() + "]..");

    		TemporalInvariantSet invs = synMiner.computeInvariants(traceGraph, false);

    		long endTime = System.currentTimeMillis();
    		logger.info("Mining took " + (endTime - startTime) + "ms");

    		logger.fine("Mined " + invs.numInvariants() + "AFby, AP, NFby invariant(s).");
        
    		logger.fine("Removing unspecified invariants");
    		Iterator<ITemporalInvariant> i = invs.iterator();
    		while (i.hasNext()){
    			ITemporalInvariant inv = i.next();
    				if (invsToDelete.contains(inv.getShortName()))
    					i.remove();        	
    		}
    		
    		minedInvs.add(invs);
    		logger.fine("There remain " + minedInvs.numInvariants() + "mined invariant(s).");	
        }
        
    }
    
    /**
     * Creates an initial, all-accepting model from mined NIFby invariants.
     * @throws Exception 
     */
    public void initializeModel() throws Exception{
    	
    	traceGraph = getSynopticChainsTraceGraph();
    	
    	logger.fine("Mining NIFby invariant(s).");
    	ImmediateInvariantMiner miner = new ImmediateInvariantMiner(traceGraph);
    	TemporalInvariantSet NIFbys = miner.getNIFbyInvariants();
    	logger.fine("Mined " + NIFbys.numInvariants() + " NIFby invariant(s).");
    	
    	logger.fine("Creating EventType encoding.");
        Set<EventType> allEvents = new HashSet<EventType>(miner.getEventTypes());
        encodings = new EventTypeEncodings(allEvents);
        
        logger.fine("Creating an initial, all-accepting, model.");
        invMintModel = new InvsModel(encodings);
        
        if (opts.neverImmediatelyFollowedBy){
        	logger.fine("Adding NIfby invariants to mined invariants");
        	minedInvs.add(NIFbys);
            logger.fine("Intersecting model with mined NIFby invariants (minimizeIntersections="
                    + opts.minimizeIntersections + ")");
            invMintModel = InvComposition.intersectModelWithInvs(NIFbys,
                    opts.minimizeIntersections, invMintModel);

        	} else {logger.fine("Did not intersect model with NIFby invariants");}
    }
    
    //TODO: this is just copied from PGraphInvariMint, it seems like bad practice to have this in here.
    /** Generate the traceGraph from input log files. */
    private ChainsTraceGraph getSynopticChainsTraceGraph() throws Exception {
        // Set up options in Synoptic Main that are used by the library.
        SynopticOptions options = new SynopticOptions();
        options.logLvlExtraVerbose = true;
        options.internCommonStrings = true;
        options.recoverFromParseErrors = opts.recoverFromParseErrors;
        options.debugParse = opts.debugParse;
        options.ignoreNonMatchingLines = opts.ignoreNonMatchingLines;

        SynopticMain synMain = SynopticMain.getInstance();
        if (synMain == null) {
            synMain = new SynopticMain(options, new DotExportFormatter());
        }

        // Instantiate the parser and parse the log lines.
        TraceParser parser = new TraceParser(opts.regExps,
                opts.partitionRegExp, opts.separatorRegExp);

        List<EventNode> parsedEvents = SynopticMain.parseEvents(parser,
                opts.logFilenames);

        String errMsg = null;
        if (opts.debugParse) {
            // Terminate since the user is interested in debugging the parser.
            errMsg = "Terminating. To continue further, re-run without the debugParse option.";
        }

        if (!parser.logTimeTypeIsTotallyOrdered()) {
            errMsg = "Partially ordered log input detected. Stopping.";
        }

        if (parsedEvents.size() == 0) {
            errMsg = "Did not parse any events from the input log files. Stopping.";
        }

        if (errMsg != null) {
            logger.severe(errMsg);
            throw new Exception(errMsg);
        }

        // //////////////////
        traceGraph = parser.generateDirectTORelation(parsedEvents);
        // //////////////////

        return traceGraph;
    }
    
    
    
}
