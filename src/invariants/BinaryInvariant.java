package invariants;

import java.util.HashSet;
import java.util.Set;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.trans.LTL2Buchi;
import gov.nasa.ltl.trans.ParseErrorException;
import invariants.ltlchecker.LTLFormula;
import model.Action;

public abstract class BinaryInvariant implements TemporalInvariant {
	protected String first;
	protected String second;
	protected Action relation;
	private Graph automaton;

	public BinaryInvariant(String typeFrist, String typeSecond, Action relation) {
		this.first = typeFrist;
		this.second = typeSecond;
		this.relation = relation;
	}
	
	public String toString() {
		return getLTLString();
	}
	
	public Action getRelation() {
		return relation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getClass().hashCode();
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result
				+ ((relation == null) ? 0 : relation.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BinaryInvariant other = (BinaryInvariant) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (relation == null) {
			if (other.relation != null)
				return false;
		} else if (!relation.equals(other.relation))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}
	
	@Override
	public gov.nasa.ltl.graph.Graph getAutomaton() {
		try {
			if (automaton == null) {
				String formula = getLTLString();
				if (useDIDCAN)
					formula = LTLFormula.prepare(getLTLString());
				automaton = LTL2Buchi.translate("! (" + formula + ")");
			}
			return automaton;
		} catch (ParseErrorException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Set<String> getPredicates() {
		Set<String> set = new HashSet<String>();
		set.add(first);
		set.add(second);
		return set;
	}
	
	public String getFirst() {
		return first;
	}
	
	public String getSecond() {
		return second;
	}
}
