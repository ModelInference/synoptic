package synoptic.model.nets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SimulationChecker {
	private Net net;
	private HashMap<Place, Integer> marking = new HashMap<Place, Integer>();
	
	public SimulationChecker(Net net) {
		this.net = net;
		for (Place p : net.getPlaces()) {
			marking.put(p, net.getPre(p).size() == 0 ? 1 : 0);
		}
	}
	
	public boolean tryPerform(String eventName) {
		out: for (Event e : net.getEvents()) {
			if (!e.getName().equals(eventName))
				continue;
			// check precondition
			for (Place p : net.getPre(e)) {
				if (marking.get(p) < Math.max(1, net.getPre(p).size()))
					continue out;
			}
			// perform step
			for (Place p : net.getPre(e)) {
				marking.put(p, marking.get(p)-Math.max(1, net.getPre(p).size()));
			}
			for (Place p : net.getPost(e)) {
				marking.put(p, marking.get(p)+1);
			}
			// we did it
			return true;
		}
		return false;
	}

	public int getTokens(Place p) {
		return marking.get(p);
	}

	public Set<String> isFinishedReason() {
		Set<String> reason = new HashSet<String>();
		for (Place p : net.getPlaces()) {
			if (p.getPost().size() != 0 && marking.get(p) > 0) {
				//System.out.println(marking.get(p) + " tokens on " + p);
				reason.add(p.toString());
			}
		}
		return reason;
	}
	
	public boolean isFinished() {
		return isFinishedReason().size() == 0; 
	}
}
