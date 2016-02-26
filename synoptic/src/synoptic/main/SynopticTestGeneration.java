package synoptic.main;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import synoptic.model.PartitionGraph;
import synoptic.model.Transition;
import synoptic.model.TransitionLabelType;
import synoptic.model.UniformPartition;
import synoptic.model.UniformStatePartition;
import synoptic.model.interfaces.ITransition;
import synoptic.model.testgeneration.AbstractTestCase;
import synoptic.model.testgeneration.Action;

import daikonizer.DaikonInvariants;

/**
 * SynopticTestGeneration derives abstract test cases from a Synoptic model,
 * i.e., a final partition graph.
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
        Set<List<UniformPartition>> paths = model.getAllBoundedPredictedPaths();
        for (List<UniformPartition> path : paths) {
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
    public static AbstractTestCase convertPathToAbstractTest(
            List<UniformPartition> path) {
        assert !path.isEmpty();
        AbstractMain main = AbstractMain.getInstance();

        Action currAction = new Action(path.get(0).getEType());
        AbstractTestCase testCase = new AbstractTestCase(currAction);

        for (int i = 0; i < path.size() - 1; i++) {
            UniformPartition next = path.get(i + 1);

            Action nextAction = new Action(next.getEType());
            testCase.add(nextAction);
            ITransition<Action> actionTrans = new Transition<Action>(
                    currAction, nextAction, timeRelation);

            if (main.options.stateProcessing) {
                UniformPartition curr = path.get(i);
                assert curr instanceof UniformStatePartition;
                UniformStatePartition currState = (UniformStatePartition) curr;

                List<? extends ITransition<UniformStatePartition>> transitions = currState
                        .getTransitionsWithDaikonInvariants();

                for (ITransition<UniformStatePartition> trans : transitions) {
                    if (trans.getTarget().compareTo(next) == 0) {
                        DaikonInvariants invs = trans.getLabels()
                                .getDaikonInvariants();
                        actionTrans.getLabels().setLabel(
                                TransitionLabelType.DAIKON_INVARIANTS_LABEL,
                                invs);
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
