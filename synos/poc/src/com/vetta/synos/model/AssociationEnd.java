package com.vetta.synos.model;

/**
 * @model
 * @author Ricardo Giacomin
 *
 */
public interface AssociationEnd {

	/**
	 * @model
	 */
	public Equipment getEquipment();
	
	/**
	 * @model
	 */
	public String getName();
	
	/**
	 * @model
	 */
	public boolean getIsNavigable();
}
