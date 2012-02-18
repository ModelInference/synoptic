package synopticgwt.client.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.util.TooltipListener;
import synopticgwt.shared.GWTEdge;
import synopticgwt.shared.LogLine;

/**
 * A panel used to display different information about the log. So far, the
 * provided information that the panel displays are lists of log lines and sets
 * of paths. To save screen real estate, the panel switches state based on what
 * is going to be displayed (ex: running showPaths will switch to the path
 * displaying layout of the panel if it isn't already in said state). If the
 * user so chooses, they can toggle between states manually by clicking on the
 * label atop the panel. The state of the table, when being switched to
 * manually, remains the same until the next RPC is initiated. For example, if
 * one views a list of log lines, and then views a set of paths. If the user
 * then clicks on the label to switch back to displaying the list of log lines,
 * they will remain as they were until the next RPC.
 */
public class LogInfoPanel extends VerticalPanel {

    // CSS Attributes of the log info label
    public static final String LOG_INFO_PATHS_CLASS = "log-info-displaying-paths";
    public static final String LOG_INFO_LINES_CLASS = "log-info-displaying-log-lines";
    public static final String LOG_INFO_LABEL_ID = "log-info-label";

    private final Label logInfoLabel;
    private final LogLinesTable logLinesTable;
    private final PathsThroughPartitionsTable pathsThroughPartitionsTable;

    private static final String logInfoLabelLogLines = "Selected node log lines";
    private static final String logInfoLabelPathTraces = "Selected path traces";

    public LogInfoPanel(String width) {
        super();

        logInfoLabel = new Label(logInfoLabelLogLines);
        logLinesTable = new LogLinesTable();
        pathsThroughPartitionsTable = new PathsThroughPartitionsTable();
        this.setWidth(width);

        this.add(logInfoLabel);
        this.add(logLinesTable);
        this.add(pathsThroughPartitionsTable);

        TooltipListener
                .setTooltip(
                        logInfoLabel,
                        "Click on a node to view the associated log lines. Shift+Click to select multiple nodes to view paths through nodes.",
                        ModelTab.TOOLTIP_URL);

        // Set up the default CSS attributes for the table
        DOM.setElementAttribute(logInfoLabel.getElement(), "id",
                LOG_INFO_LABEL_ID);
        DOM.setElementAttribute(logInfoLabel.getElement(), "class",
                LOG_INFO_LINES_CLASS);

        pathsThroughPartitionsTable.setVisible(false);
    }

    /**
     * Takes a list of log lines and displays them on the panel, line by line.
     * If the state of the log line table is not visible, the panel will switch
     * to accommodate.
     * 
     * @param lines
     */
    public void showLogLines(List<LogLine> lines) {
        this.logLinesTable.showLines(lines);
        if (!logLinesTable.isVisible()) {
            this.toggleLogInfoDisplay();
        }
    }

    /**
     * Takes a set of trace IDs (each mapped to a path), and displays them on
     * the panel. If the state of the paths table is not already visible, the
     * panel will switch to accommodate. TODO: Explain how these will be
     * displayed according to the pathsThroughPartitionsTable object once the
     * full functionality is implemented.
     * 
     * @param paths
     *            Set of trace IDs mapped to specific paths
     */
    public void showPaths(Map<List<GWTEdge>, Set<Integer>> paths) {
        this.pathsThroughPartitionsTable.showPaths(paths);
        if (!pathsThroughPartitionsTable.isVisible()) {
            this.toggleLogInfoDisplay();
        }
    }

    /**
     * Clears the log lines and the paths tables, and set the visibility back to
     * the log lines table (the default).
     */
    @Override
    public void clear() {
        this.logLinesTable.clear();
        this.pathsThroughPartitionsTable.clear();

        if (!logLinesTable.isVisible()) {
            toggleLogInfoDisplay();
        }
    }

    /**
     * Toggles the info display between showing log lines and showing paths
     * through selected partitions.
     */
    private void toggleLogInfoDisplay() {
        if (logLinesTable.isVisible()) {
            logInfoLabel.setText(logInfoLabelPathTraces);
            DOM.setElementAttribute(logInfoLabel.getElement(), "class",
                    LOG_INFO_PATHS_CLASS);
        } else {
            logInfoLabel.setText(logInfoLabelLogLines);
            DOM.setElementAttribute(logInfoLabel.getElement(), "class",
                    LOG_INFO_LINES_CLASS);
        }

        logLinesTable.setVisible(!logLinesTable.isVisible());
        pathsThroughPartitionsTable.setVisible(!pathsThroughPartitionsTable
                .isVisible());
    }
}
