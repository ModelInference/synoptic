package synoptic.main.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import synoptic.main.AbstractMain;
import synoptic.main.options.AbstractOptions;
import synoptic.main.options.Options;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Relation;
import synoptic.model.TraceGraph;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.state.State;
import synoptic.util.InternalSynopticException;
import synoptic.util.matching.NamedMatcher;
import synoptic.util.matching.NamedPattern;
import synoptic.util.matching.NamedSubstitution;
import synoptic.util.resource.AbstractResource;
import synoptic.util.resource.DTotalResource;
import synoptic.util.resource.EqualVectorTimestampsException;
import synoptic.util.resource.ITotalResource;
import synoptic.util.resource.LTotalResource;
import synoptic.util.resource.NotComparableVectorsException;
import synoptic.util.resource.VectorTime;

/**
 * A generic trace parser, configured in terms of Java 7 style named capture
 * regular expressions.
 */
public class TraceParser {

    private static Logger logger = Logger.getLogger("Parser Logger");

    private final List<NamedPattern> parsers;
    private final List<LinkedHashMap<String, NamedSubstitution>> constantFields;
    private final List<Map<String, Boolean>> incrementors;

    private static int nextTraceID;
    private final Map<String, Integer> partitionNameToTraceID;

    private NamedSubstitution filter;

    // Partitioning based on filter expressions -- maps a unique partition
    // string to a set of parsed events corresponding to that partition.
    Map<String, ArrayList<EventNode>> partitions = new LinkedHashMap<String, ArrayList<EventNode>>();

    // EventNode -> Relation associated with this event node.
    Map<EventNode, Set<Relation>> allEventRelations = new HashMap<EventNode, Set<Relation>>();

    // Patterns used to pre-process regular expressions
    private static final Pattern matchEscapedSeparator = Pattern
            .compile("\\\\;\\\\;");
    private static final Pattern matchAssign = Pattern
            .compile("\\(\\?<(\\w*)=>([^\\)]*)\\)");
    private static final Pattern matchPreIncrement = Pattern
            .compile("\\(\\?<\\+\\+(\\w*)>\\)");
    private static final Pattern matchPostIncrement = Pattern
            .compile("\\(\\?<(\\w*)\\+\\+>\\)");
    private static final Pattern matchDefault = Pattern
            .compile("\\(\\?<((\\w|\\*|\\-)*)>\\)");

    // Pattern used to parse dates
    private SimpleDateFormat dateFormatter = null;

    // All line-matching regexps will be checked to include either of the
    // following
    // groups, but not both.
    // Event type group
    private static final String typeGroup = "TYPE";
    // State group, must be in the form id=value,...,id=value
    private static final String stateGroup = "STATE";

    // Regexp groups that represent valid time in a log line:
    // TIME: integer time (e.g. 123) -- 32 bits
    // LTIME: long time (e.g. 123) -- 64 bits
    // VTIME: vector clock time (e.g. [12,23,34], and [12,234])
    // FTIME: float time (e.g. 123.456) -- 32 bits
    // DTIME: double time (e.g. 1234.56) -- 64 bits
    // DATETIME: date time parsed according to the dateFormatter option and
    // converted to LTIME -- 64 bits
    public static final List<String> validTimeGroups = Arrays.asList("TIME",
            "LTIME", "VTIME", "FTIME", "DTIME", "DATETIME");

    // Regexp group representing multiple relations
    private static final String relationGroup = "RELATION";
    private static final String namedRelationGroup = "RELATION-";

    // Regexp group representing closure relations, call and return for now.
    private static final String closureRelationGroup = "RELATION*";
    private static final String namedclosureRelationGroup = "RELATION*-";

    // A group that is used to capture the process ID in a PO log -- can only be
    // used in conjunction with VTIME, but is optional. However, if it used in
    // any reg-exp with VTIME then all VTIME reg-exps must use/set it.
    private static final String processIDGroup = "PID";

    // If selectedTimeGroup is "VTIME" then whether or not processIDGroup is
    // specified (true), or if process IDs will be implicitly mined (false)
    private boolean parsePIDs = false;

    // The time we use implicitly. LOGTIME is log-line-number time. Which exists
    // implicitly for every log-line.
    private static final String implicitTimeGroup = "LOGTIME";

    // Regexp groups that represent totally ordered time.
    private static final List<String> totallyOrderedTimeGroups = Arrays.asList(
            "LOGTIME", "LTIME", "TIME", "FTIME", "DTIME", "DATETIME");

    // The time group regexp selected (implicitly) for use by this parser via
    // passed reg exps to match lines. The parser allows only one type of time
    // to be used.
    private String selectedTimeGroup = null;

    // Used to convert delta values into absolute values when the flag is set
    private AbstractResource lastTime;

    private static final String dummyEtypeLabel = "dummy-etype-for-line-with-state"
            .intern();

    /**
     * Returns an un-parameterized trace parser.
     */
    public TraceParser() {
        parsers = new ArrayList<NamedPattern>();
        constantFields = new ArrayList<LinkedHashMap<String, NamedSubstitution>>();
        incrementors = new ArrayList<Map<String, Boolean>>();
        filter = new NamedSubstitution("");
        nextTraceID = 0;
        partitionNameToTraceID = new LinkedHashMap<String, Integer>();
    }

    /**
     * Initializes and returns a new trace parser that is parameterized with
     * regular expressions for parsing log lines/partitions/separators.
     * 
     * @param rExps
     *            list of regular expressions for matching log lines
     * @param partitioningRegExp
     *            a partitioning regular expression -- used to map parsed event
     *            instances to executions
     * @param sepRegExp
     *            a separator regular expression -- used to split the sequence
     *            of parsed event instances into executions
     * @return new trace parser
     * @throws ParseException
     */
    public TraceParser(List<String> rExps, String partitioningRegExp,
            String sepRegExp, String dateFormat) throws ParseException {
        this();

        assert (rExps != null);
        assert (partitioningRegExp != null);

        logger.fine("Setting up the log file parser.");
        if (partitioningRegExp.equals(AbstractOptions.partitionRegExpDefault)) {
            logger.info("Using the default partitions mapping regex: "
                    + AbstractOptions.partitionRegExpDefault);
        }

        if (!rExps.isEmpty()) {
            // The user provided custom regular expressions.
            for (String exp : rExps) {
                logger.fine("\taddRegex with exp:" + exp);
                this.addRegex(exp);
            }

            this.setPartitionsMap(partitioningRegExp);
        } else {
            // No custom regular expressions provided - warn and use defaults.
            logger.warning("Using a default regular expression to parse log-lines: "
                    + "will map the entire log line to an event type."
                    + "\nTo use a custom regular expressions use the option:\n\t"
                    + Options.getOptDesc("regExps",
                            AbstractOptions.plumeOpts.getClass()) + "\n\t");
            // TODO: is this next statement necessary?
            // parser.addRegex("^\\s*$(?<SEPCOUNT++>)");
            this.addRegex(AbstractOptions.regExpDefault);
            this.setPartitionsMap(partitioningRegExp);
        }

        if (sepRegExp != null) {
            this.addPartitionsSeparator(sepRegExp);
            if (!partitioningRegExp
                    .equals(AbstractOptions.partitionRegExpDefault)) {
                logger.warning("Partition separator and partition mapping regex are both specified. This may result in difficult to understand parsing behavior.");
            }
        }

        if (dateFormat != null) {
            try {
                dateFormatter = new SimpleDateFormat(dateFormat);
            } catch (Exception e) {
                String error = "Date parsing format (" + dateFormat
                        + ") is not well defined";
                logger.severe(error);
                throw new ParseException(error);
            }
        }
    }

    /**
     * Returns whether or not the time type used to parse the log(s) has a
     * canonical total order or not.
     * 
     * @return whether time type is totally ordered or not
     */
    public boolean logTimeTypeIsTotallyOrdered() {
        return totallyOrderedTimeGroups.contains(selectedTimeGroup);
    }

    /**
     * Specifies a specific date format for parsing DATETIME capture groups.
     * 
     * @param dateFormat
     * @throws ParseException
     */
    public void addDateFormat(String dateFormat) throws ParseException {
        if (dateFormat != null) {
            try {
                dateFormatter = new SimpleDateFormat(dateFormat);
            } catch (Exception e) {
                String error = "Date parsing format (" + dateFormat
                        + ") is not well defined";
                logger.severe(error);
                throw new ParseException(error);
            }
        }
    }

    /**
     * Adds an individual trace line type, which consists of a regex with
     * additional syntax. <b>The regex must match the entire line.</b> The
     * additional syntax is as follows:
     * <ul>
     * <li>(?<name>) -- Matches the default field regex (?:\s*(?<name>\S+)\s*)</li>
     * <li>(?<name=>value) -- Specifies a value for a field, potentially with
     * back-references which get filled.</li>
     * <li>(?<name++>) and (?<++name>) -- Specify context fields which are
     * included with every type of trace.
     * </ul>
     * Note that \;\; becomes ;; (this is to support the parsing of multiple
     * regexes, described above).
     * 
     * @param input_regex
     *            Regular expression of the form described.
     * @throws ParseException
     *             When the input_regex can't be compiled
     */
    public void addRegex(String input_regex) throws ParseException {
        // TODO: this method for splitting is ugly, but it works for now
        // In order to use ";;" in a regex, escape as \;\;
        // TODO: document this on the wiki
        if (input_regex == null) {
            throw new ParseException("Regular expression cannot be null.");
        }

        logger.fine("entering addRegex with: " + input_regex);
        String regex = matchEscapedSeparator.matcher(input_regex).replaceAll(
                ";;");

        // Parse out all of the constants.
        Matcher matcher = matchAssign.matcher(regex);

        // Maintains a map between fields and their values in the regex.
        LinkedHashMap<String, NamedSubstitution> cmap = new LinkedHashMap<String, NamedSubstitution>();

        // A set of all the fields that were assigned in the regex.
        Set<String> fields = new LinkedHashSet<String>();

        // Indicates whether this regexp sets the HIDE field to true or not.
        boolean isHidden = false;
        while (matcher.find()) {
            String field = matcher.group(1);
            String value = matcher.group(2);

            if (fields.contains(field)) {
                String error = "The field: " + field
                        + " already appears in regex: " + regex;
                logger.severe(error);
                ParseException parseException = new ParseException(error);
                parseException.setRegex(regex);
                throw parseException;
            }
            fields.add(field);

            cmap.put(field, new NamedSubstitution(value));
            logger.fine("matchAssign: " + field + " -> " + matcher.group(2));
            // Prevent the user from adding regexes that modify the parsing of
            // special time fields.
            if (validTimeGroups.contains(field)) {
                String error = "Cannot assign custom regex expressions to parse time field "
                        + field + " in regex: " + input_regex;
                logger.severe(error);
                ParseException parseException = new ParseException(error);
                parseException.setRegex(input_regex);
                throw parseException;
            }

            if (implicitTimeGroup.equals(field)) {
                String error = "The group " + implicitTimeGroup
                        + " cannot be used explicitly as a capture group.";
                logger.severe(error);
                throw new ParseException(error);
            }

            // HIDE groups can only be assigned to 'true'
            if (field.equals("HIDE")) {
                if (!value.equals("true")) {
                    String error = "HIDE field cannot be assigned to: " + value
                            + ", it can only be assigned to 'true' in regex: "
                            + input_regex;
                    logger.severe(error);
                    ParseException parseException = new ParseException(error);
                    parseException.setRegex(input_regex);
                    throw parseException;
                }
                isHidden = true;
            }
        }

        constantFields.add(parsers.size(), cmap);

        // Remove the constant fields from the regex.
        regex = matcher.replaceAll("");

        // Parse out all of the incrementors.
        matcher = matchPreIncrement.matcher(regex);
        Map<String, Boolean> incMap = new LinkedHashMap<String, Boolean>();
        while (matcher.find()) {
            incMap.put(matcher.group(1), false);
        }
        regex = matcher.replaceAll("");
        matcher = matchPostIncrement.matcher(regex);
        while (matcher.find()) {
            incMap.put(matcher.group(1), true);
        }
        regex = matcher.replaceAll("");
        incrementors.add(incMap);

        // Replace fields which lack regex content with default matching
        // pattern.
        // TODO: Different defaults for some special fields.
        // TODO: document defaults on the wiki
        matcher = matchDefault.matcher(regex);
        StringBuffer newRegex = new StringBuffer();
        boolean isFirst = true;
        while (matcher.find()) {
            if (isFirst) {
                matcher.appendReplacement(newRegex,
                        "(?:\\\\s*(?<$1>\\\\S+)\\\\s*)");
                isFirst = false;
            } else {
                matcher.appendReplacement(newRegex,
                        "(?:\\\\s+(?<$1>\\\\S+)\\\\s*)");
            }
        }
        matcher.appendTail(newRegex);
        regex = newRegex.toString();

        NamedPattern parser = null;
        try {
            parser = NamedPattern.compile(regex);
        } catch (Exception e) {
            String error = "Error parsing named-captures in " + input_regex
                    + ":";
            logger.severe(error);
            logger.severe(e.toString());
            ParseException parseException = new ParseException(error + " "
                    + e.getMessage());
            parseException.setRegex(input_regex);
            throw parseException;
        }
        parsers.add(parser);
        // A list of all named groups i.e., fields that have no assignment.
        List<String> groups = parser.groupNames();

        // Check that special/internal field names do not appear.
        // Currently this is just LOGTIME.
        for (String group : groups) {
            if (implicitTimeGroup.equals(group)) {
                String error = "The group " + implicitTimeGroup
                        + " cannot be used explicitly as a capture group.";
                logger.severe(error);
                throw new ParseException(error);
            }
        }

        // Process non-hidden expression specially, since these expressions are
        // supposed to generate event instances, while the hidden ones do not.
        if (!isHidden) {
            // Check that either type or state group is present, but not both.
            boolean typePresent = groups.contains(typeGroup)
                    || fields.contains(typeGroup);
            boolean statePresent = groups.contains(stateGroup)
                    || fields.contains(stateGroup);
            if (typePresent == statePresent) {
                String error = "Regular expression: " + input_regex
                        + " should contain either a " + typeGroup
                        + " named group" + " or a " + stateGroup
                        + " named group, but not both";
                logger.severe(error);
                ParseException parseException = new ParseException(error);
                parseException.setRegex(input_regex);
                throw parseException;
            }

            // Whether or not the PID group appears in this expressions.
            boolean usingPID = false;

            // We have two cases for specifying time types:
            // (1) Implicit: type is not specified and we use implicitTimeGroup.
            // (2) Explicit: exactly one kind of type is specified in the regex.
            String regexTimeUsed = null;
            for (String group : groups) {
                if (processIDGroup.equals(group)) {
                    usingPID = true;
                }

                // logger.info("group is : " + group);

                if (validTimeGroups.contains(group)) {
                    if (regexTimeUsed != null) {
                        String error = "The regex: " + input_regex
                                + " contains multiple time field definitions: "
                                + group + ", " + regexTimeUsed;
                        logger.severe(error);
                        ParseException parseException = new ParseException(
                                error);
                        parseException.setRegex(input_regex);
                        throw parseException;
                    }
                    regexTimeUsed = group;
                }
            }

            if (regexTimeUsed == null) {
                regexTimeUsed = implicitTimeGroup;
            }

            if (selectedTimeGroup == null) {
                // No prior time type was specified. So we use regex's type as
                // the time type.
                selectedTimeGroup = regexTimeUsed;
                if (selectedTimeGroup.equals("VTIME")) {
                    parsePIDs = usingPID;
                } else {
                    if (usingPID) {
                        String error = "The PID group name can only be used with a VTIME time group.";
                        logger.severe(error);
                        throw new ParseException(error);
                    }
                }
            } else {
                // Prior time type was used, make sure that it matches regex's.
                if (!selectedTimeGroup.equals(regexTimeUsed)) {
                    String error = "Time type cannot vary. A prior regex used the type "
                            + selectedTimeGroup
                            + ", while regex "
                            + input_regex + " uses the type " + regexTimeUsed;
                    logger.severe(error);
                    ParseException parseException = new ParseException(error);
                    parseException.setRegex(input_regex);
                    throw parseException;
                }

                if (regexTimeUsed.equals("VTIME") && parsePIDs != usingPID) {
                    String error = "Either all or none of the VTIME-parsing reg-exps must specify the PID group.";
                    logger.severe(error);
                    throw new ParseException(error);
                }
            }

            for (String group : groups) {
                if (group.startsWith(relationGroup)) {

                    // Check to see if relation capture group strings are
                    // well-formed
                    Pattern relation = Pattern.compile("RELATION\\*?(-\\w*)?");
                    Matcher fieldMatcher = relation.matcher(group);
                    if (!fieldMatcher.matches()) {
                        String error = "Relation field: " + group
                                + " is malformed."
                                + "Accepts: RELATION*?(-\\w*)?";
                        logger.severe(error);
                        throw new ParseException(error);
                    }

                    // Check if VTIME is used with relation
                    if (selectedTimeGroup.equals("VTIME")) {
                        String error = "RELATION and VTIME groups cannot be mixed since multiple"
                                + "relations requires a totally ordered log.";
                        logger.severe(error);
                        throw new ParseException(error);
                    }
                }
            }

        }

        if (AbstractMain.getInstance().options.debugParse) {
            logger.info("input: " + input_regex);
            logger.info("processed: " + regex);
            logger.info("standard: " + parser.standardPattern());
            if (!groups.isEmpty()) {
                logger.info("\tgroups: " + groups.toString());
            }
            if (!cmap.isEmpty()) {
                logger.info("\tfields: " + cmap.toString());
            }
            if (!incMap.isEmpty()) {
                logger.info("\tincs: " + incMap.toString());
            }
        }
    }

    /**
     * Moves the last element in the list to the front of the list.
     * 
     * @param <T>
     *            list element type
     * @param l
     *            list
     */
    private static <T> void cycle(List<T> l) {
        T removed = l.remove(l.size() - 1);
        l.add(0, removed);
    }

    /**
     * Create a separator-granularity match. This works by creating an
     * incrementing variable (on separator match), and adding SEPCOUNT to the
     * granularity filter.
     * 
     * @throws InternalSynopticException
     *             On internal error: wrong internal separator reg-exp
     */
    public void addPartitionsSeparator(String regex)
            throws InternalSynopticException {
        try {
            addRegex(regex + "(?<SEPCOUNT++>)(?<HIDE=>true)");
        } catch (ParseException e) {
            InternalSynopticException internalSynopticException = InternalSynopticException
                    .wrap(e);
            internalSynopticException.setRegex(e.getRegex());
            throw internalSynopticException;
        }
        cycle(parsers);
        cycle(incrementors);
        cycle(constantFields);
        filter.concat(new NamedSubstitution("\\k<SEPCOUNT>"));
    }

    /**
     * Sets the partitioning filter, to the passed, back-reference containing
     * string.
     */
    public void setPartitionsMap(String f) {
        filter = new NamedSubstitution(f);
    }

    /**
     * Parses a trace file into a list of log events.
     * 
     * @param file
     *            File to read and then parse.
     * @param linesToRead
     *            Bound on the number of lines to read. Negatives indicate
     *            unbounded.
     * @return The parsed log events.
     * @throws ParseException
     *             when user supplied expressions are the problem
     * @throws InternalSynopticException
     *             when Synoptic code is the problem
     */
    public ArrayList<EventNode> parseTraceFile(File file, int linesToRead)
            throws ParseException, InternalSynopticException {
        String fileName = "";
        try {
            fileName = file.getAbsolutePath();
            FileInputStream fstream = new FileInputStream(file);
            InputStreamReader fileReader = new InputStreamReader(fstream);
            return parseTrace(fileReader, fileName, linesToRead);
        } catch (IOException e) {
            String error = "Error while attempting to read log file ["
                    + fileName + "]: " + e.getMessage();
            logger.severe(error);
            throw new ParseException(error);
        }
    }

    /**
     * Parses a string containing a log into a list of log events.
     * 
     * @param trace
     *            The trace, with lines separated by newlines.
     * @param traceName
     *            The name for this trace -- maps to the FILE parse group.
     * @param linesToRead
     *            Bound on the number of lines to read. Negatives indicate
     *            unbounded.
     * @return The parsed log events.
     * @throws ParseException
     *             when user supplied expressions are the problem
     * @throws InternalSynopticException
     *             when Synoptic code is the problem
     */
    public ArrayList<EventNode> parseTraceString(String trace,
            String traceName, int linesToRead) throws ParseException {
        if (trace == null) {
            throw new ParseException("Trace string cannot be null.");
        }
        if (traceName == null) {
            throw new ParseException("Trace name string cannot be null.");
        }
        StringReader stringReader = new StringReader(trace);
        try {
            return parseTrace(stringReader, traceName, linesToRead);
        } catch (IOException e) {
            String error = "Error while reading string [" + traceName + "]: "
                    + e.getMessage();
            logger.severe(error);
            throw new ParseException(error);
        }
    }

    /**
     * Parses strings generated by traceReader as event instances.
     * 
     * @param traceReader
     *            reader generated lines to parse.
     * @param linesToRead
     *            Bound on the number of lines to read. Negatives indicate
     *            unbounded.
     * @return The parsed log events.
     * @throws IOException
     *             when the reader we're using is the problem
     * @throws ParseException
     *             when user supplied expressions are the problem
     * @throws InternalSynopticException
     *             when Synoptic code is the problem
     */
    private ArrayList<EventNode> parseTrace(Reader traceReader,
            String traceName, int linesToRead) throws ParseException,
            IOException, InternalSynopticException {
        BufferedReader br = new BufferedReader(traceReader);

        // Initialize incrementor context.
        Map<String, Integer> context = new LinkedHashMap<String, Integer>();
        for (Map<String, Boolean> incs : incrementors) {
            for (String incField : incs.keySet()) {
                context.put(incField, 0);
            }
        }

        ArrayList<EventNode> results = new ArrayList<EventNode>();
        String strLine = null;

        String tName = traceName;
        if (AbstractMain.getInstance().options.internCommonStrings) {
            tName = tName.intern();
        }

        lastTime = null;

        int lineNum = 0;
        // Process each line in sequence.
        while ((strLine = br.readLine()) != null) {
            if (results.size() == linesToRead) {
                break;
            }
            lineNum++;
            EventNode node = parseLine(strLine, tName, context, lineNum);
            if (node == null) {
                continue;
            }
            results.add(node);
        }
        br.close();
        // TODO: this is a hacky solution, should refactor the parseTrace and
        // parseLine methods so that State is separated from EventNode.
        // At this point, each node in results either represents an event or
        // a state. We need to bundle pre- and post-event states and events.
        if (AbstractMain.getInstance().options.stateProcessing) {
            mergeStatesWithEventNodes(results);
        }

        if (selectedTimeGroup.equals("VTIME") && !parsePIDs) {
            // Infer the PID (process ID) corresponding to each of the parsed
            // events, if PIDs were not parsed explicitly from the trace.

            for (List<EventNode> group : partitions.values()) {
                // A list in which the list at index j is a (totally ordered)
                // list of events that occurred at node j.
                List<List<EventNode>> listsNodeEvents;
                try {
                    // Perform the inference.
                    listsNodeEvents = VectorTime.mapLogEventsToNodes(group);
                } catch (Exception e) {
                    String error = "Could not match vector times to host id.";
                    logger.severe(error);
                    throw new ParseException(error);
                }

                int pid = 0;
                // Assign a pid to each of the event nodes.
                for (List<EventNode> nodeEvents : listsNodeEvents) {
                    for (EventNode eNode : nodeEvents) {
                        if (!(eNode.getEType() instanceof DistEventType)) {
                            String error = "Parsed a non dist. event type for a trace with VTIME format.";
                            logger.severe(error);
                            throw new ParseException(error);
                        }
                        ((DistEventType) eNode.getEType())
                                .setProcessName(Integer.toString(pid));
                    }
                    pid += 1;
                }
            }
        } else if (selectedTimeGroup.equals("VTIME") && parsePIDs) {
            // Check that for each partition, the set of events corresponding to
            // a PID can be totally ordered -- this is a critical property of a
            // PID.

            for (List<EventNode> group : partitions.values()) {
                // Determine the set of unique PIDs in this partition.
                LinkedHashSet<String> PIDs = new LinkedHashSet<String>();
                for (EventNode node : group) {
                    if (!(node.getEType() instanceof DistEventType)) {
                        String error = "Parsed a non dist. event type for a trace with VTIME format.";
                        logger.severe(error);
                        throw new ParseException(error);
                    }
                    PIDs.add(((DistEventType) node.getEType()).getProcessName());
                }

                LinkedList<EventNode> pidEvents = new LinkedList<EventNode>();
                for (String pid : PIDs) {
                    // Select all events from the partition with the same PID
                    for (EventNode node : group) {
                        if (((DistEventType) node.getEType()).getProcessName()
                                .equals(pid)) {
                            pidEvents.add(node);
                        }
                    }

                    // Now walk through all pidEvents and remove the minimal
                    // element -- all elements should be comparable, otherwise
                    // we've violated the property we're checking.
                    while (pidEvents.size() != 0) {
                        EventNode minElement = pidEvents.get(0);
                        for (EventNode node : pidEvents) {
                            if (node == minElement) {
                                continue;
                            }
                            if (node.getTime().lessThan(minElement.getTime())) {
                                minElement = node;
                            } else {
                                if (!minElement.getTime().lessThan(
                                        node.getTime())) {
                                    String error = "Two events in the same partition with same PID["
                                            + pid
                                            + "] have incomparable VTIMEs: \n"
                                            + "\t"
                                            + minElement.toString()
                                            + ": "
                                            + minElement.getTime().toString()
                                            + "\n\t"
                                            + node.toString()
                                            + ": "
                                            + node.getTime().toString();
                                    logger.severe(error);
                                    throw new ParseException(error);
                                }
                            }
                        }
                        pidEvents.remove(minElement);
                    }
                }
            }
        }

        logger.info("Successfully parsed " + partitions.size()
                + " traces, containing a total of " + results.size()
                + " events from [" + tName + "]");
        return results;
    }

    /**
     * Merge each state node in results with its surrounding event nodes of the
     * same traceID.
     * 
     * @throws ParseException
     */
    private void mergeStatesWithEventNodes(List<EventNode> results)
            throws ParseException {
        int i = 0;
        while (i < results.size()) {
            EventNode node = results.get(i);
            State state = node.getPostEventState();
            if (state != null) {
                // This node represents a state, not an event.
                // Merge this node with surrounding event nodes of the same
                // traceID.
                // Assumptions:
                // (1) A trace cannot have 2 consecutive states.
                // (2) If a trace contains a state, it also contains at least 1
                // event.
                EventNode prevNode = i > 0 ? results.get(i - 1) : null;
                EventNode nextNode = i < results.size() - 1 ? results
                        .get(i + 1) : null;
                int traceID = node.getTraceID();
                boolean mergeFront = false;
                boolean mergeBack = false;
                if (prevNode != null && prevNode.getTraceID() == traceID) {
                    if (prevNode.getPostEventState() != null) {
                        // This trace has 2 consecutive states.
                        throw new ParseException("Found 2 consecutive states: "
                                + prevNode.getPostEventState() + " and "
                                + state + " in trace ID: " + traceID);
                    }
                    prevNode.setPostEventState(state);
                    mergeFront = true;
                }
                if (nextNode != null && nextNode.getTraceID() == traceID) {
                    nextNode.setPreEventState(state);
                    mergeBack = true;
                }
                if (!mergeFront && !mergeBack) {
                    throw new ParseException("Trace ID: " + traceID
                            + " contains a state: " + state
                            + " but does not contain any event");
                }
                results.remove(i);
            } else {
                // This node represents an event -- do nothing.
                i++;
            }
        }
    }

    /**
     * Builds a generic string to describe a location of an error on a line in
     * some input file.
     */
    private String buildLineErrorLocString(String line, String fileName,
            int lineNum) {
        // Basic error location string is simply the line number.
        String ret = "Line #" + Integer.toString(lineNum);
        if (!fileName.equals("")) {
            // Include filename if it is not blank.
            ret += " from file [" + fileName + "]";
        }

        AbstractMain main = AbstractMain.getInstance();
        if (main.options.logLvlVerbose || main.options.logLvlExtraVerbose) {
            // Include the actual line if verbose output is desired.
            return ret + " line [" + line + "]";
        }
        return ret;
    }

    /**
     * Parse an individual line.
     */
    private EventNode parseLine(String line, String fileName,
            Map<String, Integer> context, int lineNum) throws ParseException,
            InternalSynopticException {

        Event event = null;
        AbstractResource nextTime = null;

        AbstractMain main = AbstractMain.getInstance();

        for (int i = 0; i < parsers.size(); i++) {
            NamedMatcher matcher = parsers.get(i).matcher(line);
            if (!matcher.matches()) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, NamedSubstitution> cs = (Map<String, NamedSubstitution>) constantFields
                    .get(i).clone();
            Map<String, String> matched = matcher.toMatchResult().namedGroups();

            // Perform pre-increments.
            for (Map.Entry<String, Boolean> inc : incrementors.get(i)
                    .entrySet()) {
                if (inc.getValue() == false) {
                    context.put(inc.getKey(), context.get(inc.getKey()) + 1);
                }
            }

            // Overlay increment context.
            for (Map.Entry<String, Integer> entry : context.entrySet()) {
                matched.put(entry.getKey(), entry.getValue().toString());
            }

            for (Map.Entry<String, NamedSubstitution> entry : cs.entrySet()) {
                // Process the constant field by substituting
                // back-references.
                String key = entry.getKey();
                String val = entry.getValue().substitute(matched);

                // Special case for integers, to allow for setting
                // incrementors.
                Integer parsed = Integer.getInteger(val, Integer.MIN_VALUE);
                if (context.containsKey(key)) {
                    context.put(key, parsed);
                }

                // TODO: Determine policy of constant fields vs. extracted have
                // overlay priority
                if (!matched.containsKey(key)) {
                    matched.put(key, val);
                }
            }

            if (matched.get("HIDE") != null) {
                // Perform post-increments and exit.
                for (Map.Entry<String, Boolean> inc : incrementors.get(i)
                        .entrySet()) {
                    if (inc.getValue() == true) {
                        context.put(inc.getKey(), context.get(inc.getKey()) + 1);
                    }
                }
                return null;
            }

            // ////////////
            // Only non-hidden regexes from this point on.

            String eTypeLabel;
            EventType eType;
            // Check if this line contains event type or state.
            if (matched.containsKey(typeGroup)) {
                if (main.options.internCommonStrings) {
                    eTypeLabel = matched.get(typeGroup).intern();
                } else {
                    eTypeLabel = matched.get(typeGroup);
                }
            } else if (matched.containsKey(stateGroup)) {
                // This line has state, so event type is irrelevant.
                // Use use the dummy string as the type.
                eTypeLabel = dummyEtypeLabel;
            } else {
                // TODO: determine if this is desired + print warning
                // In the absence of an event type, use the entire log line as
                // the type.
                eTypeLabel = line;
            }

            if (selectedTimeGroup.equals("VTIME")) {
                if (parsePIDs) {
                    eType = new DistEventType(eTypeLabel,
                            matched.get(processIDGroup));
                } else {
                    eType = new DistEventType(eTypeLabel);
                }
                event = new Event(eType, line, fileName, lineNum);
            } else {
                eType = new StringEventType(eTypeLabel);
                event = new Event(eType, line, fileName, lineNum);
            }

            /*
             * Tag event nodes with relation fields. This is gross, is there a
             * nicer way to represent a state machine?
             */
            Set<String> relationValues = new HashSet<String>();
            Set<Relation> eventRelations = new HashSet<Relation>();
            for (String key : matched.keySet()) {
                if (key.startsWith(relationGroup)) {
                    String relationString = matched.get(key);

                    if (relationValues.contains(relationString)) {
                        throw new ParseException(
                                "Duplicate captured relation value: "
                                        + relationString);
                    }

                    relationValues.add(relationString);

                    String relName = Relation.anonName;
                    boolean isClosure = false;

                    if (key.startsWith(closureRelationGroup)) {
                        isClosure = true;

                        if (key.startsWith(namedclosureRelationGroup)) {
                            relName = key.substring(namedclosureRelationGroup
                                    .length());
                        }

                    } else if (key.startsWith(namedRelationGroup)) {
                        relName = key.substring(namedRelationGroup.length());
                    }

                    Relation relation = new Relation(relName, relationString,
                            isClosure);
                    eventRelations.add(relation);
                }
            }

            // We have two cases for processing time on log lines:
            // (1) Implicitly: no matched field is a time field because it is
            // set to implicitTimeGroup. For this case we simply use the prior
            // time + 1 (log-line counting time).
            // (2) Explicitly: one of the matched fields must be a time type
            // field (set in addRegex). If no such match is found then we throw
            // an exception.
            if (selectedTimeGroup == implicitTimeGroup) {
                // Implicit case: LOGTIME
                nextTime = new ITotalResource(lineNum);
            } else {
                // Explicit case.
                String timeField = matched.get(selectedTimeGroup);
                if (timeField == null) {
                    String error = buildLineErrorLocString(line, fileName,
                            lineNum)
                            + " Unable to parse time type "
                            + selectedTimeGroup;
                    logger.severe(error);
                    ParseException parseException = new ParseException(error);
                    parseException.setLogLine(line);
                    throw parseException;
                }

                // Attempt to parse the time type field as a VectorTime -- we
                // use this type for all the current types of time.
                try {
                    if (selectedTimeGroup.equals("TIME")) {
                        int t = Integer.parseInt(timeField.trim());
                        nextTime = new ITotalResource(t);
                    } else if (selectedTimeGroup.equals("LTIME")) {
                        long t = Long.parseLong(timeField.trim());
                        nextTime = new LTotalResource(t);
                    } else if (selectedTimeGroup.equals("FTIME")
                            || selectedTimeGroup.equals("DTIME")) {
                        nextTime = new DTotalResource(timeField.trim());
                    } else if (selectedTimeGroup.equals("VTIME")) {
                        nextTime = new VectorTime(timeField.trim());
                    } else if (selectedTimeGroup.equals("DATETIME")) {
                        if (dateFormatter == null) {
                            String error = "Date formatter is not initialized with a format, cannot continue";
                            logger.severe(error);
                            throw new ParseException(error);
                        }
                        Date date = dateFormatter.parse(timeField.trim());
                        nextTime = new LTotalResource(date.getTime());
                    } else {
                        String error = buildLineErrorLocString(line, fileName,
                                lineNum)
                                + " Unable to recognize time type "
                                + selectedTimeGroup;
                        logger.severe(error);
                        throw new ParseException(error);
                    }

                    if (AbstractOptions.inputDelta) {
                        // If this is the first node, assign the zero time to
                        // lastTime
                        if (lastTime == null) {
                            lastTime = nextTime.getZeroResource();
                        }

                        nextTime = nextTime.incrBy(lastTime);
                        lastTime = nextTime;
                    }
                } catch (Exception e) {
                    String errMsg = buildLineErrorLocString(line, fileName,
                            lineNum)
                            + " Unable to parse time field on log line.";
                    if (main.options.ignoreNonMatchingLines) {
                        logger.warning(errMsg
                                + " Ignoring line and continuing.");
                        continue;
                    }
                    String error = errMsg
                            + "\n\tTry cmd line options:\n\t"
                            + Options.getOptDesc("ignoreNonMatchingLines",
                                    AbstractOptions.plumeOpts.getClass())
                            + "\n\t"
                            + Options.getOptDesc("debugParse",
                                    AbstractOptions.plumeOpts.getClass());
                    logger.severe(error);
                    logger.severe(e.toString());
                    ParseException parseException = new ParseException(errMsg);
                    parseException.setLogLine(line);
                    throw parseException;
                }
            }

            Map<String, String> eventStringArgs = new LinkedHashMap<String, String>();

            for (Map.Entry<String, String> group : matched.entrySet()) {
                String name = group.getKey();
                if (!name.equals("TYPE") && !name.equals("TIME")) {
                    eventStringArgs.put(name, group.getValue());
                }
            }

            if (AbstractOptions.partitionRegExp.equals("\\k<FILE>")) {
                // These logs are to be partitioned via file
                eventStringArgs.put("FILE", fileName);
                // "" + traceNameToTraceID.get(fileName));
            }

            // Perform post-increments.
            for (Map.Entry<String, Boolean> inc : incrementors.get(i)
                    .entrySet()) {
                if (inc.getValue() == true) {
                    context.put(inc.getKey(), context.get(inc.getKey()) + 1);
                }
            }

            if (main.options.debugParse) {
                // TODO: include partition name in the list of field values
                logger.info("input: " + line);
                StringBuilder msg = new StringBuilder("{");
                for (Map.Entry<String, String> entry : eventStringArgs
                        .entrySet()) {
                    if (entry.getKey().equals("FILE")) {
                        continue;
                    }
                    msg.append(entry.getKey() + " = " + entry.getValue() + ", ");
                }
                msg.append("TYPE = " + eType.toString());
                msg.append("}");
                logger.info(msg.toString());
            }
            event.setTime(nextTime);

            Relation timeRelation = new Relation("time-relation",
                    Event.defTimeRelationStr, false);
            eventRelations.add(timeRelation);

            String partitionName = filter.substitute(eventStringArgs);
            EventNode eventNode = addEventNodeToPartition(event, partitionName);

            // If a state is captured, save it to eventNode's post-event state
            // to indicate that this node represents a state and not an event.
            // Use post-event state because, when merging state nodes to event
            // nodes,
            // pre-event state might be set before we check if that node
            // represents
            // state or event.
            if (eventStringArgs.containsKey(stateGroup)) {
                String stateStr = eventStringArgs.get(stateGroup);
                State state = new State(stateStr);
                eventNode.setPostEventState(state);
                // State is parsed. Enable state processing.
                main.options.stateProcessing = true;
            }

            // We want to add eventNode->eventRelations to allEventRelations
            // ONLY IF eventNode actually represents an event, not a dummy
            // for state.
            if (!eventNode.getEType().getETypeLabel().equals(dummyEtypeLabel)) {
                if (!allEventRelations.containsKey(eventNode)) {
                    allEventRelations.put(eventNode, new HashSet<Relation>());
                }

                Set<Relation> relations = allEventRelations.get(eventNode);

                // Relations are immutable so we don't have to worry about
                // representation exposure.
                relations.addAll(eventRelations);
            }

            eventStringArgs = null;
            return eventNode;
        }

        if (main.options.recoverFromParseErrors) {
            logger.warning(buildLineErrorLocString(line, fileName, lineNum)
                    + " Failed to parse trace line. Using entire line as type.");
            event = new Event(new StringEventType(line), line, fileName,
                    lineNum);
            if (selectedTimeGroup.equals(implicitTimeGroup)) {
                // We can recover OK with log-line counting time.
                event.setTime(new ITotalResource(lineNum));
            } else {
                // We can't recover with vector time -- incrementing it simply
                // doesn't make sense.
                String error = buildLineErrorLocString(line, fileName, lineNum)
                        + " Unable to recover from parse error with vector-time type.";
                logger.severe(error);
                throw new ParseException(error);
            }

            EventNode eventNode = addEventNodeToPartition(event,
                    filter.substitute(new LinkedHashMap<String, String>()));
            return eventNode;

        } else if (main.options.ignoreNonMatchingLines) {
            logger.fine(buildLineErrorLocString(line, fileName, lineNum)
                    + " Failed to parse trace line. Ignoring line and continuing.");
            return null;
        }

        String exceptionError = buildLineErrorLocString(line, fileName, lineNum)
                + " does not match any of the provided regular expressions.";

        String loggerError = exceptionError
                + "\nTry cmd line options:\n\t"
                + Options.getOptDesc("ignoreNonMatchingLines",
                        AbstractOptions.plumeOpts.getClass())
                + "\n\t"
                + Options.getOptDesc("debugParse",
                        AbstractOptions.plumeOpts.getClass());

        logger.severe(loggerError);
        ParseException parseException = new ParseException(exceptionError);
        parseException.setLogLine(line);
        throw parseException;
    }

    /**
     * Adds an event to an internal map of partitions. NOTE: Only eventNodes
     * that actually represent events (i.e., not pseudo eventNodes that
     * represent states) can be added to the partitions. But, we create a
     * partition for every pName, since every partition contains at least 1
     * event.
     * 
     * @param eventNode
     * @param pName
     */
    private EventNode addEventNodeToPartition(Event event, String pName) {
        EventNode eventNode = new EventNode(event);
        ArrayList<EventNode> events = partitions.get(pName);
        if (events == null) {
            events = new ArrayList<EventNode>();
            partitions.put(pName, events);
            logger.fine("Created partition '" + pName + "'");

            // This is the first time this partition has been observed,
            // assign it a trace ID and add it to the map of traceIDs
            partitionNameToTraceID.put(pName, nextTraceID);
            nextTraceID++;
        }
        eventNode.setTraceID(partitionNameToTraceID.get(pName));

        // We want to add eventNode to partitions ONLY IF event actually
        // represents an event, not a dummy for state.
        if (!event.getEType().getETypeLabel().equals(dummyEtypeLabel)) {
            events.add(eventNode);
        }
        return eventNode;
    }

    public TraceGraph<?> generateDefaultOrderRelation(List<EventNode> allEvents)
            throws ParseException {
        if (logTimeTypeIsTotallyOrdered()) {
            return generateDirectTORelation(allEvents);
        }
        return generateDirectPORelation(allEvents);
    }

    /**
     * Given a list of log events that can be totally ordered, manipulates the
     * builder to construct the corresponding trace graph. Supports multiple
     * relations.
     * 
     * @param allEvents
     *            The list of events to process.
     * @throws ParseException
     */
    public ChainsTraceGraph generateDirectTORelation(List<EventNode> allEvents)
            throws ParseException {

        assert logTimeTypeIsTotallyOrdered();

        ChainsTraceGraph graph = new ChainsTraceGraph(allEvents);
        for (String partition : partitions.keySet()) {
            graph.addTrace(partitions.get(partition), allEventRelations);
        }
        return graph;
    }

    /**
     * Given a list of log events that can be only partially ordered,
     * manipulates the builder to construct the corresponding trace graph.
     * 
     * @param allEvents
     *            The list of events to process.
     * @throws ParseException
     */
    public DAGsTraceGraph generateDirectPORelation(List<EventNode> allEvents)
            throws ParseException {

        assert !logTimeTypeIsTotallyOrdered();

        DAGsTraceGraph graph = new DAGsTraceGraph(allEvents);

        // Maintains nodes without predecessors.
        Set<EventNode> noPredecessor = new LinkedHashSet<EventNode>(allEvents);

        Set<EventNode> directSuccessors;
        for (String partition : partitions.keySet()) {
            List<EventNode> group = partitions.get(partition);

            for (EventNode e1 : group) {

                // In the partially ordered case there may be multiple direct
                // successors.
                try {
                    directSuccessors = EventNode.getDirectPOSuccessors(e1,
                            group);
                } catch (EqualVectorTimestampsException e) {
                    String error = "Found two events with identical timestamps: (1) "
                            + e.e1.toString() + " (2) " + e.e2.toString();
                    logger.severe(error);
                    throw new ParseException(error);

                } catch (NotComparableVectorsException e) {
                    String error = "Found two events with different length vector timestamps: (1) "
                            + e.e1.toString() + " (2) " + e.e2.toString();
                    logger.severe(error);
                    throw new ParseException(error);
                }

                if (directSuccessors.size() == 0) {
                    // Tag messages without successor as terminal.
                    assert allEventRelations.get(e1).size() == 1;
                    String r = allEventRelations.get(e1).iterator().next()
                            .getRelation();
                    graph.tagTerminal(e1, r);
                } else {
                    for (EventNode e2 : directSuccessors) {
                        assert allEventRelations.get(e2).size() == 1;
                        String r = allEventRelations.get(e1).iterator().next()
                                .getRelation();
                        e1.addTransition(e2, r);
                        noPredecessor.remove(e2);
                    }
                }
            }

        }

        // Mark messages without a predecessor as initial.
        for (EventNode e : noPredecessor) {
            assert allEventRelations.get(e).size() == 1;
            String r = allEventRelations.get(e).iterator().next().getRelation();
            graph.tagInitial(e, r);
        }

        return graph;
    }
}
