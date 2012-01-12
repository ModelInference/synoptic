package synopticgwt.client.util;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Implements methods for manipulating JavaScript data structures.
 */
public class JsniUtil {
    // //////////////////////////////////////////////////////////////////////////
    // JSNI methods -- JavaScript Native Interface methods. The method body of
    // these calls is pure JavaScript.

    /**
     * A JSNI method for adding a String element to a java script array object.
     * (Yes, this is rather painful.)
     * 
     * @param array
     *            Array object to add to
     * @param s
     *            element to add
     */
    public native static void pushArray(JavaScriptObject array, String s) /*-{
		array.push(s);
    }-*/;

    /**
     * A JSNI method for associating a key in an array to a value. (Yes, this is
     * rather painful.)
     * 
     * @param array
     *            Array object to add to
     * @param key
     * @param val
     */
    public native static void addToKeyInArray(JavaScriptObject array,
            String key, String val) /*-{
		if (!(key in array)) {
			array[key] = [];
		}
		array[key].push(val);
    }-*/;

    // </JSNI methods>
    // //////////////////////////////////////////////////////////////////////////
}
