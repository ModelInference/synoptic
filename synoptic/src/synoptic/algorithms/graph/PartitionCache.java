package synoptic.algorithms.graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import synoptic.model.EventNode;
import synoptic.model.Partition;

public class PartitionCache {
	private Set<Partition> cache;
	
	public PartitionCache() {
		cache = new HashSet<Partition>();
	}
	
	// Confused here. Why replace constructor
	// calls with pcache calls if node is not
	// going to be in partition?
	
	// Also, calling get with a pointer to a
	// partition seems redundant
	
	// All of the get functions seem redundant
	// what am I missing here?
	
	public Partition get(Set<EventNode> eNodes) {
        return null;
	}
	
	public Partition get(EventNode eNode) {
        return null;
	}
	
	public Partition get(Partition part) {
        return null;
	}
	
	public boolean put(Partition part) {
		return false;
	}

}
