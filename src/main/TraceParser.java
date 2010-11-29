package main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Action;
import model.Graph;
import model.MessageEvent;
import model.input.GraphBuilder;
import model.input.IBuilder;
import model.input.VectorTime;

import util.NamedMatcher;
import util.NamedPattern;

/**
 * TraceParser is a generic trace parser, configured in terms of Java 7 style
 * named capture regexes.
 * @author mgsloan
 */
public class TraceParser {
	private List<NamedPattern> parsers;
	private List<Map<String, String>> constantFields;
	private List<Map<String, Boolean>> incrementors;
	
	//TODO: some things fail when this is uninitialized.  Either initialize or add preconditions.
	public IBuilder<MessageEvent> builder;
	public Set<String> filters;
	public boolean internActions = true;
	
	public static Logger LOG = null;
	
	//TODO: figure out how we deal with constraints which involve the multiple parsers.
	//  Eg, how do we verify that either none of the parsers have time fields, or all do.
	
	public TraceParser() {
		this.parsers = new ArrayList<NamedPattern>();
		this.constantFields = new ArrayList<Map<String, String>>();
		this.incrementors = new ArrayList<Map<String, Boolean>>();
		this.filters = new HashSet<String>();
		this.LOG = Logger.getLogger("Parser Logger");
	}
	
	/**
	 * This constructor takes a string of the form regex1;;regex2;;regex3, and
	 * initializes the parsers with them.
	 */
	public TraceParser(String parser) {
		this.parsers = new ArrayList<NamedPattern>();
		this.constantFields = new ArrayList<Map<String, String>>();
		this.incrementors = new ArrayList<Map<String, Boolean>>();
		this.filters = new HashSet<String>();
		for (String regex : matchSeparator.split(parser)) {
			addRegex(regex);
		}
	}
	
	// Patterns used to pre-process regular expressions
	private static Pattern
		matchSeparator        = Pattern.compile(";;"),
		matchEscapedSeparator = Pattern.compile("\\\\;\\\\;"),
		matchConstant         = Pattern.compile("\\(\\?<(\\w*)=(\\w*)>\\)"),
		matchPreIncrement     = Pattern.compile("\\(\\?<\\+\\+(\\w*)>\\)"),
		matchPostIncrement    = Pattern.compile("\\(\\?<(\\w*)\\+\\+>\\)"),
		matchDefault          = Pattern.compile("\\(\\?<(\\w*)>\\)"),
		matchReference        = Pattern.compile("\\\\k<(\\w*)>");
	
	/**
	 * Adds an individual trace line type, which consists of a regex with
	 * additional syntax.  This additional syntax is as follows:
	 * 
	 * (?<name>)        Matches the default field regex, (?:\s*(?<name>\S*)\s*)
	 * 
	 * (?<name=value>)  This specifies a constant field, which provides a
	 *                  value to bind to the name.  No content regex allowed.
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
		Matcher matcher = matchConstant.matcher(regex);

		Map<String, String> cmap = new HashMap<String, String>();
		while (matcher.find()) {
			cmap.put(matcher.group(1), matcher.group(2));
		}
		this.constantFields.add(this.parsers.size(), cmap);
		
		// Remove the constant fields from the regex.
		regex = matcher.replaceAll("");

		// Parse out all of the incrementors.
		matcher = matchPreIncrement.matcher(regex);
		Map<String, Boolean> incMap = new HashMap<String, Boolean>();
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
			LOG.severe("Error parsing named-captures in " + regex + ":");
			LOG.severe(e.toString());
			return;
		}
		this.parsers.add(parser);

		List<String> groups = parser.groupNames();
		if (LOG != null) {
			LOG.info(input_regex);
			LOG.info("processed: " + regex);
			LOG.info("standard: " + parser.standardPattern());
			if (!groups.isEmpty())
				LOG.info("groups: " + groups.toString());
			if (!cmap.isEmpty())
				LOG.info("fields: " + cmap.toString());
			if (!incMap.isEmpty())
				LOG.info("incs: " + incMap.toString());
			/* TODO: warn about missing time / type fields. eg (old code):
			System.err.println("Error: 'type' named group required in regex.");
			System.out.println("No provided time field; Using integer time.");
			 */
		}
	}
	
	public void addPartitionField(String field) {
		this.filters.add(field);
	}
	
	public void addSeparator(String regex) {
		this.addRegex(regex + "(?<SEPCOUNT++>)");
		this.addPartitionField("SEPCOUNT");
	}
	
	public void setPartitioner(String filter) {
		Matcher matcher = matchReference.matcher(filter);
		while (matcher.find()) {
			addPartitionField(matcher.group(1));
		}
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
	/*
	public List<Occurrence> parseTraceFiles(String wildcardPath, int linesToRead) {
		List<Occurrence> result = new ArrayList<Occurrence>();
		FileFilter filter = new WildcardFileFilter(wildcardPath);
		for (File f : (new File(".")).listFiles(filter)) {
			parseTraceFile(f.getAbsolutePath(), linesToRead);
		}
	}*/
	
	/**
	 * Parses a trace file into a list of occurrences.
	 * 
	 * @param fileName Path to the file to read.
	 * @param linesToRead Bound on the number of lines to read.  Negatives
	 *     indicate unbounded.
	 * @return The parsed occurrences.
	 */
	public List<Occurrence> parseTraceFile(String fileName, int linesToRead) {
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			ArrayList<Occurrence> results = new ArrayList<Occurrence>();
			String strLine = null;
			VectorTime prevTime = new VectorTime("0");
			Map<String, Integer> context = new HashMap<String, Integer>();
			for (Map<String, Boolean> incs : incrementors) {
				for (String incField : incs.keySet()) {
					context.put(incField, 0);
				}
			}
			while ((strLine = br.readLine()) != null) {
				if (results.size() == linesToRead) break;
				Occurrence occ = parseLine(prevTime, strLine, fileName, context);
				if (occ == null) continue;
				prevTime = occ.getTime();
				results.add(occ);
			}
			br.close();
			LOG.log(Level.INFO, "Successfully parsed " + results.size() + " events from " + fileName);
			return results;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Error while attempting to read log file: "
				+ e.getMessage());
		}
		return null;
	}
	
	/* Increment time if it's a singleton. */
	private static VectorTime incTime(VectorTime t) {
		return t.isSingular() ? t.step(0) : t;
	}

	/* If there's a filter, this helper yields that argument from an action. */
	private String getNodeName(Action a) {
		if (filters == null) return null;
		StringBuilder result = new StringBuilder();
		for (String filter : filters) {
			result.append(a.getStringArgument(filter));
			result.append(" ");
		}
		return result.toString();
		//if (filter == null || filter.equals("")) return null;
		//return a.getStringArgument(filter);
	}
	
	/* 
	 * Parse an individual line.  If it contains no time field, prevTime is
	 * incremented and used instead.
	 */
	private Occurrence parseLine(VectorTime prevTime, String line, String filename, Map<String, Integer> context) {
		Action action = null;
		VectorTime nextTime = null;
		for (int i = 0; i < this.parsers.size(); i++) {
			NamedMatcher matcher = this.parsers.get(i).matcher(line);
			if (matcher.matches()) {
				Map<String, String> gs = this.constantFields.get(i);
				
				// Re-set members of the context with constant fields
				for (Map.Entry<String, String> entry : gs.entrySet()) {
					Integer parsed = Integer.getInteger(entry.getValue(), Integer.MIN_VALUE);
					if (context.containsKey(entry.getKey()) && parsed != Integer.MIN_VALUE) {
						context.put(entry.getKey(), parsed);
					}
				}
				
				// Overlay extracted groups.
				gs.putAll(matcher.toMatchResult().namedGroups());
				
				// Perform preincrements.
				for (Map.Entry<String, Boolean> inc : this.incrementors.get(i).entrySet())
					if (inc.getValue() == false)
						context.put(inc.getKey(), context.get(inc.getKey()) + 1);
				
				// Overlay increment context.
				for (Map.Entry<String, Integer> entry : context.entrySet()) {
					gs.put(entry.getKey(), entry.getValue().toString());
				}
				
				if (gs.get("HIDE") != null) return null;

				String eventType = gs.get("TYPE");
				//TODO: determine if this is desired + print warning
				// In the absence of a type, use the entire line
				action = new Action(eventType == null ? line : eventType);
				
				action.setStringArgument("FILE", filename);

				String timeField = gs.get("TIME");
				if (timeField == null) {
					//TODO: warning when appropriate.
					nextTime = incTime(prevTime);
				} else {
					//TODO: more types of time
					try {
						nextTime = new VectorTime(timeField.trim());
					} catch (Exception e) {
						LOG.warning("Could not parse time field " + e.toString());
						LOG.warning("For this log line: " + line);
						nextTime = incTime(prevTime);
					}
				}
				for (Map.Entry<String, String> group : gs.entrySet()) {
					String name = group.getKey();
					if (!name.equals("TYPE") && !name.equals("TIME")) {
						action.setStringArgument(name, group.getValue());
					}
				}
				
				// Perform postincrements.
				for (Map.Entry<String, Boolean> inc : this.incrementors.get(i).entrySet())
					if (inc.getValue() == true)
						context.put(inc.getKey(), context.get(inc.getKey()) + 1);
				
				if (internActions) action = action.intern();
				String nodeName = getNodeName(action);
				return new Occurrence(builder.insert(action), nextTime, nodeName);
			}
		}
		System.err.println("Warning: Failed to parse trace line.  Using entire line as type.");
		System.err.println(line);
		action = new Action(line);
		if (internActions) action = action.intern();
		return new Occurrence(builder.insert(action), incTime(prevTime), null);
	}

	/**  TODO: deprecated --> remove
	 * 
	 * Splits on all actions containing a <?sep> named field.
	 * 
	 * @param os The list of actions to separate.
	 * @return A list of lists, where each list (except potentially the last)
	 *     ends with a separator. 
	public ArrayList<ArrayList<Occurrence>> splitOnSeperators(List<Occurrence> os) {
		ArrayList<ArrayList<Occurrence>> result = new ArrayList<ArrayList<Occurrence>>();
		ArrayList<Occurrence> current = new ArrayList<Occurrence>();
		for (Occurrence o : os) {
			current.add(o);
			if (o.isSeperator()) {
				result.add(current);
				current = new ArrayList<Occurrence>();
			}
		}
		if (!current.isEmpty())
			result.add(current);
		return result;
	}
	*/
	
	/**
	 * Convenience function, yielding a graph for the specified file.
	 * 
	 * @param file The trace file to read
	 * @param linesToRead Maximum number of tracelines to read.  Negative if unlimited.
	 * @param partition True indicates partitioning the occurrences on the nodeName field.
	 * @return The resulting graph.
	 */
	public Graph<MessageEvent> readGraph(String file, int linesToRead, boolean partition) {
		this.builder = new GraphBuilder();
		List<Occurrence> set = this.parseTraceFile(file, linesToRead);
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
}