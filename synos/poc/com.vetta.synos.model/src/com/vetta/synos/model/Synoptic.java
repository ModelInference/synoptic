package com.vetta.synos.model;

import java.util.List;

/**
 * @model
 * @author Ricardo Giacomin
 *
 */
public interface Synoptic {

	/**
	 * @model type="Equipment" containment="true"
	 */
	public List getEquipments();
	
	/**
	 * @model type="Association" containment="true"
	 */
	public List getAssociations();
	
	/**
	 * @model
	 */
	public String getTitle(); 
}
