package synoptic.util.time;

import java.util.ArrayList;
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
}
