package synoptic.invariants.ltlchecker;

public class LTLSyntaxChecker {

    /**
     * Checks the preprocessed version of the formula
     * 
     * @param formula
     * @return true, if it's correct syntax, false otherwise
     */
    public static boolean correctSyntax(String formula) {
        return checkExp(LTLFormulaPreprocessor.preprocessFormula(formula))
                .trim().equals("");
    }

    private static String checkExp(String str) {
        str = str.trim();
        String temp = checkPre(str).trim();

        if (temp.equals("")) {
            return "";
        } else if (temp.startsWith("->")) {
            return checkExp(temp.substring(2));
        } else if (temp.startsWith("U")) {
            return checkExp(temp.substring(1));
        } else if (temp.startsWith("&&")) {
            return checkExp(temp.substring(2));
        } else if (temp.startsWith("||")) {
            return checkExp(temp.substring(2));
        }

        return temp;
    }

    private static String checkPre(String str) {
        str = str.trim();

        if (str.startsWith("[]")) {
            return checkPre(str.substring(2));
        }
        if (str.startsWith("<>")) {
            return checkPre(str.substring(2));
        }
        if (str.startsWith("!")) {
            return checkPre(str.substring(1));
        }
        if (str.startsWith("X")) {
            return checkPre(str.substring(1));
        }

        return checkAtomic(str);
    }

    private static String checkAtomic(String str) {
        str = str.trim();

        if (str.startsWith("can(") || str.startsWith("did(")) {
            str = readUntilBracket(str.substring(4));
            return str;
        } else if (str.startsWith("(")) {
            String temp = checkExp(str.substring(1)).trim();
            if (temp.startsWith(")")) {
                return temp.substring(1).trim();
            }
        } else if (str.startsWith("true")) {
            return str.substring("true".length());
        } else if (str.startsWith("false")) {
            return str.substring("false".length());
        }

        return "failed";
    }

    private static String readUntilBracket(String str) {
        int i = 1;
        int j = 0, len = str.length();
        while (i > 0 || j > len) {
            if (str.charAt(j) == '(') {
                i++;
            } else if (str.charAt(j) == ')') {
                i--;
            }

            j++;
        } // j is after the bracket

        if (j > len) {
            return "failed";
        } else {
            return str.substring(j);
        }
    }
}
