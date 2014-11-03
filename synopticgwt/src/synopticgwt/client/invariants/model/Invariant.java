package synopticgwt.client.invariants.model;

/**
 * An interface for use by Event to highlight PO and TO invariants.
 * 
 */
public interface Invariant {
    public void highlightOn();

    public void highlightOff();

    /** Sets Invariant to be visible on the graph. */
    public void show();

    /** Sets Invariant to be invisible on the graph. */
    public void hide();
}
