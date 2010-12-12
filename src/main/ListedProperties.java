package main;

import java.util.Properties;
import java.util.Vector;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Extends Java's Properties class with the ability to specify and recall
 * a list of property values corresponding to a key. This is used by Synoptic
 * to handle property files that encode Synoptic command line options, which
 * e.g. use the -r option to specify <i>multiple</i> regular expressions.
 *  
 * @author ivan
 */
public class ListedProperties extends Properties {
	
	/**
	 * For serializability
	 */
	private static final long serialVersionUID = 7939736199754405750L;
	
	/**
	 * Maintains lists of values per property key
	 */
	private HashMap<String, Vector<String>> propertyVectors;

	public ListedProperties() {
		super();
		propertyVectors = new HashMap<String, Vector<String>>();
	}

	/**
	 * Hook that replicates behavior of super and also updates propertyVectors
	 * to maintain a list of property values per key
	 */
	@Override
	public Object put(Object key, Object value) {
		// Properties does not assert that key and value are Strings but it
		// discourages non-String keys and values. No loss in being more strict.
		assert key instanceof String;
		assert value instanceof String;

		Vector<String> currVal = this.propertyVectors.get(key);
		if (currVal == null) {
			// first time we see this key
			currVal = new Vector<String>();
			this.propertyVectors.put((String) key, currVal);
		}
		// extend the list of values corresponding to this key with one more value 
		currVal.add((String) value);

		// replicate super's behavior
		return super.put(key, value);
	}

	/**
	 * Constructs an array that can be interpreted by plume library as command line
	 * arguments to Synoptic. The property key becomes the command line option and
	 * the list of property values is broken out into specification of the
	 * same option that appears multiple times with different values.
	 * 
	 * @return string array corresponding to the properties command line
	 */
	public String[] getCmdArgsLine() {
		// get all the keys
		Enumeration keys = propertyNames();
		// we'll construct the vector first, and then convert it to String[]
		Vector<String> argsVector = new Vector<String>();
		// list of values for a single key
		Vector<String> keyVals;
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			keyVals = this.propertyVectors.get(key);
			Iterator<String> itr = keyVals.iterator();
			while (itr.hasNext()) {
				// key = command line option 
				argsVector.add(key);
				// each value = command line option value
				argsVector.add(itr.next());
			}
		}
		String[] argsArray = new String[argsVector.size()];
		argsVector.toArray(argsArray);
		return argsArray;
	}

}
