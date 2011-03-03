package synoptic.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.util.EqualVectorTimestampsException;
import synoptic.util.InternalSynopticException;
import synoptic.util.NamedMatcher;
import synoptic.util.NamedPattern;
import synoptic.util.NamedSubstitution;
import synoptic.util.NotComparableVectorsException;
import synoptic.util.VectorTime;

/**
 * A generic trace parser, configured in terms of Java 7 style named capture
 * regular expressions.
 * 
 * @author mgsloan
 */
public class TraceParser {
    public final static String defaultRelation = "t";

    private static Logger logger = Logger.getLogger("Parser Logger");

    private final List<NamedPattern> parsers;
    private final List<LinkedHashMap<String, NamedSubstitution>> constantFields;
    private final List<LinkedHashMap<String, Boolean>> incrementors;

    private NamedSubstitution filter;

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
            .compile("\\(\\?<(\\w*)>\\)");

    // All line-matching regexps will be checked to include the following set of
    // required regexp groups.
    private static final List<String> requiredGroups = Arrays.asList("TYPE");

    // Regexp groups that represent valid time in a log line:
    // TIME: integer time parsed from the log line
    // VTIME: vector clock time parsed from the log line
    private static final List<String> validTimeGroups = Arrays.asList("TIME",
            "VTIME");

    // The time we use implicitly. LTIME is log-line-number time. Which exists
    // implicitly for every log-line.
    private static final String implicitTimeGroup = "LTIME";

    // The time group regexp selected (implicitly) for use by this parser via
    // passed reg exps to match lines. The parser allows only one type of time
    // to be used.
    private String selectedTimeGroup = null;

    // TODO: figure out how we deal with constraints which involve the multiple
    // parsers.
    // e.g., how do we verify that either none of the parsers have time fields,
    // or all do.

    public TraceParser() {
        parsers = new ArrayList<NamedPattern>();
        constantFields = new ArrayList<LinkedHashMap<String, NamedSubstitution>>();
        incrementors = new ArrayList<LinkedHashMap<String, Boolean>>();
        filter = new NamedSubstitution("");
    }

    /**
     * Checks whether lst contains duplicates. If so, it logs an error using
     * regex for information and throws a ParseException.
     * 
     * @param lst
     *            the list to check for duplicates
     * @param regex
     *            the associated regex string to use when printing out an error
     * @throws ParseException
     */
    public static void detectListDuplicates(List<String> lst, String regex)
            throws ParseException {
        // Check for group duplicates.
        LinkedHashSet<String> lstSet = new LinkedHashSet<String>(lst);
        if (lstSet.size() == lst.size()) {
            return;
        }

        // We have duplicates in list, which means some fields are
        // defined multiple times. We find these by removing each set
        // element from the list, then converting the list to a set (to remove
        // groups that have more than 3+ occurrences) and throw an exception.
        for (String e : lstSet) {
            lst.remove(e);
        }

        logger.severe("The fields: " + new LinkedHashSet<String>(lst)
                + " appear more than once in regex: " + regex);
        throw new ParseException();
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
        logger.fine("entering addRegex with: " + input_regex);
        String regex = matchEscapedSeparator.matcher(input_regex).replaceAll(
                ";;");

        // Parse out all of the constants.
        Matcher matcher = matchAssign.matcher(regex);

        // Maintains a map between fields and their values in the regex.
        LinkedHashMap<String, NamedSubstitution> cmap = new LinkedHashMap<String, NamedSubstitution>();

        // A list of all the fields that were assigned in the regex.
        LinkedList<String> fields = new LinkedList<String>();

        // Indicates whether this regexp sets the HIDE field to true or not.
        boolean isHidden = false;
        while (matcher.find()) {
            String field = matcher.group(1);
            String value = matcher.group(2);
            fields.add(field);

            cmap.put(field, new NamedSubstitution(value));
            logger.fine("matchAssign: " + field + " -> " + matcher.group(2));
            // Prevent the user from adding regexes that modify the parsing of
            // special time fields.
            if (validTimeGroups.contains(field)) {
                logger.severe("Cannot assign custom regex expressions to parse time field "
                        + field + " in regex: " + regex);
                throw new ParseException();
            }

            // HIDE groups can only be assigned to 'true'
            if (field.equals("HIDE")) {
                if (!value.equals("true")) {
                    logger.severe("HIDE field cannot be assigned to: " + value
                            + ", it can only be assigned to 'true' in regex: "
                            + regex);
                    throw new ParseException();
                }
                isHidden = true;
            }
        }
        // Check for field duplicates.
        detectListDuplicates(fields, regex);

        constantFields.add(parsers.size(), cmap);

        // Remove the constant fields from the regex.
        regex = matcher.replaceAll("");

        // Parse out all of the incrementors.
        matcher = matchPreIncrement.matcher(regex);
        LinkedHashMap<String, Boolean> incMap = new LinkedHashMap<String, Boolean>();
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
            logger.severe("Error parsing named-captures in " + regex + ":");
            logger.severe(e.toString());
            throw new ParseException();
        }
        parsers.add(parser);
        List<String> groups = parser.groupNames();

        // Check for group duplicates.
        detectListDuplicates(groups, regex);

        // Check that line-matching regexes contain the required named groups.
        // But only if they are not HIDDEN.
        if (!isHidden) {
            for (String reqGroup : requiredGroups) {
                if (!groups.contains(reqGroup) && !fields.contains(reqGroup)) {
                    logger.severe("Regular expression: " + regex
                            + " is missing the required named group: "
                            + reqGroup);
                    throw new ParseException();
                }
            }
        }

        if (!isHidden) {
            // We have two cases for specifying time types:
            // (1) Implicit: type is not specified and we use implicitTimeGroup.
            // (2) Explicit: exactly one kind of type is specified in the regex.
            String regexTimeUsed = null;
            for (String group : groups) {
                if (validTimeGroups.contains(group)) {
                    if (regexTimeUsed != null) {
                        logger.severe("The regex: " + regex
                                + " contains multiple time field definitions: "
                                + group + ", " + regexTimeUsed);
                        throw new ParseException();
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
            } else {
                // Prior time type was used, make sure that it matches regex's.
                if (!selectedTimeGroup.equals(regexTimeUsed)) {
                    logger.severe("Time type cannot vary. A prior regex used the type "
                            + selectedTimeGroup
                            + ", while regex "
                            + regex
                            + " uses the type " + regexTimeUsed);
                    throw new ParseException();
                }
            }

        }

        if (Main.debugParse) {
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
            throw InternalSynopticException.Wrap(e);
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
    public void setPartitionsMap(String filter) {
        this.filter = new NamedSubstitution(filter);
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
    public ArrayList<LogEvent> parseTraceFile(File file, int linesToRead)
            throws ParseException, InternalSynopticException {
        String fileName = "";
        try {
            fileName = file.getAbsolutePath();
            String localFileName = file.getName();
            FileInputStream fstream = new FileInputStream(file);
            InputStreamReader fileReader = new InputStreamReader(fstream);
            return parseTrace(fileReader, fileName, linesToRead, localFileName);
        } catch (IOException e) {
            logger.severe("Error while attempting to read log file ["
                    + fileName + "]: " + e.getMessage());
            throw new ParseException();
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
    public ArrayList<LogEvent> parseTraceString(String trace, String traceName,
            int linesToRead) throws ParseException, InternalSynopticException {
        StringReader stringReader = new StringReader(trace);
        try {
            return parseTrace(stringReader, traceName, linesToRead, null);
        } catch (IOException e) {
            logger.severe("Error while reading string [" + traceName + "]: "
                    + e.getMessage());
            throw new ParseException();
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
    public ArrayList<LogEvent> parseTrace(Reader traceReader, String traceName,
            int linesToRead, String localFileName) throws ParseException,
            IOException, InternalSynopticException {
        BufferedReader br = new BufferedReader(traceReader);

        // Initialize incrementor context.
        Map<String, Integer> context = new LinkedHashMap<String, Integer>();
        for (Map<String, Boolean> incs : incrementors) {
            for (String incField : incs.keySet()) {
                context.put(incField, 0);
            }
        }

        ArrayList<LogEvent> results = new ArrayList<LogEvent>();
        String strLine = null;
        VectorTime prevTime = new VectorTime("0");
        int lineNum = 0;
        // Process each line in sequence.
        while ((strLine = br.readLine()) != null) {
            if (results.size() == linesToRead) {
                break;
            }
            lineNum++;
            LogEvent event = parseLine(prevTime, strLine, traceName, context,
                    localFileName, lineNum);
            if (event == null) {
                continue;
            }
            prevTime = event.getTime();
            results.add(event);
        }
        br.close();
        logger.info("Successfully parsed " + results.size() + " events from "
                + traceName);
        return results;
    }

    /* Increment time if it's a singleton. */
    private static VectorTime incTime(VectorTime t) {
        return t.isSingular() ? t.step(0) : t;
    }

    /* If there's a filter, this helper yields that argument from an action. */
    private String getPartitionName(Action a) {
        return filter.substitute(a.getStringArguments());
    }

    /**
     * Parse an individual line. If it contains no time field, prevTime is
     * incremented and used instead.
     */
    private LogEvent parseLine(VectorTime prevTime, String line,
            String filename, Map<String, Integer> context,
            String localFileName, int lineNum) throws ParseException,
            InternalSynopticException {

        Action action = null;
        VectorTime nextTime = null;

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

            String eventType = matched.get("TYPE");

            // TODO: determine if this is desired + print warning

            if (eventType == null) {
                // In the absence of a type, use the entire log line.
                action = new Action(line, line, localFileName, lineNum);
            } else {
                action = new Action(eventType, line, localFileName, lineNum);
            }
            action = action.intern();

            action.setStringArgument("FILE", filename);

            // We have two cases for processing time on log lines:
            // (1) Implicitly: no matched field is a time field because it is
            // set to implicitTimeGroup. For this case we simply use the prior
            // time + 1 (log-line counting time).
            // (2) Explicitly: one of the matched fields must be a time type
            // field (set in addRegex). If no such match is found then we throw
            // an exception.
            if (selectedTimeGroup == implicitTimeGroup) {
                // Implicit case.
                nextTime = incTime(prevTime);
            } else {
                // Explicit case.
                String timeField = matched.get(selectedTimeGroup);
                if (timeField == null) {
                    logger.severe("Unable to parse time type "
                            + selectedTimeGroup + " from line " + line);
                    throw new ParseException();
                }

                // Attempt to parse the time type field as a vector time -- we
                // use this for both integer and vector time types (all current
                // types).
                if (selectedTimeGroup.equals("TIME")) {
                    try {
                        Integer.parseInt(timeField.trim());
                    } catch (Exception e) {
                        String errMsg = "Unable to parse time field on log line:\n"
                                + line;
                        if (Main.ignoreNonMatchingLines) {
                            logger.warning(errMsg
                                    + "\nIgnoring line and continuing.");
                            continue;
                        }
                        logger.severe(errMsg
                                + "\n\tTry cmd line options:\n\t"
                                + Main.getCmdLineOptDesc("ignoreNonMatchingLines")
                                + "\n\t" + Main.getCmdLineOptDesc("debugParse"));

                        logger.severe(e.toString());
                        throw new ParseException();
                    }
                }

                try {
                    if (selectedTimeGroup.equals("TIME")) {
                        Integer timeFieldInt = Integer.parseInt(timeField
                                .trim());
                        nextTime = new VectorTime(timeFieldInt);
                    } else {
                        nextTime = new VectorTime(timeField.trim());
                    }
                } catch (Exception e) {
                    String errMsg = "Unable to parse time field on log line:\n"
                            + line;
                    if (Main.ignoreNonMatchingLines) {
                        logger.warning(errMsg
                                + "\nIgnoring line and continuing.");
                        continue;
                    }
                    logger.severe(errMsg + "\n\tTry cmd line options:\n\t"
                            + Main.getCmdLineOptDesc("ignoreNonMatchingLines")
                            + "\n\t" + Main.getCmdLineOptDesc("debugParse"));

                    logger.severe(e.toString());
                    throw new ParseException();
                }
            }

            for (Map.Entry<String, String> group : matched.entrySet()) {
                String name = group.getKey();
                if (!name.equals("TYPE") && !name.equals("TIME")) {
                    action.setStringArgument(name, group.getValue());
                }
            }

            // Perform post-increments.
            for (Map.Entry<String, Boolean> inc : incrementors.get(i)
                    .entrySet()) {
                if (inc.getValue() == true) {
                    context.put(inc.getKey(), context.get(inc.getKey()) + 1);
                }
            }

            if (Main.debugParse) {
                // TODO: include partition name in the list of field values
                logger.info("input: " + line);
                StringBuilder msg = new StringBuilder("{");
                for (Map.Entry<String, String> entry : action
                        .getStringArguments().entrySet()) {
                    if (entry.getKey().equals("FILE")) {
                        continue;
                    }
                    msg.append(entry.getKey() + " = " + entry.getValue() + ", ");
                }
                msg.append("TYPE = " + eventType);
                msg.append("}");
                logger.info(msg.toString());
            }
            action.setTime(nextTime);
            return new LogEvent(action);
        }

        if (Main.recoverFromParseErrors) {
            logger.warning("Failed to parse trace line: \n" + line + "\n"
                    + "Using entire line as type.");
            action = new Action(line, line, localFileName, lineNum);
            action = action.intern();
            if (selectedTimeGroup.equals(implicitTimeGroup)) {
                // We can recover OK with log-line counting time.
                action.setTime(incTime(prevTime));
            } else {
                // We can't recover with vector time -- incrementing it simply
                // doesn't make sense.
                logger.severe("Unable to recover from parse error with vector-time type.");
                throw new ParseException();
            }
            return new LogEvent(action);
        } else if (Main.ignoreNonMatchingLines) {
            logger.fine("Failed to parse trace line: \n" + line + "\n"
                    + "Ignoring line and continuing.");
            return null;
        }

        logger.severe("Line from file [" + filename
                + "] does not match any of the provided regular exceptions:\n"
                + line + "\nTry cmd line options:\n\t"
                + Main.getCmdLineOptDesc("ignoreNonMatchingLines") + "\n\t"
                + Main.getCmdLineOptDesc("debugParse"));
        throw new ParseException();
    }

    /**
     * Convenience function, yielding a graph for the specified file.
     * 
     * @param file
     *            The trace file to read
     * @param linesToRead
     *            Maximum number of tracelines to read. Negative if unlimited.
     * @param partition
     *            True indicates partitioning the log events on the nodeName
     *            field.
     * @return The resulting graph.
     * @throws ParseException
     * @throws InternalSynopticException
     */
    public Graph<LogEvent> readGraph(String file, int linesToRead,
            boolean partition) throws ParseException, InternalSynopticException {
        ArrayList<LogEvent> set = parseTraceFile(new File(file), linesToRead);
        return generateDirectTemporalRelation(set, partition);
    }

    /**
     * Wrapper for calling generateDirectTemporalRelation statically.
     * 
     * @param allEvents
     * @param partition
     * @return
     * @throws ParseException
     */
    public static Graph<LogEvent> generateDirectTemporalRelationWithoutParser(
            ArrayList<LogEvent> allEvents, boolean partition)
            throws ParseException {
        TraceParser parser = new TraceParser();
        return parser.generateDirectTemporalRelation(allEvents, partition);
    }

    /**
     * Given a list of log events, manipulates the builder to construct the
     * corresponding graph.
     * 
     * @param allEvents
     *            The list of events to process.
     * @param partition
     *            True indicates partitioning the events on the partition name
     *            field.
     * @throws ParseException
     */
    public Graph<LogEvent> generateDirectTemporalRelation(
            ArrayList<LogEvent> allEvents, boolean partition)
            throws ParseException {

        Graph<LogEvent> graph = new Graph<LogEvent>();

        LinkedHashMap<String, ArrayList<LogEvent>> partitions = new LinkedHashMap<String, ArrayList<LogEvent>>();
        if (partition) {
            // Partition based on filter expression.
            for (LogEvent e : allEvents) {
                String pName = getPartitionName(e.getAction());
                ArrayList<LogEvent> events = partitions.get(pName);
                if (events == null) {
                    events = new ArrayList<LogEvent>();
                    partitions.put(pName, events);
                    logger.fine("Created partition '" + pName + "'");
                }
                events.add(e);
            }
            graph.setPartitions(partitions);
        } else {
            // Otherwise, place all events into a single partition.
            partitions.put(null, allEvents);
            graph.setPartitions(null);
        }

        // Find all direct successors of all events. For an event e1, direct
        // successors are successors (in terms of vector-clock) that are not
        // preceded by any other successors of e1. That is, if e1 < x then x
        // is a direct successor if there is no other successor of e1 y such
        // that y < x.
        LinkedHashMap<LogEvent, Set<LogEvent>> directSuccessors = new LinkedHashMap<LogEvent, Set<LogEvent>>();
        for (List<LogEvent> group : partitions.values()) {
            for (LogEvent e1 : group) {
                // Find and set all direct successors of e1. In totally ordered
                // case there is at most one direct successor, in partially
                // ordered case there may be multiple direct successors.
                try {
                    directSuccessors.put(e1,
                            LogEvent.getDirectSuccessors(e1, group));
                } catch (EqualVectorTimestampsException e) {
                    logger.severe("Found two events with identical timestamps: (1) "
                            + e.e1.toString() + " (2) " + e.e2.toString());
                    throw new ParseException();

                } catch (NotComparableVectorsException e) {
                    logger.severe("Found two events with different length vector timestamps: (1) "
                            + e.e1.toString() + " (2) " + e.e2.toString());
                    throw new ParseException();
                }
            }
        }

        // Add all the log events to the graph.
        for (LogEvent e : allEvents) {
            graph.add(e);
        }

        // Connect the events in the graph, and also build up noPredecessor and
        // noSuccessor event sets.
        LinkedHashSet<LogEvent> noPredecessor = new LinkedHashSet<LogEvent>(
                allEvents);
        LinkedHashSet<LogEvent> noSuccessor = new LinkedHashSet<LogEvent>();
        for (LogEvent e1 : directSuccessors.keySet()) {
            for (LogEvent e2 : directSuccessors.get(e1)) {
                e1.addTransition(e2, defaultRelation);
                noPredecessor.remove(e2);
            }
            if (directSuccessors.get(e1).size() == 0) {
                noSuccessor.add(e1);
            }
        }

        // TODO: make sure that initialNodeLabel does not conflict with any of
        // the event labels in the trace.
        Action dummyAct = Action.NewInitialAction();
        dummyAct = dummyAct.intern();
        graph.setDummyInitial(new LogEvent(dummyAct), defaultRelation);
        // Mark messages without a predecessor as initial.
        for (LogEvent e : noPredecessor) {
            graph.tagInitial(e, defaultRelation);
        }

        // TODO: make sure that terminalNodeLabel does not conflict with any of
        // the event labels in the trace.
        dummyAct = Action.NewTerminalAction();
        dummyAct = dummyAct.intern();
        graph.setDummyTerminal(new LogEvent(dummyAct));
        // Mark messages without a predecessor as terminal.
        for (LogEvent e : noSuccessor) {
            graph.tagTerminal(e, defaultRelation);
        }

        return graph;
    }
}
