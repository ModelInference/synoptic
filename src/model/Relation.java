package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//import daikon.inv.Invariant;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * A relation that supports storing frequencies and invariants.
 * @author Sigurd Schneider
 *
 * @param <StateType>
 */
public class Relation<StateType> extends Transition<StateType> {
	double frequency = 0.0;
//	private List<Invariant> inv = new ArrayList<Invariant>();

	/**
	 * Creates a relation.
	 * @param source source node
	 * @param target target node
	 * @param relation relation name
	 */
	public Relation(StateType source, StateType target, String relation){
		super(source, target, relation);
	}
	
	/**
	 * Creates a relation.
	 * @param source source node
	 * @param target target node
	 * @param relation relation name
	 * @param frequency frequency of the transition
	 */
	public Relation(StateType source, StateType target, String relation, double frequency) {
		super(source, target, relation);
		this.frequency = frequency;
	}

	/**
	 * Set the frequency of this transition.
	 * @param frequency the frequency value to set
	 */
	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
	
	/**
	 * Get the frequency of this transition.
	 * @return the frequency of this transition.
	 */
	public double getFrequency() {
		return frequency;
	}

	@Override
	public String toStringConcise() {
		Locale.setDefault(Locale.ENGLISH);
		//return getAction().toString()+(inv.size()==0 ? "" : " if "+inv)+", freq "+String.format("%.2f", frequency)+"";
		//return String.format("%.2f", frequency);
		return String.format("%.2f", frequency);
	}
	
//	@Override
//	public String toString() {
//		return getSource() + "-"+getRelation()+"-"+inv+"-("+frequency+")>"+getTarget();
//	}

	@Override
	public void setSource(StateType target) {
		throw new NotImplementedException();
	}

	@Override
	public void setTarget(StateType target) {
		throw new NotImplementedException();
	}

	/**
	 * Set the invariants of this transition.
	 * @param inv the invariants to set.
	 */
//	public void setInvariants(List<Invariant> inv) {
//		this.inv = inv;
//	}
}
