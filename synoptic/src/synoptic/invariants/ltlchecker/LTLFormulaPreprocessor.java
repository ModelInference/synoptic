package synoptic.invariants.ltlchecker;

public class LTLFormulaPreprocessor {

    private static final String UFAIR = "UFAIR";
    private static final String SFAIR = "SFAIR";
    private static final String WFAIR = "WFAIR";

    public static String preprocessFormula(String formula) {

        // formula form -> UFAIR(W);...;<formula>"
        String parts[] = formula.split(";");

        String processedFairness[] = new String[parts.length - 1];
        for (int i = 0; i < parts.length - 1; i++) { // -1 because last isn't
            // checked
            processedFairness[i] = processFairness(parts[i]);
        }

        StringBuilder resultingFormula = new StringBuilder();
        resultingFormula.append(processedFairness.length > 0 ? "(" : "");
        for (int i = 0; i < processedFairness.length; i++) {
            resultingFormula.append(processedFairness[i]);
            if (i + 1 < processedFairness.length) {
                resultingFormula.append(" && ");
            }
        }
        resultingFormula.append(processedFairness.length > 0 ? ") -> " : "");

        resultingFormula.append("( "
                + replaceDidCanSets(parts[parts.length - 1]) + " )");
        return resultingFormula.toString();
    }

    private static String replaceDidCanSets(String formula) {
        String result = replace(replace(formula, "did"), "can");
        return result;
    }

    private static String replace(String formula, String pattern) {
        int lastPos = 0;
        StringBuilder replaced = new StringBuilder();

        int pos = formula.indexOf(pattern + "(", lastPos);
        while (pos != -1) {
            int end = formula.indexOf(")", pos);
            if (end == -1) {
                return formula; // syntax error
            }

            replaced.append(formula.substring(lastPos, pos));
            replaced.append("(");
            String set[] = formula.substring(pos + pattern.length() + 1, end)
                    .split(",");
            for (int i = 0; i < set.length; i++) {
                replaced.append(pattern + "(" + set[i] + ")");
                if (i + 1 < set.length) {
                    replaced.append(" || ");
                }
            }
            replaced.append(")");

            lastPos = end + 1;
            pos = formula.indexOf(pattern + "(", lastPos);
        }
        replaced.append(formula.substring(lastPos));

        return replaced.toString();
    }

    private static String processFairness(String formula) {
        formula = formula.trim();
        if (formula.startsWith(UFAIR + "(")) {
            if (!formula.endsWith(")")) {
                return formula;
            }
            return replaceDidCanSets("[]<> did("
                    + formula.substring(UFAIR.length() + 1,
                            formula.length() - 1) + ")");
        } else if (formula.startsWith(SFAIR + "(")) {
            if (!formula.endsWith(")")) {
                return formula;
            }
            String set = formula.substring(SFAIR.length() + 1,
                    formula.length() - 1);
            return replaceDidCanSets("( []<> can(" + set + ") -> []<> did("
                    + set + ") )");
        } else if (formula.startsWith(WFAIR + "(")) {
            if (!formula.endsWith(")")) {
                return formula;
            }
            String set = formula.substring(WFAIR.length() + 1,
                    formula.length() - 1);
            return replaceDidCanSets("( <>[] can(" + set + ") -> []<> did("
                    + set + ") )");
        }
        return null;
    }

    // DEBUG Main just to test & debug
    public static void main(String[] args) {
        // Method to test the result
        String formulas[] = { "did(a,b,c)", "can(a,b,c) -> did(b)",
                "WFAIR(a,b,c); <>did(a,b,c)",
                "WFAIR(a,b,c); UFAIR(d,e,f); <>did(a,b,c)" };
        for (String s : formulas) {
            System.out.println(s + " becomes to " + preprocessFormula(s));
        }
    }
}
