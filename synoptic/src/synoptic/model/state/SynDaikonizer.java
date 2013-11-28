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
     * If the state is null, then nothing is added.
     */
    public void addInstance(State state) {
        if (state == null) {
            return;
        }
        
        Set<DaikonVar> stateVars = state.getVariables();
        // TODO: What if variables are not consistent among all added states?
        if (!vars.isEmpty()
                && (!vars.containsAll(stateVars)
                        || !stateVars.containsAll(vars))) {
            /*
            throw new Exception(
                    "Variables are inconsistent among added states: "
                    + vars + " and " + stateVars);
            */
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
     */
    public DaikonInvariants getDaikonEnterInvariants() {
        // TODO: add a new method to Daikonizer that generates invariants
        // at a single point, and use that method instead.
        List<Invariant> enterInvs = new Vector<Invariant>();
        List<Invariant> exitInvs = new Vector<Invariant>();
        List<Invariant> flow = new Vector<Invariant>();
        
        // invariants that Daikon would output
        String printedInvs = "";
        
        // Only run Daikon when there are state instances.
        if (daikonizer != null) {
            printedInvs = daikonizer.genDaikonInvariants(enterInvs, exitInvs, flow, false);
        }
        DaikonInvariants invs = new DaikonInvariants(enterInvs, printedInvs);
        return invs;
    }
}
