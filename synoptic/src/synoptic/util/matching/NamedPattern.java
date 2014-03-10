// Written by "clement.denis"
// http://code.google.com/p/named-regexp/
// Apache License 2.0

package synoptic.util.matching;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import synoptic.main.parser.ParseException;

public class NamedPattern {

    private static final Pattern NAMED_GROUP_PATTERN = Pattern
            .compile("\\(\\?<((\\w|\\*|\\-)+)>");
    private static final Pattern NAMED_GROUP_PREFIX = Pattern
            .compile("\\(\\?<");

    private final Pattern pattern;
    private final String namedPattern;
    private final List<String> groupNames = new LinkedList<String>();

    public static NamedPattern compile(String regex) throws Exception {
        return new NamedPattern(regex, 0);
    }

    public static NamedPattern compile(String regex, int flags)
            throws Exception {
        return new NamedPattern(regex, flags);
    }

    private NamedPattern(String regex, int i) throws Exception {
        namedPattern = regex;
        pattern = buildStandardPattern(regex);

        Matcher matcher = NAMED_GROUP_PATTERN.matcher(namedPattern);
        while (matcher.find()) {
            String g = matcher.group(1);
            if (groupNames.contains(g)) {
                String error = "The group name " + g
                        + " already appears in a regex: " + regex;
                ParseException parseException = new ParseException(error);
                parseException.setRegex(regex);
                throw parseException;
            }
            groupNames.add(g);
        }
    }

    public int flags() {
        return pattern.flags();
    }

    public NamedMatcher matcher(CharSequence seqInput) {
        return new NamedMatcher(this, seqInput);
    }

    Pattern pattern() {
        return pattern;
    }

    public String standardPattern() {
        return pattern.pattern();
    }

    public String namedPattern() {
        return namedPattern;
    }

    public List<String> groupNames() {
        return groupNames;
    }

    public String[] split(CharSequence input, int limit) {
        return pattern.split(input, limit);
    }

    public String[] split(CharSequence input) {
        return pattern.split(input);
    }

    @Override
    public String toString() {
        return namedPattern;
    }

    private static String repeatString(String str, int reps) {
        String result = "";
        for (int i = 0; i < reps; i++) {
            result.concat(str);
        }
        return result;
    }

    static Pattern buildStandardPattern(String namedPattern) throws Exception {
        String regularPattern = NAMED_GROUP_PATTERN.matcher(namedPattern)
                .replaceAll("(");
        Matcher errorMatch = NAMED_GROUP_PREFIX.matcher(regularPattern);
        if (errorMatch.find()) {
            StringBuilder err = new StringBuilder(
                    "Parse error in named pattern:\n");
            err.append(namedPattern);
            err.append("\n");
            int prev = 0;
            while (true) {
                int nxt = errorMatch.start();
                err.append(repeatString(" ", nxt - prev - 1));
                err.append("^");
                prev = nxt;
                if (!errorMatch.find()) {
                    break;
                }
            }
            throw new Exception(err.toString());
        }
        return Pattern.compile(regularPattern);
    }

}
