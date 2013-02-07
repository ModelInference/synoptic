package synoptic.model.state;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import daikon.inv.Invariant;
import daikonizer.Daikonizer;

/**
 * A wrapper class of Daikonizer. This class is used for detecting invariants
 * from all data records (i.e., States) at a particular Synoptic point.
 * 
 * @author rsukkerd
 *
 */
public class SynDaikonizer {
    private List<String> varNames;    
    private Daikonizer daikonizer;
    
    public SynDaikonizer() {
        varNames = new Vector<String>();
        daikonizer = null;
    }
    
    /**
     * Adds an instance of data record i.e., State.
     * Variables must be consistent among all added states.
     * 
     * @throws Exception
     */
    public void addInstance(State state) throws Exception {
        Set<String> stateVarNames = state.getVariableNames();
        if (!varNames.containsAll(stateVarNames)
                || !stateVarNames.containsAll(varNames)) {
            throw new Exception(
                    "Variables are inconsistent among added states: "
                    + varNames + " and " + stateVarNames);
        }
        
        Vector<Object> record = new Vector<Object>();
        record.setSize(stateVarNames.size());
        
        for (Map.Entry<String, String> entry : state) {
            String varName = entry.getKey();
            String value = entry.getValue();
            int index = varNames.indexOf(varName);
            if (index < 0) {
                index = 0;
                varNames.add(value);
            }
            record.set(index, value);
        }
        
        if (daikonizer == null) {
            daikonizer = new Daikonizer("SynopticPoint", varNames, varNames);
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
