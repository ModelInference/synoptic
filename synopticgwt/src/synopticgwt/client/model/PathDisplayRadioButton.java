package synopticgwt.client.model;

import java.util.Set;

import synopticgwt.shared.GWTEdge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.RadioButton;

public class PathDisplayRadioButton extends RadioButton {

    private final Set<GWTEdge> path;

    public PathDisplayRadioButton(String group, String traceID,
            Set<GWTEdge> path) {
        super(group, traceID);
        this.path = path;
        this.addClickHandler(new PDRBClickHandler());
    }

    private class PDRBClickHandler implements ClickHandler {
        @SuppressWarnings("synthetic-access")
        public void onClick(ClickEvent event) {
            // TODO Overlay the path onto the model.
        }
    }
}
