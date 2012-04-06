package synoptic.util.time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ITimeSeries<TimeType extends ITime> {
	List<TimeType> times;
	
	public ITimeSeries() {
		times = new ArrayList<TimeType>();
	}
	
	/**
     * @return 
     * 		mode delta time for transition, null if transition has zero delta times.
     */
    public TimeType computeMode() {
    	
    	if (this.times.isEmpty()) {
            return null;
        }
    	
        Map<TimeType, Integer> counts = new HashMap<TimeType, Integer>();
        TimeType mostCommon = null;
        int max = 1;
        for (TimeType delta : times) {
            Integer count = counts.get(delta);

            if (count == null) {
                count = 1;
            } else {
                count++;
            }

            if (count > max) {
                mostCommon = delta;
                max = count;
            }

            counts.put(delta, count);
        }

        return mostCommon;
    }
	
    /**
     * @return 
     * 		median delta time for transition, null if transition has zero delta times.
     */
    public TimeType computeMedian() {
        
        if (this.times.isEmpty()) {
            return null;
        }
        
        Collections.sort(this.times);

        // Simple case of picking about the middle every time.
        // TODO Calculate between the halfway values if the size
        // of the list is even.
       
        return times.get((times.size() / 2));
    }
    
    /**
     * @return
     * 		mean delta time for transition, null if transition has zero delta times.
     */
    //TODO implement function
    public TimeType computeMean() {	
    	if (times.isEmpty()) {
            return null;
        }
    
    	for (TimeType t : times) {
    	
    	}
    	
    	return null;
    }
    
    public List<TimeType> getTimes() {
    	return times;
    }
    
    /**
     * Adds a time for the transition between the source and target nodes.
     * 
     * @param delta
     *            The time between nodes.
     */
    public void addDelta(TimeType t) {
    	assert t != null;
    	times.add(t);
    }
    
    /**
     * Adds a collection of times for transition between source and target
     * nodes.
     * 
     * @param deltas
     */
    public void addAllDeltas(Collection<TimeType> deltas) {
        assert deltas != null;
       times.addAll(deltas);
    }
    
    
}
