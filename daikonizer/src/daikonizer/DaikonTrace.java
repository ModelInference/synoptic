package daikonizer;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

// a trace corresponds to a program point and a series of value sets
// which associate values with variables at the program point
//
// NOTE: for now we assume that this trace is of a ENTER\EXIT program point
// that is, a procedure call program point.
public class DaikonTrace {
    public ProgramPoint ppt;
    public Vector<Pair> value_sets;

    public DaikonTrace(ProgramPoint ProgramPoint) {
        this.ppt = ProgramPoint;
        this.value_sets = new Vector<Pair>();
    }

    private boolean checkValues(List<Object> enterValues) {
        // check that we have the expected number of variables
        if (enterValues.size() != ppt.vars.size()) {
            return false;
        }

        // TODO: ideally we would use reflection and make sure that the data
        // types of members in values match the ProgramPoint.vars types

        return true;
    }

    public boolean addInstance(List<Object> enter_values,
            List<Object> exit_values) {
        if (this.checkValues(enter_values) && this.checkValues(exit_values)) {
            Pair pair = new Pair(enter_values, exit_values);
            this.value_sets.addElement(pair);
            return true;
        }
        return false;
    }

    public String headerString() {
        return this.ppt.toString();
    }

    private String ProgramPointInstanceToString(List<?> o1,
            String ProgramPointInstanceType) {
        String ret = ppt.pptName + ":::" + ProgramPointInstanceType
                + "\n";

        for (int i = 0; i < ppt.vars.size(); i++) {
            DaikonVar v = ppt.vars.get(i);
            Object val = o1.get(i);
            ret += v.vname + "\n";
            ret += val + "\n";
            ret += "1\n"; // modified bit always set to 1
        }
        return ret;
    }

    public String toString() {
        Iterator<Pair> pair_e;
        String ret = this.headerString();
        for (pair_e = this.value_sets.iterator(); pair_e.hasNext();) {
            Pair p = pair_e.next();
            ret += this.ProgramPointInstanceToString((List<?>) p.o1, "ENTER");
            ret += "\n";
            ret += this.ProgramPointInstanceToString((List<?>) p.o2, "EXIT1");
            ret += "\n";
        }
        return ret;
    }
}
