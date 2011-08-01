package synoptic.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PartitionCache {
	private static final int DEFAULT_SIZE = 50;
	private List<Partition> free;
	private List<Partition> allocated;
	private Random random;
	
	public PartitionCache(int size) {
		free = new ArrayList<Partition>(size);
		allocated = new ArrayList<Partition>(size);
		random = new Random();
	}
	
	public PartitionCache() {
		this(DEFAULT_SIZE);
	}
	
	public void clear() {
		free.clear();
		allocated.clear();
	}
	
	public Partition getPartition(Partition partition) {
		if (allocated.contains(partition)) {
			throw new IllegalStateException("Partition already allocated");
		}
		
		allocated.add(partition);
		return partition;
	}
	
	public Partition getPartition() {
		int range = free.size();
		int index = random.nextInt(range);
		Partition partition = free.remove(index);
		allocated.add(partition);
		return partition;
	}
	
	public void putPartition(Partition partition) {
		if (!allocated.contains(partition)) {
			throw new IllegalStateException("Partition is not allocated.");
		} else if (free.contains(partition)) {
			throw new IllegalStateException("Partition already freed.");
		}
		allocated.remove(partition);
		free.add(partition);		
	}
	
	public int getSize() {
		return allocated.size() + free.size();
	}
}
