package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import synoptic.model.Event;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionCache;
import synoptic.tests.SynopticTest;

public class PartitionCacheTests extends SynopticTest {
	public static final PartitionCache pCache = new PartitionCache();
	public static final Partition testPart = new Partition(new EventNode(new Event("Test")));

	@Test
	public void getTest() {
		pCache.clear();
		pCache.getPartition(testPart);
		pCache.putPartition(testPart);
		Partition returnPart = pCache.getPartition(testPart);
		assertTrue(returnPart == testPart);
	}
	
	@Test(expected = IllegalStateException.class)
	public void illegalGetTest() {
		pCache.clear();
		pCache.getPartition(testPart);
		pCache.getPartition(testPart);
	}
	
	@Test(expected = IllegalStateException.class)
	public void illegalPutTest() {
		pCache.clear();
		pCache.putPartition(testPart);
	}
	
	@Test
	public void getAllTest() {
		pCache.clear();
		for (int i = 0; i < pCache.getSize(); i++) {
			pCache.getPartition();
		}
	}
	
	@Test
	public void getOverCurrentSizeTest() {
		pCache.clear();
		for (int i = 0; i < pCache.getSize() + 1; i++) {
			pCache.getPartition();
		}
	}
	
	@Test
	public void putAllTest() {
		pCache.clear();
		for (int i = 0; i < pCache.getSize(); i++) {
			Partition p = new Partition(new EventNode(new Event("" + i)));
			pCache.putPartition(p);
		}
	}
	
	@Test
	public void putOverCurrentSizeTest() {
		pCache.clear();
		for (int i = 0; i < pCache.getSize() + 1; i++) {
			Partition p = new Partition(new EventNode(new Event("" + i)));
			pCache.putPartition(p);
		}
	}

}
