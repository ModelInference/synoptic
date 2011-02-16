package synoptic.model.export;

import java.util.LinkedHashMap;



/**
 * A class to generate unique names for objects.
 * 
 * @author Sigurd Schneider
 * @param <T>
 *            the type of the objects
 */
public class LabelEnumerator<T> {
    LinkedHashMap<T, String> displayNames = new LinkedHashMap<T, String>();
    int state_ctr = 0;

    /**
     * Allows generating unique display names for visualizing states.
     * 
     * @param s
     * @return a unique name for s
     */
    public String getDisplayName(T s) {
        if (!displayNames.containsKey(s)) {
            displayNames.put(s, s.toString() + state_ctr++);
        }
        return displayNames.get(s);
    }

}
