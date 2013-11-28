package synoptic.model.testgeneration;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.ITransition;

/**
 * AbstractTestCase is a sequence of actions corresponding to a valid path
 * in some Synoptic model.
 * 
 *
 */
public class AbstractTestCase implements IGraph<Action> {
	/** A dummy initial action of this abstract test. **/
	private final Action dummyInitAction;
	/** All actions in this abstract test. **/
	private final Set<Action> allActions = new LinkedHashSet<Action>();
	/** A cache of inter-action transitions. */
	private final Map<Action, Set<Action>> transitionCache = 
			new LinkedHashMap<Action, Set<Action>>();
	
	public AbstractTestCase(Action dummyInitAction) {
		this.dummyInitAction = dummyInitAction;
		allActions.add(dummyInitAction);
	}

	@Override
	public Set<Action> getNodes() {
		return allActions;
	}

	@Override
	public Set<String> getRelations() {
	    Set<String> relations = new LinkedHashSet<String>();
	    for (Action action : allActions) {
	        List<? extends ITransition<Action>> transitions = action
	                .getAllTransitions();
	        // Each action in an abstract test can have at most 1 transition.
	        assert transitions.size() <= 1;
	        for (ITransition<Action> trans : transitions) {
	            relations.addAll(trans.getRelation());
	        }
	    }
		return relations;
	}

	@Override
	public Action getDummyInitialNode() {
		return dummyInitAction;
	}

	@Override
	public Set<Action> getAdjacentNodes(Action node) {
		if (transitionCache.containsKey(node)) {
            return transitionCache.get(node);
        }

        Set<Action> adjActions = node.getAllSuccessors();
        transitionCache.put(node, adjActions);
        return adjActions;
	}

	@Override
	public void add(Action node) {
		allActions.add(node);
	}

}
