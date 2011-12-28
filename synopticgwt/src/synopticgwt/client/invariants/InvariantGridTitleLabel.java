package synopticgwt.client.invariants;

import com.google.gwt.user.client.ui.Label;

/**
 * Represents a title cell in the invariants table. This cell keeps track of an
 * "active" boolean flag, which determines whether or not all of the cells in
 * the invariant category (e.g., NFby) are activated (true) or deactivated
 * (false).
 */
public class InvariantGridTitleLabel extends Label {
    public boolean active;

    public InvariantGridTitleLabel(String s) {
        super(s);
        active = true;
    }
}
