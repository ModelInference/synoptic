package synoptic.util.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Parses and manipulates substitution strings.
public class NamedSubstitution {
    public static Pattern matchReference = Pattern.compile("\\\\k<(\\w*)>");

    // Even elements are regular strings, odd reference group names.
    // should be odd length.
    List<String> contents;

    public NamedSubstitution(String subst) {
        contents = new ArrayList<String>();
        Matcher matcher = matchReference.matcher(subst);
        int prev = 0;
        while (matcher.find()) {
            int nxt = matcher.start();
            contents.add(subst.substring(prev, nxt));
            contents.add(matcher.group(1));
            prev = matcher.end();
        }
        contents.add(subst.substring(prev, subst.length()));
    }

    public void concat(NamedSubstitution other) {
        contents.get(contents.size() - 1).concat(other.contents.get(0));
        for (int i = 1; i < other.contents.size(); i++) {
            contents.add(other.contents.get(i));
        }
    }

    public String substitute(Map<String, String> smap) {
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < contents.size(); i += 2) {
            result.append(contents.get(i - 1));
            result.append(smap.get(contents.get(i)));
        }
        result.append(contents.get(contents.size() - 1));
        return result.toString();
    }

    @Override
    public String toString() {
        return contents.toString();
    }
}
