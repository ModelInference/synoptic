package synopticgwt.client;

import java.io.Serializable;

/**
 * A class that represents an explicit serialization policy white list --
 * contains objects that should be allowed to be serialized. This is necessary
 * for generic types and/or types that are not explicitly present in the
 * SynopticService interface. <br />
 * <br/>
 * This prevents errors of the form:
 * "com.google.gwt.user.client.rpc.SerializationException: Type
 * 'java.io.FileNotFoundException' was not included in the set of types which
 * can be serialized by this SerializationPolicy or its Class object could not
 * be loaded. For security purposes, this type will not be serialized." <br />
 * <br/>
 * This solution is borrowed from here:
 * 
 * <pre>
 * http://stackoverflow.com/questions/4202964/serializationpolicy-error-when-performing-rpc-from-within-gwt-application
 * </pre>
 */
public final class SerializableWhitelist implements Serializable {
    private static final long serialVersionUID = 1L;
}
