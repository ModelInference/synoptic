package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import daikon.inv.Invariant;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Relation<StateType> extends Transition<StateType> {
	double frequency = 0.0;
	private List<Invariant> inv = new ArrayList<Invariant>();

	public Relation(StateType source, StateType target, Action action){
		super(source, target, action);
	}
	
	public Relation(StateType source, StateType target, Action action, double probability) {
		super(source, target, action);
		this.frequency = probability;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
	
	public double getFrequency() {
		return frequency;
	}

	public String toStringConcise() {
		Locale.setDefault(Locale.ENGLISH);
		//return getAction().toString()+(inv.size()==0 ? "" : " if "+inv)+", freq "+String.format("%.2f", frequency)+"";
		//return String.format("%.2f", frequency);
		return String.format("%.2f", frequency);
	}
	
	public String toString() {
		return getSource() + "-"+getAction()+"-"+inv+"-("+frequency+")>"+getTarget();
	}

	@Override
	public void setSource(StateType target) {
		throw new NotImplementedException();
	}

	@Override
	public void setTarget(StateType target) {
		throw new NotImplementedException();
	}

	public void setInvariants(List<Invariant> inv) {
		this.inv = inv;
	}
}
