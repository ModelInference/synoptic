// Written by "clement.denis"
// http://code.google.com/p/named-regexp/
// Apache License 2.0

package synoptic.util.matching;

import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;

public interface INamedMatchResult extends MatchResult {

    List<String> orderedGroups();

    Map<String, String> namedGroups();

    String group(String groupName);

    int start(String groupName);

    int end(String groupName);
}
