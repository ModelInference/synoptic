package dynoptic.util;

import java.util.ArrayList;
import java.util.List;

public final class Util {

    /**
     * Given [a,b,c] and [d,e], returns a new list containing:
     * [[a,d],[a,e],[b,d],[b,e],[c,d],[c,e]]
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
     * Given a list [[a,d],[a,e],[b,d],[b,e],[c,d],[c,e]] and [f,g], returns a
     * new list containing:
     * 
     * <pre>
     *  [[a,d,f],[a,e,f],[b,d,f],[b,e,f],[c,d,f],[c,e,f],   <- first list with f appended
     *   [a,d,g],[a,e,g],[b,d,g],[b,e,g],[c,d,f],[c,e,g]]   <- first list with g appended
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
