package model.export;

import java.util.HashMap;

public class LabelEnumerator<T> {
	HashMap<T, String> displayNames = new HashMap<T, String>();
	int state_ctr = 0;

	/**
	 * Allows generating unique display names for visualizing states.
	 * 
	 * @param s
	 * @return
	 */
	public String getDisplayName(T s) {
		if (!displayNames.containsKey(s)) {
			displayNames.put(s, s.toString() + (state_ctr++));
		}
		return displayNames.get(s);
	}

}
