package com.vetta.synos.model;

/**
 * @model
 * @author Ricardo Giacomin
 *
 */
public interface Association {

	/**
	 * @model
	 */
	public AssociationEnd getFirstEnd();

	/**
	 * @model
	 */
	public AssociationEnd getSecondEnd();

	/**
	 * @model
	 */
	public String getName();
}
