package mcscm;

import java.util.List;

/**
 * Represents the result of running the McScM verify tool and contains parsing
 * utilities for parsing the output of the verify tool.
 */
public class VerifyResult {
    static String safeRe = "^Result: Model is safe.";
    static String unsafeRe = "^Result: Model is unsafe.*";
    static String syntaxErrRe = "^Syntaxical error:.*";
    static String counterExRe = "^Counterexample:";

    boolean modelIsSafe;

    /**
     * Creates a new VerifyResult from the raw verify output, broken into lines.
     * 
     * @param verifyRawOutput
     * @throws Exception
     */
    public VerifyResult(List<String> verifyRawLines)
            throws VerifyOutputParseException {
        parseVerifyOutput(verifyRawLines);
    }

    public boolean modelIsSafe() {
        return this.modelIsSafe;
    }

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
    void parseCounterExample(List<String> lines, int lineCnt) {
        // TODO: parse and set the counter-example path here.

        // Pattern safeP = Pattern.compile(counterExRe);
        // Matcher m = safeP.matcher(line);
        // if (m.find()) {
        //
        // }
    }
}
