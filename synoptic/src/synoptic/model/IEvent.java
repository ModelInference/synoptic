package synoptic.model;

import synoptic.util.time.ITime;

/**
 * An interface all events should implement. Currently this is somewhat inane.
 * 
 * @author Sigurd Schneider
 */
public interface IEvent {
    /**
     * Get the time the event occured.
     * 
     * @return vector time the event occured
     */
    ITime getTime();

    /**
     * Return the name (i.e. label) of the event.
     * 
     * @return the name of the event
     */
    String getName();
}
