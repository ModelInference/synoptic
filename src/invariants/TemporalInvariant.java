package invariants;

import java.util.List;
import java.util.Set;

import model.Action;
import model.interfaces.INode;

public interface TemporalInvariant {
	public String getLTLString();
	public <T extends INode<T>> List<T> shorten(List<T> path);
	public gov.nasa.ltl.graph.Graph getAutomaton();
	public Action getRelation();
	public Set<String> getLabels();
	public String getShortName();
}
