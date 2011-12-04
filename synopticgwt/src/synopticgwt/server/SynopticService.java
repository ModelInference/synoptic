package synopticgwt.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.algorithms.graph.PartitionMultiSplit;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.ConcurrencyInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.POInvariantMiner;
import synoptic.invariants.miners.TOInvariantMiner;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.main.Main;
import synoptic.main.ParseException;
import synoptic.main.SynopticOptions;
import synoptic.main.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.WeightedTransition;
import synoptic.model.export.DotExportFormatter;
import synoptic.model.export.GraphExporter;
import synopticgwt.client.ISynopticService;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTNode;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.GWTParseException;
import synopticgwt.shared.LogLine;

/**
 * Implements the Synoptic service which does:
 * 
 * <pre>
 * - Log parsing
 * - Model refinement
 * - Model coarsening
 * </pre>
 */
public class SynopticService extends RemoteServiceServlet implements
        ISynopticService {

    private static final long serialVersionUID = 1L;

    public static Logger logger = Logger.getLogger("SynopticService");

    // Session attribute name storing path of client's uploaded log file.
    static final String logFileSessionAttribute = "logFilePath";

    AppConfiguration config;
    HttpSession session;

    // Variables corresponding to session state.
    private PartitionGraph pGraph;
    private Integer numSplitSteps;
    private Set<ITemporalInvariant> unsatInvs;
    private TemporalInvariantSet minedInvs;
    private Set<ITemporalInvariant> activeInvs;
    private List<CExamplePath<Partition>> counterExampleTraces;
    private ChainsTraceGraph traceGraph;

    // //////////////////////////////////////////////////////////////////////////////
    // Helper methods.

    /**
     * Initializes the server state.
     */
    private void initializeRefinementState(TemporalInvariantSet invs) {
        pGraph = new PartitionGraph(traceGraph, true, invs);
        numSplitSteps = 0;

        unsatInvs = new LinkedHashSet<ITemporalInvariant>();
        unsatInvs.addAll(pGraph.getInvariants().getSet());

        counterExampleTraces = new TemporalInvariantSet(unsatInvs)
                .getAllCounterExamples(pGraph);

        unsatInvs.clear();
        if (counterExampleTraces != null) {
            logger.info("counterExampleTraces : "
                    + counterExampleTraces.toString());
            for (CExamplePath<Partition> relPath : counterExampleTraces) {
                unsatInvs.add(relPath.invariant);
            }
        } else {
            logger.info("counterExampleTraces : NONE");
        }

        activeInvs = new LinkedHashSet<ITemporalInvariant>();
        activeInvs.addAll(pGraph.getInvariants().getSet());
    }

    /**
     * Save server state into the session object.
     */
    private void storeSessionState() {
        // Store all the state in the session.
        if (session == null) {
            return;
        }

        session.setAttribute("partitionGraph", pGraph);
        session.setAttribute("numSplitSteps", 0);
        session.setAttribute("unsatInvs", unsatInvs);
        session.setAttribute("minedInvs", minedInvs);
        session.setAttribute("activeInvs", activeInvs);
        session.setAttribute("traceGraph", traceGraph);
        session.setAttribute("counterExampleTraces", counterExampleTraces);
    }

    /**
     * Retrieves session state and sets/reconstructs the local variables.
     */
    @SuppressWarnings("unchecked")
    private void retrieveSessionState() throws Exception {
        ServletContext context = getServletConfig().getServletContext();
        this.config = AppConfiguration.getInstance(context);

        // Retrieve HTTP session to access storage.
        HttpServletRequest request = getThreadLocalRequest();
        session = request.getSession();

        // Retrieve stuff from storage, and if we can't find something then we
        // throw an error since we can't continue with refinement.

        if (session.getAttribute("partitionGraph") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        pGraph = (PartitionGraph) session.getAttribute("partitionGraph");

        if (session.getAttribute("numSplitSteps") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        numSplitSteps = (Integer) session.getAttribute("numSplitSteps");

        if (session.getAttribute("unsatInvs") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        unsatInvs = (Set<ITemporalInvariant>) session.getAttribute("unsatInvs");

        if (session.getAttribute("minedInvs") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        minedInvs = (TemporalInvariantSet) session.getAttribute("minedInvs");

        if (session.getAttribute("activeInvs") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        activeInvs = (Set<ITemporalInvariant>) session
                .getAttribute("activeInvs");

        if (session.getAttribute("traceGraph") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        traceGraph = (ChainsTraceGraph) session.getAttribute("traceGraph");

        // NOTE: counterExampleTraces is allowed to be null.

        // TODO: create the ability to detect when counterExampleTraces state
        // has not been initialized.

        // if (session.getAttribute("counterExampleTraces") == null) {
        // TODO: throw appropriate exception
        // throw new Exception();
        // }
        counterExampleTraces = (List<CExamplePath<Partition>>) session
                .getAttribute("counterExampleTraces");
        return;
    }

    /**
     * Converts a partition graph into a GWTGraph
     * 
     * @param partGraph
     *            partition graph
     * @return Equivalent GWTGraph
     */
    private GWTGraph PGraphToGWTGraph(PartitionGraph partGraph) {
        GWTGraph graph = new GWTGraph();

        Set<Partition> nodeSet = partGraph.getNodes();
        HashMap<Integer, GWTNode> nodeIds = new HashMap<Integer, GWTNode>();
        GWTNode gwtPNode, adjGWTPNode;

        // Iterate through all the nodes in the pGraph
        for (Partition pNode : nodeSet) {
            String pNodeEType = pNode.getEType().toString();
            // Add the pNode to the GWTGraph
            if (nodeIds.containsKey(pNode.hashCode())) {
                gwtPNode = nodeIds.get(pNode.hashCode());
            } else {
                gwtPNode = new GWTNode(pNodeEType, pNode.hashCode());
                nodeIds.put(pNode.hashCode(), gwtPNode);
                graph.addNode(gwtPNode);
            }

            /*
             * Get the list of adjacent nodes that have the current pNode as the
             * source.
             */
            List<WeightedTransition<Partition>> adjacents = pNode
                    .getWeightedTransitions();

            // For every adjacent node, calculate the likelihood of the
            // transition, and add that to the graph's edge.
            for (WeightedTransition<Partition> wTransition : adjacents) {
                // The current adjacent partition.
                Partition adjPNode = wTransition.getTarget();

                if (nodeIds.containsKey(adjPNode.hashCode())) {
                    adjGWTPNode = nodeIds.get(adjPNode.hashCode());
                } else {
                    // Add the node to the graph so it can be connected
                    // if it doesn't exist.
                    adjGWTPNode = new GWTNode(adjPNode.getEType().toString(),
                            adjPNode.hashCode());
                    nodeIds.put(adjPNode.hashCode(), adjGWTPNode);
                    graph.addNode(adjGWTPNode);
                }

                // Truncate the last three digits to make the weight more
                // readable
                Double transitionFrac = Math
                        .ceil(wTransition.getFraction() * 1000) / 1000;

                // Add the complete weighted edge
                graph.addEdge(gwtPNode, adjGWTPNode, transitionFrac);
            }
        }
        return graph;
    }

    /**
     * Calls the TemporalInvariantSetToGWTInvariants below, but first determines
     * if there are any concurrency invariants in the input set.
     */
    private GWTInvariantSet TemporalInvariantSetToGWTInvariants(
            Set<ITemporalInvariant> invs) {
        boolean containsConcurrencyInvs = false;
        for (ITemporalInvariant inv : invs) {
            if (inv instanceof ConcurrencyInvariant) {
                containsConcurrencyInvs = true;
            }
        }
        return TemporalInvariantSetToGWTInvariants(containsConcurrencyInvs,
                invs);
    }

    /**
     * Converts a TemporalInvariantSet into GWTInvariants
     * 
     * @param containsConcurrencyInvs
     *            whether or not the set of invariants contains any
     *            ConcurrencyInvariant instances
     * @param invs
     * @return Equivalent GWTInvariants
     */
    private GWTInvariantSet TemporalInvariantSetToGWTInvariants(
            boolean containsConcurrencyInvs, Set<ITemporalInvariant> invs) {
        GWTInvariantSet GWTinvs = new GWTInvariantSet();
        for (ITemporalInvariant inv : invs) {
            assert (inv instanceof BinaryInvariant);
            BinaryInvariant bInv = ((BinaryInvariant) inv);
            String invKey = inv.getShortName();
            GWTInvariant invVal = new GWTInvariant(bInv.getFirst().toString(),
                    bInv.getSecond().toString(), bInv.getShortName());

            // Set a unique identification id.
            invVal.setID(inv.hashCode());

            GWTinvs.addInv(invKey, invVal);

        }
        return GWTinvs;
    }

    // //////////////////////////////////////////////////////////////////////////////

    /**
     * Parses the input log, and sets up and stores Synoptic session state for
     * refinement\coarsening. For a PO log this returns a null GWTGraph.
     * 
     * @throws Exception
     */
    @Override
    public GWTPair<GWTInvariantSet, GWTGraph> parseLog(String logLines,
            List<String> regExps, String partitionRegExp, String separatorRegExp)
            throws Exception {

        // Set up some static variables in Main that are necessary to use the
        // Synoptic library.
        Main.options = new SynopticOptions();
        // Output as much internal Synoptic information as possible.
        Main.options.logLvlExtraVerbose = true;
        synoptic.main.Main.setUpLogging();
        Main.random = new Random(Main.options.randomSeed);
        Main.graphExportFormatter = new DotExportFormatter();

        // Instantiate the parser and parse the log lines.
        TraceParser parser = null;
        ArrayList<EventNode> parsedEvents = null;

        try {
            parser = synoptic.main.Main.newTraceParser(regExps,
                    partitionRegExp, separatorRegExp);
            parsedEvents = parser.parseTraceString(logLines, new String(
                    "traceName"), -1);
        } catch (ParseException pe) {
            logger.info("Caught parse exception: " + pe.toString());
            pe.printStackTrace();
            throw new GWTParseException(pe.getMessage(), pe.getCause(),
                    pe.getRegex(), pe.getLogLine());

        } catch (Exception e) {
            logger.info("Caught exception: " + e.toString());
            e.printStackTrace();
            throw e;
        }

        // Code below mines invariants, and converts them to GWTInvariants.
        // TODO: refactor synoptic main so that it does all of this most of this
        // for the client.
        GWTGraph graph;

        if (parser.logTimeTypeIsTotallyOrdered()) {
            traceGraph = parser.generateDirectTORelation(parsedEvents);
            TOInvariantMiner miner = new ChainWalkingTOInvMiner();
            minedInvs = miner.computeInvariants(traceGraph);

            // Since we're in the TO case then we also initialize and store
            // refinement state.
            initializeRefinementState(minedInvs);
            storeSessionState();
            graph = PGraphToGWTGraph(pGraph);

        } else {
            // TODO: expose to the user the option of using another kind of
            // PO invariant miner.
            DAGsTraceGraph inputGraph = parser
                    .generateDirectPORelation(parsedEvents);
            POInvariantMiner miner = new TransitiveClosureInvMiner();
            minedInvs = miner.computeInvariants(inputGraph);
            graph = null;
        }

        GWTInvariantSet invs = TemporalInvariantSetToGWTInvariants(
                !parser.logTimeTypeIsTotallyOrdered(), minedInvs.getSet());

        return new GWTPair<GWTInvariantSet, GWTGraph>(invs, graph);
    }

    /**
     * Reads the log file given by path in session state on server. Passes log
     * file contents into parseLog(). Parses the input log, and sets up and
     * stores Synoptic session state for refinement\coarsening.
     * 
     * @throws Exception
     */
    @Override
    public GWTPair<GWTInvariantSet, GWTGraph> parseUploadedLog(
            List<String> regExps, String partitionRegExp, String separatorRegExp)
            throws Exception {
        // Set up state.
        retrieveSessionState();

        // Retrieve HTTP session to access location of recent log file uploaded.
        // HttpServletRequest request = getThreadLocalRequest();
        // HttpSession session = request.getSession();

        // This session state attribute set from LogFileUploadServlet and
        // contains
        // path to log file saved on disk from client.
        if (session.getAttribute(logFileSessionAttribute) == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }

        // Absolute path to the uploaded file.
        String path = session.getAttribute(logFileSessionAttribute).toString();
        logger.info("Reading uploaded file from: " + path);

        String logFileContent = null;
        try {
            FileInputStream fileStream = new FileInputStream(path);
            BufferedInputStream bufferedStream = new BufferedInputStream(
                    fileStream);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(bufferedStream));

            // Build string containing contents within file
            StringBuilder buildLog = new StringBuilder();
            String checkLine;
            while ((checkLine = bufferedReader.readLine()) != null) {
                buildLog.append(checkLine);
                buildLog.append("\n");
            }
            fileStream.close();
            bufferedStream.close();
            bufferedReader.close();
            logFileContent = buildLog.toString();
        } catch (Exception e) {
            throw new Exception("Unable to read uploaded file.");
        }
        return parseLog(logFileContent, regExps, partitionRegExp,
                separatorRegExp);
    }

    /**
     * Restarts refinement by re-creating the partition graph with the active
     * invariants that the user selected.
     * 
     * @throws Exception
     */
    @Override
    public GWTGraph commitInvariants(Set<Integer> activeInvsHashes)
            throws Exception {
        // Set up current state.
        retrieveSessionState();

        activeInvs.clear();
        // Get the actual set of invariants to be removed.
        for (ITemporalInvariant inv : minedInvs) {
            if (activeInvsHashes.contains(inv.hashCode())) {
                activeInvs.add(inv);
            }
        }

        initializeRefinementState(new TemporalInvariantSet(activeInvs));
        storeSessionState();

        return PGraphToGWTGraph(pGraph);
    }

    /**
     * Performs a single step of refinement on the cached model.
     * 
     * @throws Exception
     */
    @Override
    public GWTGraphDelta refineOneStep() throws Exception {
        // Set up state.
        retrieveSessionState();

        if (counterExampleTraces == null) {
            // We do not need to perform refinement.
            return null;
        }
        assert (counterExampleTraces.size() > 0);

        // Perform a single refinement step.
        numSplitSteps = Bisimulation.performOneSplitPartitionsStep(
                numSplitSteps, pGraph, counterExampleTraces);

        // Recompute the counter-examples for the unsatisfied invariants.
        counterExampleTraces = new TemporalInvariantSet(unsatInvs)
                .getAllCounterExamples(pGraph);

        unsatInvs.clear();
        if (counterExampleTraces != null) {
            for (CExamplePath<Partition> relPath : counterExampleTraces) {
                unsatInvs.add(relPath.invariant);
            }
        }
        PartitionMultiSplit last = pGraph.getMostRecentSplit();

        GWTNode refinedNode = new GWTNode(last.getPartition().getEType()
                .toString(), last.getPartition().hashCode());

        // Because we've created new objects on top of older objects we need to
        // store the state explicitly.
        storeSessionState();

        // Return the new model.
        return new GWTGraphDelta(PGraphToGWTGraph(pGraph), refinedNode,
                TemporalInvariantSetToGWTInvariants(unsatInvs));

    }

    /**
     * Performs coarsening of the completely refined model in one single step.
     */
    @Override
    public GWTGraph coarsenOneStep() throws Exception {
        // Set up state.
        retrieveSessionState();

        if (unsatInvs.size() != 0) {
            return null;
        }

        Bisimulation.mergePartitions(pGraph);
        return PGraphToGWTGraph(pGraph);
    }

    /**
     * Completes any refinement left to be done and then coarsens the graph into
     * a final model.
     */
    @Override
    public GWTGraph getFinalModel() throws Exception {
        // Set up state.
        retrieveSessionState();

        // Refine.
        Bisimulation.splitPartitions(pGraph);
        unsatInvs.clear();

        // Coarsen.
        Bisimulation.mergePartitions(pGraph);
        return PGraphToGWTGraph(pGraph);
    }

    /**
     * Find the requested partition and returns a list of log lines, each in the
     * form [line #, line, filename]
     */
    @Override
    public List<LogLine> handleLogRequest(int nodeID) throws Exception {
        // Set up state.
        retrieveSessionState();

        // Find partition
        Partition requested = null;
        for (Partition p : pGraph.getNodes()) {
            if (p.hashCode() == nodeID) {
                requested = p;
                break;
            }
        }

        // Fetch log lines
        List<LogLine> validLines = new ArrayList<LogLine>();
        if (requested != null) {
            for (EventNode event : requested.getEventNodes()) {
                validLines.add(new LogLine(event.getLineNum(), event.getLine(),
                        event.getShortFileName()));
            }
        }

        return validLines;
    }

    /**
     * Exports the model to a dot file and returns the dot fileName.
     */
    private String exportModelToDot() throws Exception {
        Calendar now = Calendar.getInstance();
        // Naming convention for the file can be improved
        String fileName = now.getTimeInMillis() + ".model.dot";
        String filePath = config.modelExportsDir + fileName;
        GraphExporter.exportGraph(filePath, pGraph, true);
        return fileName;
    }

    /**
     * Exports the current model as a .dot file. Returns the URL where the
     * generated file may be accessed by a client.
     */
    @Override
    public String exportDot() throws Exception {
        retrieveSessionState();
        String fileName = exportModelToDot();
        return config.modelExportsURLprefix + fileName;
    }

    /**
     * Exports the current model as a .png file. Returns the URL where the
     * generated file may be accessed by a client.
     */
    @Override
    public String exportPng() throws Exception {
        retrieveSessionState();
        String fileName = exportModelToDot();
        GraphExporter.generatePngFileFromDotFile(config.modelExportsDir
                + fileName);
        return config.modelExportsURLprefix + fileName + ".png";
    }
}
