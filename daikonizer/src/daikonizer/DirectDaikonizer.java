package daikonizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import daikon.ValueTuple;
import daikon.inv.Invariant;
import daikon.inv.InvariantStatus;
import daikon.util.Intern;

//import synoptic.model.event.Event;
//import synoptic.model.interfaces.INode;
//import synoptic.model.interfaces.ITransition;

/**
 * This attempts to use Daikon as a library (I think).
 */
public class DirectDaikonizer {

    // TODO: The current code doesn't compile.
    /*
    public static <T extends INode<T>> void generateStructuralInvariants(
            Set<T> hasPredecessor, Set<T> hasNoPredecessor,
            Set<T> isPredecessor, Set<T> isNoPredecessor,
            HashMap<String, ArrayList<T>> partitions, String label1,
            String label2) throws Exception {
        ArrayList<String> datafieldList = new ArrayList<String>();
        ArrayList<String> datatypes = new ArrayList<String>();
        getFields(hasPredecessor, datafieldList, datatypes);
        List<Invariant> inv = generateInvariants(hasPredecessor, datafieldList,
                datatypes);
        List<Invariant> invNo = generateInvariants(hasNoPredecessor,
                datafieldList, datatypes);
        List<Invariant> all = generateInvariants(partitions.get(label1),
                datafieldList, datatypes);
        if (inv != null && invNo != null) {
            ArrayList<Invariant> list = getRelevantInvariants(inv, invNo, all);
            List<Invariant> inv2 = generateInvariants(isPredecessor,
                    datafieldList, datatypes);
            List<Invariant> invNo2 = generateInvariants(isNoPredecessor,
                    datafieldList, datatypes);
            List<Invariant> all2 = generateInvariants(partitions.get(label2),
                    datafieldList, datatypes);
            ArrayList<Invariant> list2 = getRelevantInvariants(inv2, invNo2,
                    all2);
            if (list.size() > 0 || list2.size() > 0) {
                System.out.println("    " + label2 + list2 + "\nAP  " + label1
                        + list);

                for (Invariant i : list) {
                    double r = getInvariantRelevance(i,
                            (Collection) partitions.get(label1), datafieldList);
                    System.out.println("Conf " + r + " for " + i);
                }
            }
        }
    }

    public static ArrayList<Invariant> getRelevantInvariants(
            List<Invariant> inv, List<Invariant> invNo, List<Invariant> all) {
        ArrayList<Invariant> list = new ArrayList<Invariant>();
        // System.out.println(inv);
        // System.out.println(invNo);
        for (Invariant i : inv) {
            boolean found = false;
            for (Invariant j : invNo) {
                if (i.isSameInvariant(j)) {
                    found = true;
                    break;
                }
            }
            for (Invariant j : all) {
                if (i.isSameInvariant(j)) {
                    found = true;
                    break;
                }
            }
            if (!found)
                list.add(i);
        }
        // System.out.println("->" + list);
        return list;
    }

    public static double getInvariantRelevance(Invariant inv,
            Collection<Event> events, List<String> argNames) {
        Invariant clone = inv.clone();
        double num = 0;
        for (Event e : events) {
            int size = argNames.size();
            Object[] vals = new Object[size];
            int[] mods = new int[size];
            int index = 0;
            for (String s : argNames) {
                if (e.getStringArgument(s) == null)
                    vals[index] = Intern.internedLong(-1);
                else
                    vals[index] = Intern.internedLong(Long.parseLong(e
                            .getStringArgument(s)));
                mods[index] = 0;
                index++;
            }
            ValueTuple vt = new ValueTuple(vals, mods);
            InvariantStatus status = clone.add_sample(vt, 1);
            if (status != InvariantStatus.NO_CHANGE) {
                num++;
            }
        }
        return num / events.size();
    }

    public static <T extends INode<T>> List<Invariant> generateFlowInvariants(
            Collection<T> messages, String relation, String targetType,
            List<String> datafieldList, List<String> datatypes)
            throws Exception {
        Daikonizer d = new Daikonizer("foo", datafieldList, datatypes);
        boolean dont = false;
        for (T n : messages) {
            if (!(n instanceof Event)) {
                dont = true;
                break;
            }
            Event e = (Event) n;
            List<String> beginVals = new ArrayList<String>();
            for (String argName : datafieldList) {
                String s = e.getStringArgument(argName);
                if (s == null || s.length() == 0)
                    s = "-1";
                beginVals.add(s);
            }
            Event e2 = null;
            for (ITransition<T> t : n.getTransitionsIterator(relation)) {
                if (t.getTarget().getLabel().equals(targetType)) {
                    e2 = (Event) t.getTarget();
                    break;
                }
            }
            List<String> endVals = new ArrayList<String>();
            if (e2 != null) {
                for (String argName : datafieldList) {
                    String s = e2.getStringArgument(argName);
                    if (s == null || s.length() == 0)
                        s = "-1";
                    endVals.add(s);
                }
            }
            d.addValues((List) beginVals, (List) endVals);
        }
        if (!dont) {
            List<Invariant> enter = new ArrayList<Invariant>();
            List<Invariant> exit = new ArrayList<Invariant>();
            List<Invariant> flow = new ArrayList<Invariant>();
            d.genDaikonInvariants(enter, exit, flow, false);
            return flow;
        }
        return null;
    }

    public static <T extends INode<T>> List<Invariant> generateFlowInvariants(
            Collection<T> messages, String relation, String targetType)
            throws Exception {
        ArrayList<String> datafieldList = new ArrayList<String>();
        ArrayList<String> datatypes = new ArrayList<String>();
        getFields(messages, datafieldList, datatypes);
        return generateFlowInvariants(messages, relation, targetType,
                datafieldList, datatypes);
    }

    private static <T> void getFields(Collection<T> hasPredecessor,
            ArrayList<String> datafieldList, ArrayList<String> datatypes) {
        Set<String> datafields = new LinkedHashSet<String>();
        for (T n : hasPredecessor) {
            if (!(n instanceof Event))
                break;
            Event e = (Event) n;
            datafields.addAll(e.getStringArguments());
        }
        datafieldList.addAll(datafields);
        for (String s : datafieldList)
            // s intentionally not used
            datatypes.add("int");
    }

    public static List<Invariant> generateInvariants(Set<MessageEvent> messages)
            throws Exception {
        ArrayList<String> datafieldList = new ArrayList<String>();
        ArrayList<String> datatypes = new ArrayList<String>();
        getFields(messages, datafieldList, datatypes);
        return generateInvariants(messages, datafieldList, datatypes);
    }

    public static <T> List<Invariant> generateInvariants(
            Collection<T> hasPredecessor, List<String> datafieldList,
            List<String> datatypes) throws Exception {
        Daikonizer d = new Daikonizer("foo", datafieldList, datatypes);
        boolean dont = false;
        for (T n : hasPredecessor) {
            if (!(n instanceof Event)) {
                dont = true;
                break;
            }
            Event e = (Event) n;
            List<String> vals = new ArrayList<String>();
            for (String argName : datafieldList) {
                String s = e.getStringArgument(argName);
                if (s == null || s.length() == 0)
                    s = "-1";
                vals.add(s);
            }
            d.addValues((List) vals, (List) vals);
        }
        if (!dont) {
            List<Invariant> enter = new ArrayList<Invariant>();
            List<Invariant> exit = new ArrayList<Invariant>();
            List<Invariant> flow = new ArrayList<Invariant>();
            d.genDaikonInvariants(enter, exit, flow, false);
            return enter;
        }
        return null;
    }
    */

}
