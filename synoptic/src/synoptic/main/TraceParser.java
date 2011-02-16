package synoptic.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.util.InternalSynopticException;
import synoptic.util.NamedMatcher;
import synoptic.util.NamedPattern;
import synoptic.util.NamedSubstitution;
import synoptic.util.VectorTime;

/**
 * A generic trace parser, configured in terms of Java 7 style named capture
 * regular expressions.
 * 
 * @author mgsloan
 */
public class TraceParser {
    private final List<NamedPattern> parsers;
    private final List<LinkedHashMap<String, NamedSubstitution>> constantFields;
    private final List<LinkedHashMap<String, Boolean>> incrementors;

    private NamedSubstitution filter;
    private static Logger logger = Logger.getLogger("Parser Logger");

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

    // Patterns used to pre-process regular expressions
    private static Pattern matchEscapedSeparator = Pattern
            .compile("\\\\;\\\\;"), matchAssign = Pattern
            .compile("\\(\\?<(\\w*)=>([^\\)]*)\\)"),
            matchPreIncrement = Pattern.compile("\\(\\?<\\+\\+(\\w*)>\\)"),
            matchPostIncrement = Pattern.compile("\\(\\?<(\\w*)\\+\\+>\\)"),
            matchDefault = Pattern.compile("\\(\\?<(\\w*)>\\)");

    /**
     * Adds an individual trace line type, which consists of a regex with
     * additional syntax. This additional syntax is as follows: (?<name>)
     * Matches the default field regex, (?:\s*(?<name>\S+)\s*) (?<name=>value)
     * This specifies a value for a field, potentially with back-references
     * which get filled. (?<name++>) These specify context fields which are
     * included with (?<++name>) every type of trace \;\; becomes ;; (this is to
     * support the parsing of multiple regexes, described above). The regex must
     * match the entire line.
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
        String regex = matchEscapedSeparator.matcher(input_regex).replaceAll(
                ";;");

        // Parse out all of the constants.
        Matcher matcher = matchAssign.matcher(regex);

        LinkedHashMap<String, NamedSubstitution> cmap = new LinkedHashMap<String, NamedSubstitution>();
        while (matcher.find()) {
            cmap.put(matcher.group(1), new NamedSubstitution(matcher.group(2)));
        }
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

        // Replace fields which lack regex content with default.
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

        /*
         * TODO: warn about missing time / type fields. eg (old code):
         * System.err.println("Error: 'type' named group required in regex.");
         * System.out.println("No provided time field; Using integer time.");
         */

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
    public void addSeparator(String regex) throws InternalSynopticException {
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
    public void setPartitioner(String filter) {
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
    public List<LogEvent> parseTraceFile(File file, int linesToRead)
            throws ParseException, InternalSynopticException {
        String fileName = "";
        try {
            fileName = file.getAbsolutePath();
            FileInputStream fstream = new FileInputStream(file);
            InputStreamReader fileReader = new InputStreamReader(fstream);
            return parseTrace(fileReader, fileName, linesToRead);
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
    public List<LogEvent> parseTraceString(String trace, String traceName,
            int linesToRead) throws ParseException, InternalSynopticException {
        StringReader stringReader = new StringReader(trace);
        try {
            return parseTrace(stringReader, traceName, linesToRead);
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
    public List<LogEvent> parseTrace(Reader traceReader, String traceName,
            int linesToRead) throws ParseException, IOException,
            InternalSynopticException {
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

        // Process each line in sequence.
        while ((strLine = br.readLine()) != null) {
            if (results.size() == linesToRead) {
                break;
            }
            LogEvent event = parseLine(prevTime, strLine, traceName, context);
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
    private String getNodeName(Action a) {
        return filter.substitute(a.getStringArguments());
    }

    /**
     * Parse an individual line. If it contains no time field, prevTime is
     * incremented and used instead.
     */
    private LogEvent parseLine(VectorTime prevTime, String line,
            String filename, Map<String, Integer> context)
            throws ParseException, InternalSynopticException {
        Action action = null;
        VectorTime nextTime = null;
        for (int i = 0; i < parsers.size(); i++) {
            NamedMatcher matcher = parsers.get(i).matcher(line);
            if (matcher.matches()) {
                @SuppressWarnings("unchecked")
                Map<String, NamedSubstitution> cs = (Map<String, NamedSubstitution>) constantFields
                        .get(i).clone();
                Map<String, String> matched = matcher.toMatchResult()
                        .namedGroups();

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

                    // TODO: Determine policy of constant fields vs extracted
                    // have
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
                            context.put(inc.getKey(),
                                    context.get(inc.getKey()) + 1);
                        }
                    }
                    return null;
                }

                String eventType = matched.get("TYPE");

                // TODO: determine if this is desired + print warning

                if (eventType == null) {
                    // In the absence of a type, use the entire log line.
                    action = new Action(line);
                } else {
                    action = new Action(eventType);
                }
                action = action.intern();

                action.setStringArgument("FILE", filename);

                String timeField = matched.get("TIME");
                if (timeField == null) {
                    // TODO: warning when appropriate.
                    nextTime = incTime(prevTime);
                } else {
                    // TODO: more types of time
                    try {
                        nextTime = new VectorTime(timeField.trim());
                    } catch (Exception e) {
                        if (Main.recoverFromParseErrors) {
                            logger.warning("Failed to parse time field "
                                    + e.toString()
                                    + " for log line:\n"
                                    + line
                                    + "\nincrementing prior time value and continuing.");
                            // TODO: incTime makes little sense for vector time.
                            // In the vector time case, failing makes more sense
                            // here.
                            nextTime = incTime(prevTime);
                        } else {
                            if (Main.ignoreNonMatchingLines) {
                                logger.fine("Failed to parse time field "
                                        + e.toString() + " for log line:\n"
                                        + line
                                        + "\nIgnoring line and continuing.");
                            } else {
                                logger.severe("Failed to parse time field "
                                        + e.toString()
                                        + " for log line:\n"
                                        + line
                                        + "\n\tTry cmd line options:\n\t"
                                        + Main.getCmdLineOptDesc("ignoreNonMatchingLines")
                                        + "\n\t"
                                        + Main.getCmdLineOptDesc("debugParse"));
                                throw new ParseException();
                            }
                        }
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
                    logger.warning("input: " + line);
                    StringBuilder msg = new StringBuilder("{");
                    for (Map.Entry<String, String> entry : action
                            .getStringArguments().entrySet()) {
                        if (entry.getKey().equals("FILE")) {
                            continue;
                        }
                        msg.append(entry.getKey() + " = " + entry.getValue()
                                + ", ");
                    }
                    msg.append("TYPE = " + eventType);
                    msg.append("}");
                    logger.info(msg.toString());
                }
                action.setTime(nextTime);
                return new LogEvent(action);
            }
        }

        if (Main.recoverFromParseErrors) {
            logger.warning("Failed to parse trace line: \n" + line + "\n"
                    + "Using entire line as type.");
            action = new Action(line);
            action = action.intern();
            action.setTime(incTime(prevTime));
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
        List<LogEvent> set = parseTraceFile(new File(file), linesToRead);
        return generateDirectTemporalRelation(set, partition);
    }

    /**
     * Wrapper for calling generateDirectTemporalRelation statically.
     * 
     * @param allEvents
     * @param partition
     * @return
     */
    public static Graph<LogEvent> generateDirectTemporalRelationWithoutParser(
            List<LogEvent> allEvents, boolean partition) {
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
     *            True indicates partitioning the events on the nodeName field.
     */
    public Graph<LogEvent> generateDirectTemporalRelation(
            List<LogEvent> allEvents, boolean partition) {

        Graph<LogEvent> graph = new Graph<LogEvent>();

        // Partition by nodeName.
        LinkedHashMap<String, List<LogEvent>> groups = new LinkedHashMap<String, List<LogEvent>>();
        if (partition) {
            for (LogEvent e : allEvents) {
                String nodeName = getNodeName(e.getAction());
                List<LogEvent> events = groups.get(nodeName);
                if (events == null) {
                    events = new ArrayList<LogEvent>();
                    groups.put(nodeName, events);
                }
                events.add(e);
            }
        } else {
            groups.put(null, allEvents);
        }

        // Find all direct successors of all events. For an event e1, direct
        // successors are successors (in terms of vector-clock) that are not
        // preceded by any other successors of e1. That is, if e1 < x then x
        // is a direct successor if there is no other successor of e1 y such
        // that y < x.
        LinkedHashMap<LogEvent, LinkedHashSet<LogEvent>> directSuccessors = new LinkedHashMap<LogEvent, LinkedHashSet<LogEvent>>();
        for (List<LogEvent> group : groups.values()) {
            for (LogEvent e1 : group) {
                // First find all all events that succeed e1, store this set in
                // e1AllSuccessors.
                LinkedHashSet<LogEvent> e1AllSuccessors = new LinkedHashSet<LogEvent>();
                for (LogEvent e2 : group) {
                    if (e1 == e2) {
                        continue;
                    }

                    if (e1.getTime().lessThan(e2.getTime())) {
                        e1AllSuccessors.add(e2);
                    } else if (e1.getTime().equals(e2.getTime())) {
                        throw new IllegalArgumentException(
                                "Found two events with identical timestamps: (1) "
                                        + e1.toStringFull() + " (2) "
                                        + e2.toStringFull());
                    }
                }

                // Now out of all successors find all direct successors of e1.
                LinkedHashSet<LogEvent> e1DirectSuccessors = new LinkedHashSet<LogEvent>();
                for (LogEvent e1Succ1 : e1AllSuccessors) {
                    boolean directSuccessor = true; // whether or not e1Succ1 is
                                                    // a
                                                    // direct successor
                    for (LogEvent e1Succ2 : e1AllSuccessors) {
                        if (e1Succ1 == e1Succ2) {
                            continue;
                        }

                        if (e1Succ2.getTime().lessThan(e1Succ1.getTime())) {
                            directSuccessor = false;
                        }
                    }
                    if (directSuccessor) {
                        e1DirectSuccessors.add(e1Succ1);
                    }
                }
                directSuccessors.put(e1, e1DirectSuccessors);
            }
        }

        // Add all the log events to the graph.
        for (LogEvent e : allEvents) {
            graph.add(e);
        }

        String defaultRelation = "t";

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
