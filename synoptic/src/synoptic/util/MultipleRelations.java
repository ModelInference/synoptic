package synoptic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MultipleRelations {

    /** Suppress default constructor for non-instantiability */
    private MultipleRelations() {
        throw new AssertionError();
    }

    /**
     * Implements a comparator of to sets of relation strings.
     */
    static public int compareMultipleRelations(Set<String> r1, Set<String> r2) {
        // If both the sources and the targets are equal then we use the
        // relations for possible disambiguation.
        int cmpRelationsLen = ((Integer) (r1.size())).compareTo(r2.size());

        // Compare relation set lengths.
        if (cmpRelationsLen != 0) {
            return cmpRelationsLen;
        }

        // Sort the relations sets as lists.
        List<String> rList = new ArrayList<String>(r1);
        Collections.sort(rList, new Comparator<String>() {
            @Override
            public int compare(String arg0, String arg1) {
                return arg0.compareTo(arg1);
            }
        });

        List<String> rOtherList = new ArrayList<String>(r2);
        Collections.sort(rList, new Comparator<String>() {
            @Override
            public int compare(String arg0, String arg1) {
                return arg0.compareTo(arg1);
            }
        });

        // Pair-wise compare the sorted lists of relations.
        for (int i = 0; i < rList.size(); i++) {
            int cmp = rList.get(i).compareTo(rOtherList.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }
}
