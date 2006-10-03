package com.vetta.synos.model;

import java.util.List;

/**
 * @model
 * @author Ricardo Giacomin
 *
 */
public interface Equipment {

	/**
	 * @model
	 */
	public EquipmentClass getEquipmentClass();

	/**
	 * @model type="EquipmentAttribute" containment="true" 
	 */
	public List getAttributes();
	

	/**
	 * @model opposite="children"
	 */
	public Equipment getContainer();
	
	/**
	 * @model type="Equipment" containmnet= "true" opposite="container"
	 */
	public List getChildren();
	

	/**
	 * @model
	 */
	public String getName();
}
