package synopticgwt.client.input;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.Tab;
import synopticgwt.client.util.ProgressWheel;

/**
 * Represents the inputs tab, using which the user can select an InputSubTab
 * with a pre-defined example log/re values.
 */
public class InputTab extends Tab<VerticalPanel> {
    /** This contains the log-example tabs. */
    TabPanel tabPanel = new TabPanel();
    /** A list of input sub tab objects. */
    List<InputSubTab> subTabs = new LinkedList<InputSubTab>();

    static List<InputExample> inputExamples;
    static {
        inputExamples = new ArrayList<InputExample>();

        inputExamples
                .add(new InputExample(
                        "TwoPhaseCommit",
                        "TM, 0, tx_prepare, 0\nTM, 1, tx_prepare, 0\nTM, 2, tx_prepare, 0\n0, TM, commit, 0\n1, TM, abort, 0\n2, TM, commit, 0\nTM, 0, tx_abort, 0\nTM, 1, tx_abort, 0\nTM, 2, tx_abort, 0\nTM, 0, tx_prepare, 1\nTM, 1, tx_prepare, 1\nTM, 2, tx_prepare, 1\n0, TM, commit, 1\n1, TM, commit, 1\n2, TM, commit, 1\nTM, 0, tx_commit, 1\nTM, 1, tx_commit, 1\nTM, 2, tx_commit, 1\nTM, 0, tx_prepare, 2\nTM, 1, tx_prepare, 2\nTM, 2, tx_prepare, 2\n0, TM, commit, 2\n1, TM, commit, 2\n2, TM, commit, 2\nTM, 0, tx_commit, 2\nTM, 1, tx_commit, 2\nTM, 2, tx_commit, 2\nTM, 0, tx_prepare, 3\nTM, 1, tx_prepare, 3\nTM, 2, tx_prepare, 3\n0, TM, commit, 3\n1, TM, abort, 3\n2, TM, commit, 3\nTM, 0, tx_abort, 3\nTM, 1, tx_abort, 3\nTM, 2, tx_abort, 3\nTM, 0, tx_prepare, 4\nTM, 1, tx_prepare, 4\nTM, 2, tx_prepare, 4\n0, TM, commit, 4\n1, TM, abort, 4\n2, TM, commit, 4\nTM, 0, tx_abort, 4\nTM, 1, tx_abort, 4\nTM, 2, tx_abort, 4\n",
                        "^(?<sender>),(?<receiver>),(?<TYPE>),(?<txId>)",
                        "\\k<txId>"));

        inputExamples
                .add(new InputExample(
                        "abstract",
                        "1 0 c\n2 0 b\n3 0 a\n4 0 d\n1 1 f\n2 1 b\n3 1 a\n4 1 e\n1 2 f\n2 2 b\n3 2 a\n4 2 d",
                        "^(?<TIME>)(?<nodename>)(?<TYPE>)$", "\\k<nodename>"));

    }

    public InputTab(ISynopticServiceAsync synopticService, ProgressWheel pWheel) {
        super(synopticService, pWheel);

        // This panel will hold a tabPanel with subTabs.
        panel = new VerticalPanel();
        // Create the subTabs and popular the subTabs list and tabPanel.
        for (int i = 0; i < inputExamples.size(); i++) {
            // Each SubTab is initialized with log-related values, such as
            // the log and the regular expressions, from a set of statically
            // initialized InputExample objects (see above).
            InputExample ex = inputExamples.get(i);
            InputSubTab subTab = new InputSubTab(synopticService, ex.logText,
                    ex.regExpText, ex.partitionRegExpText);
            subTabs.add(subTab);
            tabPanel.add(subTab.getPanel(), String.valueOf(i) + ":"
                    + new String(ex.name));
        }

        tabPanel.setWidth("100%");
        tabPanel.selectTab(0);

        // Add the panel of tabs to the page.
        panel.add(tabPanel);
    }
}
