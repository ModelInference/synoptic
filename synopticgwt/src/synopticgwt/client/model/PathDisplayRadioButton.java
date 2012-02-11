package synopticgwt.client.model;

import java.util.Set;

import synopticgwt.shared.GWTEdge;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RadioButton;

public class PathDisplayRadioButton extends RadioButton {

    private final Set<GWTEdge> path;
    private volatile JavaScriptObject jsEdges;

    public PathDisplayRadioButton(String group, Set<Integer> traceIDs,
            Set<GWTEdge> path) {
        
        // TODO: Come up with a better way to label the radio buttons.
        super(group, "PLACEHOLDER");
        this.path = path;
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        if (DOM.eventGetType(event) == Event.ONCLICK) {
            // Lazily create edges and then cache them.
            if (jsEdges == null)
                this.jsEdges = GWTToJSUtils
                        .createJSArrayFromGWTEdges(this.path);
            ModelGraphic.highlightEdges(jsEdges);
        }
    }
}
