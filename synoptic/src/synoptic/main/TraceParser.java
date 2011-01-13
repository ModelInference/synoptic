package synoptic.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.MessageEvent;
import synoptic.model.input.GraphBuilder;
import synoptic.model.input.IBuilder;
import synoptic.model.input.VectorTime;

import synoptic.main.Main;
import synoptic.main.ParseException;

import synoptic.util.NamedMatcher;
import synoptic.util.NamedPattern;
import synoptic.util.NamedSubstitution;

/**
 * TraceParser is a generic trace parser, configured in terms of Java 7 style
 * named capture regexes.
 * @author mgsloan
 */
public class TraceParser {
	private List<NamedPattern> parsers;
	private List<HashMap<String, NamedSubstitution>> constantFields;
	private List<HashMap<String, Boolean>> incrementors;
	
	public IBuilder<MessageEvent> builder;
	private NamedSubstitution filter;
	private boolean internActions = true;
	private static Logger logger = Logger.getLogger("Parser Logger");
	
	//TODO: figure out how we deal with constraints which involve the multiple parsers.
	//  Eg, how do we verify that either none of the parsers have time fields, or all do.
	
	public TraceParser(IBuilder<MessageEvent> builder) {
		this.builder = builder;
		this.parsers = new ArrayList<NamedPattern>();
		this.constantFields = new ArrayList<HashMap<String, NamedSubstitution>>();
		this.incrementors = new ArrayList<HashMap<String, Boolean>>();
		this.filter = new NamedSubstitution("");
	}
	
	public TraceParser() {
		this(new GraphBuilder());
	}
	
	// Patterns used to pre-process regular expressions
	private static Pattern
		matchEscapedSeparator = Pattern.compile("\\\\;\\\\;"),
		matchAssign           = Pattern.compile("\\(\\?<(\\w*)=>([^\\)]*)\\)"),
		matchPreIncrement     = Pattern.compile("\\(\\?<\\+\\+(\\w*)>\\)"),
		matchPostIncrement    = Pattern.compile("\\(\\?<(\\w*)\\+\\+>\\)"),
		matchDefault          = Pattern.compile("\\(\\?<(\\w*)>\\)");
	
	/**
	 * Adds an individual trace line type, which consists of a regex with
	 * additional syntax.  This additional syntax is as follows:
	 * 
	 * (?<name>)        Matches the default field regex, (?:\s*(?<name>\S+)\s*)
	 * 
	 * (?<name=>value)  This specifies a value for a field, potentially with
	 *                  backreferences which get filled.
	 * 
	 * (?<name++>)      These specify context fields which are included with
	 * (?<++name>)      every type of trace 
	 * 
	 * \;\; becomes ;;  (this is to support the parsing of multiple regexes,
	 *                   described above).
	 *
	 * The regex must match the entire line.
	 *
	 * @param input_regex Regular expression of the form described.
	 */
	public void addRegex(String input_regex) {
		//TODO: this method for splitting is ugly, but it works for now
		// In order to use ";;" in a regex, escape as \;\;
		String regex = matchEscapedSeparator.matcher(input_regex).replaceAll(";;");

		// Parse out all of the constants.
		Matcher matcher = matchAssign.matcher(regex);

		HashMap<String, NamedSubstitution> cmap = new HashMap<String, NamedSubstitution>();
		while (matcher.find()) {
			cmap.put(matcher.group(1), new NamedSubstitution(matcher.group(2)));
		}
		this.constantFields.add(this.parsers.size(), cmap);
		
		// Remove the constant fields from the regex.
		regex = matcher.replaceAll("");

		// Parse out all of the incrementors.
		matcher = matchPreIncrement.matcher(regex);
		HashMap<String, Boolean> incMap = new HashMap<String, Boolean>();
		while (matcher.find()) {
			incMap.put(matcher.group(1), false);
		}
		regex = matcher.replaceAll("");
		matcher = matchPostIncrement.matcher(regex);
		while (matcher.find()) {
			incMap.put(matcher.group(1), true);
		}
		regex = matcher.replaceAll("");
		this.incrementors.add(incMap);
		
		// Replace fields which lack regex content with default.
		//TODO: Different defaults for some special fields.
		matcher = matchDefault.matcher(regex);
		StringBuffer newRegex = new StringBuffer();
		boolean isFirst = true;
		while (matcher.find()) {
			if (isFirst) {
				matcher.appendReplacement(newRegex, "(?:\\\\s*(?<$1>\\\\S+)\\\\s*)");
				isFirst = false;
			} else {
				matcher.appendReplacement(newRegex, "(?:\\\\s+(?<$1>\\\\S+)\\\\s*)");
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
			return;
		}
		this.parsers.add(parser);

		List<String> groups = parser.groupNames();
		if (logger != null) {
			logger.info(input_regex);
			logger.info("processed: " + regex);
			logger.info("standard: " + parser.standardPattern());
			if (!groups.isEmpty())
				logger.info("groups: " + groups.toString());
			if (!cmap.isEmpty())
				logger.info("fields: " + cmap.toString());
			if (!incMap.isEmpty())
				logger.info("incs: " + incMap.toString());
			/* TODO: warn about missing time / type fields. eg (old code):
			System.err.println("Error: 'type' named group required in regex.");
			System.out.println("No provided time field; Using integer time.");
			 */
		}
	}
	
	/**
	 * Create a separator-granularity match.  This works by creating an incrementing
	 * variable (on separator match), and adding SEPCOUNT to the granularity filter. 
	 */
	public void addSeparator(String regex) {
		this.addRegex(regex + "(?<SEPCOUNT++>)");
		this.filter.concat(new NamedSubstitution("\\k<SEPCOUNT>"));
	}
	
	/**
	 * Sets the partitioning filter, to the passed, backreference containing string.
	 */
	public void setPartitioner(String filter) {
		this.filter = new NamedSubstitution(filter);
	}
	
	/**
	 *  Occurrence class, used to track different occurrences of identical
	 *  actions.  This allows actions to be interned.  This makes equality more
	 *  efficient, and uses less memory.
	 */
	public class Occurrence {
		public MessageEvent message;
		private VectorTime time;
		public String nodeName;

		public Occurrence(MessageEvent message, VectorTime time, String nodeName) {
			this.message = message;
			this.time = time;
			this.nodeName = nodeName;
		}

		public VectorTime getTime() {
			return time;
		}
		
		public boolean isHidden() {
			return message.getStringArgument("HIDE") != null;
		}
	}
	
	/**
	 * Parses a trace file into a list of occurrences.
	 * 
	 * @param fileName Path to the file to read.
	 * @param linesToRead Bound on the number of lines to read.  Negatives
	 *     indicate unbounded.
	 * @return The parsed occurrences.
	 * @throws Exception 
	 */
	public List<Occurrence> parseTraceFile(File file, int linesToRead) throws ParseException {
		try {
			FileInputStream fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			ArrayList<Occurrence> results = new ArrayList<Occurrence>();
			String strLine = null;
			VectorTime prevTime = new VectorTime("0");
			
			// Initialize incrementor context.
			Map<String, Integer> context = new HashMap<String, Integer>();
			for (Map<String, Boolean> incs : incrementors) {
				for (String incField : incs.keySet()) {
					context.put(incField, 0);
				}
			}
			
			// Process each line in sequence.
			while ((strLine = br.readLine()) != null) {
				if (results.size() == linesToRead) break;
				Occurrence occ = parseLine(prevTime, strLine, file.getAbsolutePath(), context);
				if (occ == null) continue;
				prevTime = occ.getTime();
				results.add(occ);
			}
			br.close();
			logger.info("Successfully parsed " + results.size() + " events from " + file.getName());
			return results;
		} catch (IOException e) {
			logger.severe("Error while attempting to read log file: "
				+ e.getMessage());
			throw new ParseException();
		}
	}
	
	/* Increment time if it's a singleton. */
	private static VectorTime incTime(VectorTime t) {
		return t.isSingular() ? t.step(0) : t;
	}

	/* If there's a filter, this helper yields that argument from an action. */
	@SuppressWarnings("deprecation")
	private String getNodeName(Action a) {
		return filter.substitute(a.getStringArguments());
	}
	
	/* 
	 * Parse an individual line.  If it contains no time field, prevTime is
	 * incremented and used instead.
	 */
	private Occurrence parseLine(VectorTime prevTime, String line, String filename, Map<String, Integer> context) throws ParseException {
		Action action = null;
		VectorTime nextTime = null;
		for (int i = 0; i < this.parsers.size(); i++) {
			NamedMatcher matcher = this.parsers.get(i).matcher(line);
			if (matcher.matches()) {
				@SuppressWarnings("unchecked")
				Map<String, NamedSubstitution> cs = (Map<String, NamedSubstitution>)
					this.constantFields.get(i).clone();
				Map<String, String> matched = matcher.toMatchResult().namedGroups();
				
				// Perform pre-increments.
				for (Map.Entry<String, Boolean> inc : this.incrementors.get(i).entrySet())
					if (inc.getValue() == false)
						context.put(inc.getKey(), context.get(inc.getKey()) + 1);
				
				// Overlay increment context.
				for (Map.Entry<String, Integer> entry : context.entrySet()) {
					matched.put(entry.getKey(), entry.getValue().toString());
				}
				
				for (Map.Entry<String, NamedSubstitution> entry : cs.entrySet()) {
					// Process the constant field by substituting backreferences.
					String key = entry.getKey();
					String val = entry.getValue().substitute(matched);
					
					// Special case for integers, to allow for setting incrementors.
					Integer parsed = Integer.getInteger(val, Integer.MIN_VALUE);
					if (context.containsKey(key)) {
						context.put(key, parsed);
					}
					
					//TODO: Determine policy of constfields vs extracted have overlay priority
					if (!matched.containsKey(key)) {
						matched.put(key, val);
					}
				}
				
				if (matched.get("HIDE") != null) return null;

				String eventType = matched.get("TYPE");
				//TODO: determine if this is desired + print warning
				// In the absence of a type, use the entire line
				action = new Action(eventType == null ? line : eventType);
				
				action.setStringArgument("FILE", filename);

				String timeField = matched.get("TIME");
				if (timeField == null) {
					//TODO: warning when appropriate.
					nextTime = incTime(prevTime);
				} else {
					//TODO: more types of time
					try {
						nextTime = new VectorTime(timeField.trim());
					} catch (Exception e) {
						if (Main.recoverFromParseErrors) {
							logger.warning("Failed to parse time field " + e.toString() +
									" for log line:\n" + line +
									"\nincrementing prior time value and continuing.");
							nextTime = incTime(prevTime);
						} else {
							logger.severe("Failed to parse time field " + e.toString() +
									" for log line:\n" + line);
							throw new ParseException();
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
				for (Map.Entry<String, Boolean> inc : this.incrementors.get(i).entrySet())
					if (inc.getValue() == true)
						context.put(inc.getKey(), context.get(inc.getKey()) + 1);
				
				if (internActions) action = action.intern();
				String nodeName = getNodeName(action);
				if (Main.debugParse) {
					logger.warning("input: " + line);
					StringBuilder msg = new StringBuilder("{");
					for (Map.Entry<String, String> entry : action.getStringArguments().entrySet()) {
						if (entry.getKey().equals("FILE")) continue;
						msg.append(entry.getKey() + " = " + entry.getValue() + ", ");
					}
					msg.append("TYPE = " + nodeName);
					msg.append("}");
					logger.info(msg.toString());
				}
				return new Occurrence(this.builder.insert(action), nextTime, nodeName);
			}
		}

		if (Main.recoverFromParseErrors) {
			logger.warning("Failed to parse trace line: \n" + line +
					"\n" + "Using entire line as type.");
			action = new Action(line);
			if (internActions) action = action.intern();
			return new Occurrence(this.builder.insert(action), incTime(prevTime), null);
		}
		
		logger.severe("Failed to parse trace line: \n" + line);
		throw new ParseException();
	}
	
	/**
	 * Convenience function, yielding a graph for the specified file.
	 * 
	 * @param file The trace file to read
	 * @param linesToRead Maximum number of tracelines to read.  Negative if unlimited.
	 * @param partition True indicates partitioning the occurrences on the nodeName field.
	 * @return The resulting graph.
	 * @throws ParseException 
	 */
	public Graph<MessageEvent> readGraph(String file, int linesToRead, boolean partition) throws ParseException {
		List<Occurrence> set = this.parseTraceFile(new File(file), linesToRead);
		generateDirectTemporalRelation(set, partition);
		return ((GraphBuilder)this.builder).getRawGraph();
	}
	
	/**
	 * Given a list of occurrences, manipulates the builder to construct the
	 * corresponding graph.
	 * 
	 * @param set The list of occurrences to process.
	 * @param partition True indicates partitioning the occurrences on the nodeName field.
	 */
	public void generateDirectTemporalRelation(List<Occurrence> set, boolean partition) {
		
		// Partition by nodeName.
		HashMap<String, List<Occurrence>> groups = new HashMap<String, List<Occurrence>>();
		if (partition) {
			for (Occurrence m : set) {
				List<Occurrence> occs = groups.get(m.nodeName);
				if (occs == null) {
					occs = new ArrayList<Occurrence>();
					groups.put(m.nodeName, occs);
				}
				occs.add(m);
			}
		} else {
			groups.put(null, set);
		}
		
/* sweepline transitive reduction (WIP) 
		for (List<Occurrence> group : groups.values()) {
			sortTrace(group);
			
			Occurrence prev = null;
			List<Occurrence> prevSet = new ArrayList<Occurrence>(), curSet = new ArrayList<Occurrence>();
			for (Occurrence a : group) {
				if (prevSet.isEmpty()) {
					this.builder.addInitial(a.message, "i");
				} else {
					for (Occurrence b : prevSet) {
						this.builder.connect(a.message, b.message, "t");
					}
				}
				if (prev != null && prev.getTime().lessThan(a.getTime())) {
					prevSet = curSet;
					curSet = new ArrayList<Occurrence>();
				}
				curSet.add(a);
				prev = a;
			}
		}
*/

		HashMap<Occurrence, HashSet<Occurrence>> directSuccessors =
			new HashMap<Occurrence, HashSet<Occurrence>>();
		Set<Occurrence> noPredecessor = new HashSet<Occurrence>(set);
		
		for(List<Occurrence> group : groups.values()) {
			for (Occurrence m1 : group) {
				directSuccessors.put(m1, new HashSet<Occurrence>());
				for (Occurrence m2 : group) {
					if (m1.getTime().lessThan(m2.getTime())) {
						boolean add = true;
						List<Occurrence> removeSet = new ArrayList<Occurrence>();
						for (Occurrence m : directSuccessors.get(m1)) {
							if (m2.getTime().lessThan(m.getTime())) {
								add = true;
								removeSet.add(m);
							}
							if (m.getTime().lessThan(m2.getTime())) {
								add = false;
								break;
							}
						}
						directSuccessors.get(m1).removeAll(removeSet);
						if (add)
							directSuccessors.get(m1).add(m2);
					}
				}
			}
		}
		/*
		for (Occurrence m : directSuccessors.keySet()) {
			for (Occurrence s : directSuccessors.get(m)) {
				noPredecessor.remove(s);
			}
		} */
		for (Occurrence m : directSuccessors.keySet()) {
			for (Occurrence s : directSuccessors.get(m)) {
				this.builder.connect(m.message, s.message, "t");
				noPredecessor.remove(s);
			}
		}
		for (Occurrence m : noPredecessor) {
			this.builder.addInitial(m.message, "t");
		}
	}

	/* TODO: reinstate if sweepline transitive reduction is used.
	class TemporalComparator implements Comparator<Occurrence> {
		public int compare(Occurrence a, Occurrence b) {
			if (a == b) return 0;
			VectorTime x = a.getTime();
			VectorTime y = b.getTime();
			if (x.lessThan(y)) return -1;
			if (y.lessThan(x)) return 1;
			return 0;
		}
	}
	
	private TemporalComparator comparator = new TemporalComparator();

	private void sortTrace(List<Occurrence> acts) {
		Collections.sort(acts, comparator);
	}
	*/
}