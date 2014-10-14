package synopticgwt.client.util;

/**
 * Similar to MouseHover, except able to send over an instance of a type
 * of object in the event of a mouse over/mouse out event.
 * 
 * This is all so that mouse hover events can be written reasonably in Java
 * and registered to the respective JS objects.
 *
 * @param <T> The type of the object expected to be passed through the mouse over
 * or mouseout methods.
 */
public interface MouseEventHandler<T> {
    void mouseover(T t);
    void mouseout(T t);
    void onclick(T t, boolean shiftKey);
}
