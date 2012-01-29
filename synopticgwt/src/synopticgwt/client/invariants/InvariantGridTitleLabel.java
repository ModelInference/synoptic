package synopticgwt.client.invariants;

import com.google.gwt.user.client.ui.Label;

import synopticgwt.client.util.TooltipListener;

/**
 * Represents a title cell in the invariants table. This cell keeps track of an
 * "active" boolean flag, which determines whether or not all of the cells in
 * the invariant category (e.g., NFby) are activated (true) or deactivated
 * (false).
 */
public class InvariantGridTitleLabel extends Label {
    public boolean active;
    private static final String genericTip = "Click on an invariant or column header to activate/de-activate a specific or all invariant(s).";
    private static final String TOOLTIP_URL = "http://code.google.com/p/synoptic/wiki/DocsWebAppTutorial#Invariants_Tab";

    public InvariantGridTitleLabel(String s) {
        super(s);
        active = true;
        String toolTip = "";
        if (s.startsWith("AlwaysFollowedBy")) {
            toolTip = "When an event X appears, an event of Y always appears later in the trace. ";
        } else if (s.startsWith("AlwaysPrecedes")) {
            toolTip = "When an event X appears, an event Y must have appeared earlier in the trace. ";
        } else if (s.startsWith("NeverFollowedBy")) {
            toolTip = "When an event X appears, the event Y never appears later in the  trace. ";
        } else if (s.startsWith("NeverConcurrentWith")) {
            toolTip = "In all traces, there is an ordering between any instance of X and an instance of Y. ";
        } else if (s.startsWith("AlwaysConcurrentWith")) {
            toolTip = "In all traces, X events and Y events are always concurrent (never ordered). ";
        }

        TooltipListener.setTooltip(this, toolTip + genericTip, TOOLTIP_URL);
    }
}
