package synoptic.main;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import daikonizer.DaikonInvariants;

import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.Transition;
import synoptic.model.TransitionLabelType;
import synoptic.model.interfaces.ITransition;
import synoptic.model.testgeneration.AbstractTestCase;
import synoptic.model.testgeneration.Action;

/**
 * SynopticTestGeneration derives abstract test cases from a Synoptic model,
 * i.e., a final partition graph.
 * 
 *
 */
public class SynopticTestGeneration {
    /**
     * A dummy relation string for action transitions.
     */
    private static final String timeRelation = "time".intern();
    
    /**
     * Derives an abstract test suite from a given model.
     * 
     * @return a set of abstract test cases derived from model.
     */
    public static Set<AbstractTestCase> deriveAbstractTests(PartitionGraph model) {
        Set<AbstractTestCase> testSuite = new LinkedHashSet<AbstractTestCase>();
        Set<List<Partition>> paths = model.getAllBoundedPredictedPaths();
        for (List<Partition> path : paths) {
            AbstractTestCase testCase = convertPathToAbstractTest(path);
            testSuite.add(testCase);
        }
        return testSuite;
    }
    
    /**
     * Converts a path in the model to its corresponding abstract test case.
     * 
     * @return a corresponding abstract test case.
     */
    public static AbstractTestCase convertPathToAbstractTest(List<Partition> path) {
        assert !path.isEmpty();
        SynopticMain syn = SynopticMain.getInstanceWithExistenceCheck();
        
        Action currAction = new Action(path.get(0).getEType());
        AbstractTestCase testCase = new AbstractTestCase(currAction);
        
        for (int i = 0; i < path.size() - 1; i++) {
            Partition next = path.get(i + 1);
            
            Action nextAction = new Action(next.getEType());
            testCase.add(nextAction);
            ITransition<Action> actionTrans = new Transition<Action>(currAction,
                    nextAction, timeRelation);
            
            if (syn.options.stateProcessing) {
                Partition curr = path.get(i);
                List<? extends ITransition<Partition>> transitions =
                    curr.getTransitionsWithDaikonInvariants();
                
                for (ITransition<Partition> trans : transitions) {
                    if (trans.getTarget().compareTo(next) == 0) {
                        DaikonInvariants invs = trans.getLabels().getDaikonInvariants();
                        actionTrans.getLabels().setLabel(TransitionLabelType
                                .DAIKON_INVARIANTS_LABEL, invs);
                        break;
                    }
                }
            }
            currAction.addTransition(actionTrans);
            currAction = nextAction;
        }
        return testCase;
    }
}
