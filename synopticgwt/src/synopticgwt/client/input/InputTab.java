package synopticgwt.client.input;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.SynopticGWT;
import synopticgwt.client.Tab;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;

/**
 * Represents the inputs tab, using which the user can input a log, regular
 * expressions, and tell the service to parse the log to mine invariants and
 * build the corresponding model.
 */
public class InputTab extends Tab<VerticalPanel> {

    private final Label parseErrorMsgLabel = new Label();
    private final TextArea logTextArea = new TextArea();
    private final TextArea regExpsTextArea = new TextArea();
    private final TextBox partitionRegExpTextBox = new TextBox();
    private final TextBox separatorRegExpTextBox = new TextBox();
    private final Button parseLogButton = new Button("Parse Log");

    public InputTab(ISynopticServiceAsync synopticService, ProgressWheel pWheel) {
        super(synopticService, pWheel);
        panel = new VerticalPanel();

        // Construct the inputs panel using a grid.
        panel.add(parseErrorMsgLabel);

        Grid grid = new Grid(5, 2);
        panel.add(grid);

        grid.setWidget(0, 0, new Label("Log lines"));
        grid.setWidget(0, 1, logTextArea);
        logTextArea.setCharacterWidth(80);
        logTextArea.setVisibleLines(10);

        grid.setWidget(1, 0, new Label("Regular expressions"));
        grid.setWidget(1, 1, regExpsTextArea);
        regExpsTextArea.setCharacterWidth(80);

        grid.setWidget(2, 0, new Label("Partition expression"));
        grid.setWidget(2, 1, partitionRegExpTextBox);
        partitionRegExpTextBox.setVisibleLength(80);

        grid.setWidget(3, 0, new Label("Separator expression"));
        grid.setWidget(3, 1, separatorRegExpTextBox);
        separatorRegExpTextBox.setVisibleLength(80);

        grid.setWidget(4, 1, parseLogButton);

        grid.setStyleName("inputForm grid");
        for (int i = 0; i < grid.getRowCount(); i++) {
            for (int j = 0; j < grid.getCellCount(i); j++) {
                grid.getCellFormatter().setStyleName(i, j, "tableCell");
            }
        }

        // Set up the error label's style\visibility.
        parseErrorMsgLabel.setStyleName("serverResponseLabelError");
        parseErrorMsgLabel.setVisible(false);

        // Set up the logTextArea.
        logTextArea.setFocus(true);
        logTextArea
                .setText("1 0 c\n2 0 b\n3 0 a\n4 0 d\n1 1 f\n2 1 b\n3 1 a\n4 1 e\n1 2 f\n2 2 b\n3 2 a\n4 2 d");
        logTextArea.selectAll();

        // Set up the test areas.
        regExpsTextArea.setText("^(?<TIME>)(?<nodename>)(?<TYPE>)$");
        partitionRegExpTextBox.setText("\\k<nodename>");

        // Associate handler with the Parse Log button.
        parseLogButton.addClickHandler(new ParseLogHandler());
    }

    /**
     * Handles parse log button clicks.
     */
    class ParseLogHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            // Disallow the user from making concurrent Parse Log calls.
            parseLogButton.setEnabled(false);

            // Reset the parse error msg.
            parseErrorMsgLabel.setText("");

            // Extract arguments for parseLog call.
            String logLines = logTextArea.getText();
            String regExpLines[] = regExpsTextArea.getText().split("\\r?\\n");
            List<String> regExps = Arrays.asList(regExpLines);

            String partitionRegExp = partitionRegExpTextBox.getText();
            if (partitionRegExp == "") {
                partitionRegExp = null;
            }
            String separatorRegExp = separatorRegExpTextBox.getText();
            if (separatorRegExp == "") {
                separatorRegExp = null;
            }

            // TODO: validate the arguments to parseLog.

            // ////////////////////// Call to remote service.
            synopticService.parseLog(logLines, regExps, partitionRegExp,
                    separatorRegExp, new ParseLogAsyncCallback());
            // //////////////////////
        }
    }

    /**
     * Callback handler for the parseLog() Synoptic service call.
     */
    class ParseLogAsyncCallback implements
            AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> {
        @Override
        public void onFailure(Throwable caught) {
            displayRPCErrorMessage("Remote Procedure Call Failure while parsing log");
            parseErrorMsgLabel.setText("Remote Procedure Call - Failure");
            parseLogButton.setEnabled(true);
        }

        @Override
        public void onSuccess(GWTPair<GWTInvariantSet, GWTGraph> ret) {
            parseLogButton.setEnabled(true);
            SynopticGWT.entryPoint.logParsed(ret.getLeft(), ret.getRight());
        }
    }
}
