package synopticgwt.client.invariants;

/**
 * An interface for use by GraphicEvent to highlight PO and TO invariants.
 * 
 * @author t101jv
 */
public interface GraphicInvariant {
    public void highlightOn();

    public void highlightOff();

    /** Sets GraphicInvariant to be visible on the graph. */
    public void show();

    /** Sets GraphicInvariant to be invisible on the graph. */
    public void hide();
}
