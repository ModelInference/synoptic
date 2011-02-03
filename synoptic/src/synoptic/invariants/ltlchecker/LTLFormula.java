package synoptic.invariants.ltlchecker;

public class LTLFormula {

    /**
     * Gives a string in which every toPrep(%) is surrounded by " For example
     * formula = did(a) and toPrep is did, then the result is "did(a)"
     * 
     * @param formula
     * @param toPrep
     * @return
     */
    private static String prepare(String formula, String toPrep) {
        StringBuilder strB = new StringBuilder();

        int len = toPrep.length();
        int oldPos = 0;
        int pos = formula.indexOf(toPrep + "(");
        int bracket = formula.indexOf(')', pos);
        while (pos != -1 && bracket != -1) {
            strB.append(formula.substring(oldPos, pos));
            strB.append("\"" + toPrep + "(");

            pos += len + 1;
            strB.append(formula.substring(pos, bracket + 1));
            strB.append("\"");

            oldPos = bracket + 1;
            pos = formula.indexOf(toPrep + "(", bracket);
            bracket = formula.indexOf(')', pos);
        }
        strB.append(formula.substring(oldPos));

        return strB.toString();
    }

    /**
     * Augment formula with did and can.
     * 
     * @param formula
     *            an LTL formula
     * @return a did/can-LTL formula
     */
    public static String prepare(String formula) {
        return prepare(prepare(formula, "did"), "can");
    }

    // public static void synoptic.main(String args[]) {
    // for(String str : args) {
    // System.out.println(str);
    // System.out.println(prepare(str));
    // System.out.println();
    // }
    // }
}
