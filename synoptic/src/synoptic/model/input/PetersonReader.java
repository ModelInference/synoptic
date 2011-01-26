package synoptic.model.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import synoptic.main.TraceParser.Occurrence;
import synoptic.model.Action;
import synoptic.model.IEvent;


public class PetersonReader<T extends IEvent> {
	IBuilder<T> builder;
	HashMap<String, T> messageMap = new HashMap<String, T>();
	boolean identifyMessages = false;
	HashMap<String, Integer> roundCounter = new HashMap<String, Integer>();

	public PetersonReader(IBuilder<T> builder) {
		this.builder = builder;
		this.identifyMessages = false;
	}

	public PetersonReader(IBuilder<T> builder, boolean identifyMessages) {
		this.builder = builder;
		this.identifyMessages = identifyMessages;
	}

	class Occurence {
		public T message;
		private VectorTime time;
		public String nodeName;

		public Occurence(T message, VectorTime time, String nodeName) {
			this.message = message;
			this.time = time;
			this.nodeName = nodeName;
		}

		public VectorTime getTime() {
			return time;
		}
	}

	/*
	 * <node_id, tstamp, [recv|send|round-done], mtype, roundid, payload,
	 * msg_id>
	 */
	private Occurence readLine(String line) {
		line = line.trim();
		if (line.length() == 0 || line.charAt(0) == '#')
			return null;
		String[] fields = line.split(" ");
		String nodeName = fields[0];
		Action action = new Action(fields[2]);
		VectorTime time = new VectorTime(fields[1]);
		// action.setTime(time);
		if (fields[2].equals("round-done")) {
			roundCounter.put(nodeName, roundCounter.get(nodeName)+1);
			return null;
		}
		action.setStringArgument("nodeName", nodeName);
		action.setStringArgument("mtype", fields.length > 3 ? fields[3] : "");
		String roundId = fields.length > 4 ? fields[4] : "";
		action.setStringArgument("roundId", roundId);
		String payload = fields.length > 5 ? fields[5] : "";
		action.setStringArgument("payload", payload);
		String messageId = fields.length > 6 ? fields[6] : "";
		action.setStringArgument("id", messageId);
		if (!roundCounter.containsKey(nodeName))
			roundCounter.put(nodeName, 0);
		action.setStringArgument("localRoundId", roundCounter.get(nodeName).toString());
		action = action.intern();
		T message = null;
		if (!messageId.equals("") && identifyMessages) {
			if (messageMap.containsKey(messageId))
				message = messageMap.get(messageId);
			else {
				message = builder.insert(action);
				messageMap.put(messageId, message);
			}
		} else
			message = builder.insert(action);
		return new Occurence(message, time, nodeName);
	}

	private List<Occurence> readFile(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		ArrayList<Occurence> occurences = new ArrayList<Occurence>();
		while ((line = reader.readLine()) != null) {
			Occurence o = readLine(line);
			if (o != null)
				occurences.add(o);
		}
		reader.close();
		return occurences;
	}

	public List<Occurence> readSet(String baseName) throws IOException {
		List<Occurence> set = new ArrayList<Occurence>();
		for (int i = 1; i <= 5; ++i) {
			List<Occurence> messages = readFile(baseName.replace("?", "" + i));
			set.addAll(messages);
		}
		return set;
	}

	public void readGraphSet(String baseName, int n) throws IOException {
		//System.out.print("Reading Graph set");
		for (int i = 1; i <= n; ++i) {
			readGraphDirect(baseName.replace("?", "" + i));
			builder.split();
		//	System.out.print(".");
		}
		//System.out.println();
	}

	public void readGraph(String baseName) throws IOException {
		List<Occurence> set = readFile(baseName);
		final String relation = "t";
		for (Occurence m1 : set) {
			if (m1.time.isUnitVector())
				this.builder.addInitial(m1.message, relation);
			for (Occurence m2 : set) {
				if (m1.time.lessThan(m2.time)) {
					builder.connect(m1.message, m2.message, relation);
				}
			}
		}
	}

	public void readGraphDirect(String baseName) throws IOException {
		List<Occurence> set = readFile(baseName);
		// generateDirectTemporalRelation(set, g, null, "t");
		generateDirectTemporalRelation(set, "i", true);
	}

	private void generateDirectTemporalRelation(List<Occurence> set,
			String relation, boolean nodeInternal) {
		HashMap<Occurence, HashSet<Occurence>> directSuccessors = new HashMap<Occurence, HashSet<Occurence>>();
		Set<Occurence> noPredecessor = new HashSet<Occurence>(set);
		for (Occurence m1 : set) {
			directSuccessors.put(m1, new HashSet<Occurence>());
			for (Occurence m2 : set) {
				if (nodeInternal && !m2.nodeName.equals(m1.nodeName))
					continue;
				if (m1.getTime().lessThan(m2.getTime())) {
					//System.out.println(m1.nodeName + " vs " + m2.nodeName);
					boolean add = true;
					List<Occurence> removeSet = new ArrayList<Occurence>();
					for (Occurence m : directSuccessors.get(m1)) {
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

		for (Occurence m : directSuccessors.keySet())
			for (Occurence s : directSuccessors.get(m)) {
				builder.connect(m.message, s.message, relation);
				noPredecessor.remove(s);
			}
		for (Occurence m : noPredecessor)
			builder.addInitial(m.message, relation);
	}
}
