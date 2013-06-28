package synoptic.model.state;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import daikon.inv.Invariant;
import daikonizer.DaikonInvariants;
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
     * If the state is null, then nothing is added.
     * 
     * @throws Exception
     */
    public void addInstance(State state) throws Exception {
        if (state == null) {
            return;
        }
        
        Set<DaikonVar> stateVars = state.getVariables();
        // TODO: This might not be the best policy. Revisit this condition.
        if (!vars.isEmpty()
                && (!vars.containsAll(stateVars)
                        || !stateVars.containsAll(vars))) {
            throw new Exception(
                    "Variables are inconsistent among added states: "
                    + vars + " and " + stateVars);
        }
        
        Vector<Object> record = new Vector<Object>();
        if (daikonizer == null) {
            // Set the order of variables.
            for (DaikonVar var : stateVars) {
                vars.add(var);
                String value = state.getValue(var);
                record.add(value);
            }
            daikonizer = new Daikonizer("SynopticPoint", vars);
        } else {
            // Align values to their corresponding variables.
            record.setSize(stateVars.size());
            for (DaikonVar var : stateVars) {
                String value = state.getValue(var);
                int index = vars.indexOf(var);
                assert index >= 0;
                record.set(index, value);
            }
        }
        
        daikonizer.addValues(record, record);
    }
    
    /**
     * @return Daikon invariants detected from all added states.
     * @throws Exception
     */
    public DaikonInvariants getDaikonEnterInvariants() throws Exception {
        // TODO: add a new method to Daikonizer that generates invariants
        // at a single point, and use that method instead.
        List<Invariant> enterInvs = new Vector<Invariant>();
        List<Invariant> exitInvs = new Vector<Invariant>();
        List<Invariant> flow = new Vector<Invariant>();
        daikonizer.genDaikonInvariants(enterInvs, exitInvs, flow, false);
        DaikonInvariants invs = new DaikonInvariants(enterInvs);
        return invs;
    }
}
