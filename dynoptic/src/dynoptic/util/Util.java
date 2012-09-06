package dynoptic.util;

import java.util.ArrayList;
import java.util.List;

public final class Util {

    /**
     * Given two lists:
     * 
     * <pre>
     * 1: [a,b,c] and
     * 2: [d,e]
     * </pre>
     * 
     * returns a list of all pairs where first/second element of each pair is
     * from the first/second list:
     * 
     * <pre>
     * [[a,d],[a,e],[b,d],[b,e],[c,d],[c,e]]
     * </pre>
     */
    public static <T> List<List<T>> get2DPermutations(Iterable<T> list1,
            Iterable<T> list2) {
        List<List<T>> ret = new ArrayList<List<T>>();
        for (T e1 : list1) {
            for (T e2 : list2) {
                List<T> combo = new ArrayList<T>();
                combo.add(e1);
                combo.add(e2);
                ret.add(combo);
            }
        }
        return ret;
    }

    /**
     * Given two lists:
     * 
     * <pre>
     * 1: [[a,d],[a,e]] and
     * 2: [f,g],
     * </pre>
     * 
     * returns a list containing:
     * 
     * <pre>
     *  [[a,d,f],[a,e,f],    <- first list with f appended
     *   [a,d,g],[a,e,g],]   <- first list with g appended
     * </pre>
     */
    public static <T> void get2DPermutations(List<List<T>> listList1,
            Iterable<T> list2) {
        for (List<T> list1 : listList1) {
            for (T e2 : list2) {
                list1.add(e2);
            }
        }
    }

}
