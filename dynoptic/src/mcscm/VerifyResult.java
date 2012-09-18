package mcscm;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import synoptic.model.channelid.ChannelId;

/**
 * The result of running the McScM verify tool. Contains parsing utilities for
 * parsing the output of the verify tool.
 */
public class VerifyResult {
    static String safeRe = "^Result: Model is safe.";
    static String unsafeRe = "^Result: Model is unsafe.*";
    static String syntaxErrRe = "^Syntaxical error:.*";
    static String counterExRe = "^Counterexample:";
    static String counterExEventRe = "^.*\\|- (.*) -\\|.*";

    private boolean modelIsSafe;

    private McScMCExample cExample = null;

    private final List<ChannelId> cids;

    List<String> verifyRawLines;

    /**
     * Creates a new VerifyResult from the raw verify output, broken into lines.
     * 
     * @param verifyRawOutput
     * @throws Exception
     */
    public VerifyResult(List<String> verifyRawLines, List<ChannelId> cids)
            throws VerifyOutputParseException {
        this.cids = cids;
        this.verifyRawLines = verifyRawLines;
        parseVerifyOutput(verifyRawLines);
    }

    public boolean modelIsSafe() {
        return this.modelIsSafe;
    }

    public McScMCExample getCExample() {
        return cExample;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        if (cExample != null) {
            return "Verify-CExample: " + this.cExample.toString();
        }
        return "Verify-safe";
    }

    public String toRawString() {
        String ret = "";
        for (String line : verifyRawLines) {
            ret += line + "\n";
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    private void parseVerifyOutput(List<String> lines)
            throws VerifyOutputParseException {

        boolean detectedSafety = false;
        int lineCnt = 0;
        for (String line : lines) {
            if (line.matches(syntaxErrRe)) {
                throw new ScmSyntaxException(line);
            }

            if (line.matches(safeRe)) {
                this.modelIsSafe = true;
                detectedSafety = true;
            }
            if (line.matches(unsafeRe)) {
                this.modelIsSafe = false;
                detectedSafety = true;
            }
            if (line.matches(counterExRe)) {
                parseCounterExample(lines, lineCnt);
            }
            lineCnt += 1;
        }
        if (!detectedSafety) {
            throw new VerifyOutputParseException(
                    "Unable to parse verify result: cannot determine model safety");
        }
    }

    /**
     * Parses and sets the counter-example sequence of actions corresponding to
     * the property violation of the model.
     * 
     * @param lines
     * @param lineCnt
     *            Index of the line in lines that contains the "Counterexample:"
     *            header of the counterexample section in the output.
     */
    private void parseCounterExample(List<String> lines, int lineCnt) {
        cExample = new McScMCExample(cids);
        Pattern p = Pattern.compile(counterExEventRe);

        for (String line : lines.subList(lineCnt + 1, lines.size())) {
            Matcher m = p.matcher(line);
            if (m.find()) {
                assert m.groupCount() == 1;
                cExample.addScmEventStrToPath(m.group(1));
            }
        }
    }

}
