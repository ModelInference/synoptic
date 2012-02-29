package synopticgwt.client.model;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A class designed to interface directly with an instance of a dracula node. If
 * this class is empty, it is simply because there were no methods that could
 * clean up code by placing them here. This essentially allows a field to be
 * defined on the java server and then be serialized to the client.
 * 
 * @author andrew
 */
public class JSEdge extends JavaScriptObject implements Serializable {

    private static final long serialVersionUID = 1L;

    // JSO types always have empty constructors.
    protected JSEdge() {
    }
}
