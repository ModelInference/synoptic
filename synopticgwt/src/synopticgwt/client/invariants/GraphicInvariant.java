package synopticgwt.client.invariants;

/**
 * This exists so we can have a simple interface to use in GraphicEvent for
 * highlighting Concurrent and Ordered invariants.
 * 
 * @author t101jv
 */
public interface GraphicInvariant {
    public void highlightOn();

    public void highlightOff();
}
