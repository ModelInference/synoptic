package synoptic.statistics;

import java.util.HashMap;
import java.util.Set;

import synoptic.model.IEvent;


public class FrequencyMiner<T extends IEvent> {
	HashMap<String, Integer> frequencies = new HashMap<String, Integer>();
	public FrequencyMiner(Set<T> events) {
		for (T e : events) {
			if (!frequencies.containsKey(e.getName())) 
				frequencies.put(e.getName(), new Integer(0));
			frequencies.put(e.getName(), frequencies.get(e.getName())+1);
		}
	}
	
	public HashMap<String, Integer> getFrequencies() {
		return frequencies;
	}
	
	public String toString() {
		return frequencies.toString();
	}
}
