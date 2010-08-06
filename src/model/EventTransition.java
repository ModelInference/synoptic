package model;

/**
 * A transition for the state based model.
 * @author Sigurd Schneider
 *
 */
public class EventTransition extends Transition<SystemState<EventTransition>>{

	public EventTransition(SystemState<EventTransition> source,
			SystemState<EventTransition> target, String action) {
		super(source, target, action);
	}
}
