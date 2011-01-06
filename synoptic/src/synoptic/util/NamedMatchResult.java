// Written by "clement.denis"
// http://code.google.com/p/named-regexp/
// Apache License 2.0

package synoptic.util;

import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;

public interface NamedMatchResult extends MatchResult {

	public List<String> orderedGroups();

	public Map<String, String> namedGroups();

	public String group(String groupName);

	public int start(String groupName);

	public int end(String groupName);

}
