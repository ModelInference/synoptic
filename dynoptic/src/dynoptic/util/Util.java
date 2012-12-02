package dynoptic.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.util.Pair;

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
        List<List<T>> ret = newList();
        for (T e1 : list1) {
            for (T e2 : list2) {
                List<T> combo = newList();
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
     * returns an updated (mutated) version of the first list, containing:
     * 
     * <pre>
     *  [[a,d,f],[a,e,f],    <- first list with f appended
     *   [a,d,g],[a,e,g],]   <- first list with g appended
     * </pre>
     * 
     * NOTE: mutates listList1
     */
    public static <T> List<List<T>> get2DPermutations(List<List<T>> listList1,
            Iterable<T> list2) {
        List<List<T>> ret = newList();
        for (List<T> list1 : listList1) {
            for (T e2 : list2) {
                List<T> combo = newList();
                combo.addAll(list1);
                combo.add(e2);
                ret.add(combo);
            }
        }
        return ret;
    }

    /**
     * Generic factory methods to make declarations more concise, via type
     * inference.
     */
    public static <K, V> Map<K, V> newMap() {
        return new LinkedHashMap<K, V>();
    }

    public static <V> Set<V> newSet() {
        return new LinkedHashSet<V>();
    }

    public static <V> Set<V> newSet(Set<V> s) {
        return new LinkedHashSet<V>(s);
    }

    public static <V> Set<V> newSet(int initCap) {
        return new LinkedHashSet<V>(initCap);
    }

    public static <V> List<V> newList() {
        return new ArrayList<V>();
    }

    public static <V> List<V> newList(Collection<V> c) {
        return new ArrayList<V>(c);
    }

    public static <V> List<V> newList(int initCap) {
        return new ArrayList<V>(initCap);
    }

    public static <L, R> Pair<L, R> newPair(L l, R r) {
        return new Pair<L, R>(l, r);
    }

}
