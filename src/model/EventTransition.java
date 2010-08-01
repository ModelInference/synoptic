package model;

public class EventTransition extends Transition<SystemState<EventTransition>>{

	public EventTransition(SystemState<EventTransition> source,
			SystemState<EventTransition> target, String action) {
		super(source, target, action);
	}
}
