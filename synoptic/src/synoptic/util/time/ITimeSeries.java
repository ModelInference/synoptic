package synoptic.util.time;

import java.util.ArrayList;
import java.util.List;

public class ITimeSeries<TimeType extends ITime> {
	List<TimeType> times;
	
	public ITimeSeries() {
		times = new ArrayList<TimeType>();
	}
	
}
