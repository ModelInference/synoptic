package synoptic.model.state;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import daikon.inv.Invariant;
import daikonizer.DaikonVar;
import daikonizer.Daikonizer;

/**
 * A wrapper class of Daikonizer. This class is used for detecting invariants
 * from all data records (i.e., States) at a particular Synoptic point.
 * 
 * @author rsukkerd
 *
 */
public class SynDaikonizer {
    private List<DaikonVar> vars;
    private Daikonizer daikonizer;
    
    public SynDaikonizer() {
        vars = new Vector<DaikonVar>();
        daikonizer = null;
    }
    
    /**
     * Adds an instance of data record i.e., State.
     * Variables must be consistent among all added states.
     * 
     * @throws Exception
     */
    public void addInstance(State state) throws Exception {
        Set<DaikonVar> stateVars = state.getVariables();
        if (!vars.isEmpty()
                && (!vars.containsAll(stateVars)
                        || !stateVars.containsAll(vars))) {
            throw new Exception(
                    "Variables are inconsistent among added states: "
                    + vars + " and " + stateVars);
        }
        
        Vector<Object> record = new Vector<Object>();
        record.setSize(stateVars.size());
        
        for (DaikonVar var : stateVars) {
            String value = state.getValue(var);
            int index = vars.indexOf(var);
            if (index < 0) {
                index = vars.size();
                vars.add(var);
            }
            record.set(index, value);
        }
        
        if (daikonizer == null) {
            daikonizer = new Daikonizer("SynopticPoint", vars);
        }
        daikonizer.addValues(record, record);
    }
    
    /**
     * @return Daikon invariants detected from all added states.
     * @throws Exception
     */
    public List<Invariant> getDaikonInvariants() throws Exception {
        List<Invariant> enterInvs = new Vector<Invariant>();
        List<Invariant> exitInvs = new Vector<Invariant>();
        List<Invariant> flow = new Vector<Invariant>();
        daikonizer.genDaikonInvariants(enterInvs, exitInvs, flow, false);
        return enterInvs;
    }
}
