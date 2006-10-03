package com.vetta.synos.model;

import java.util.List;

/**
 * @model
 * @author Ricardo Giacomin
 *
 */
public interface EquipmentClass {

	/**
	 * @model type="EquipmentProperty" containment="true" 
	 */
	public List getProperties();

	/**
	 * @model
	 */
	public String getType();
}
