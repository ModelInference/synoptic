package synoptic.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import synoptic.util.InternalSynopticException;

/**
 * Implements a combo-box control that displays a list of layouts. When a layout
 * is selected, the actionPerformed() method below creates a new layout
 * parameterized by the JUNG Graph and tells the VisualizationViewer to use it.
 * 
 * @author ivan
 * @param <Node>
 *            The Node type maintained by the JUNG Graph
 * @param <Transition>
 *            The Transition type maintained by the JUNG Graph
 */
class LayoutChooser<Node, Transition> implements ActionListener {
    static final Class<?>[] constructorArgsWanted = { Graph.class };

    private final JComboBox comboBox;
    private final Graph<Node, Transition> jGraph;
    private final VisualizationViewer<Node, Transition> vizViewer;

    private LayoutChooser(JComboBox comboBox, Graph<Node, Transition> jGraph,
            VisualizationViewer<Node, Transition> vizViewer) {
        super();
        this.comboBox = comboBox;
        this.jGraph = jGraph;
        this.vizViewer = vizViewer;
    }

    /**
     * This method is executed whenever a new item is selected in the combo-box.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent arg0) {
        Object[] constructorArgs = { jGraph };

        Class<?> layoutC = (Class<?>) comboBox.getSelectedItem();
        try {
            Constructor<?> constructor = layoutC
                    .getConstructor(constructorArgsWanted);
            // Create a new layout instance.
            Object o = constructor.newInstance(constructorArgs);
            // Double check that the item in the combo-box is a valid Layout.
            if (o instanceof Layout<?, ?>) {
                // Initialize the layout with the current layout's graph.
                Layout<Node, Transition> layout = (Layout<Node, Transition>) o;
                layout.setGraph(vizViewer.getGraphLayout().getGraph());
                // Tell the viewer to use the new layout.
                vizViewer.setGraphLayout(layout);
            }
        } catch (Exception e) {
            throw new InternalSynopticException("Could not load layout "
                    + layoutC);
        }
    }

    /**
     * Creates a combo-box of various layouts supported by JUNG and adds this
     * box to the passed panel.
     * 
     * @param <Node>
     *            The type of node maintained by the graph.
     * @param <Transition>
     *            The type of transition maintained by the graph.
     * @param panel
     *            the JPanel to add the box to.
     * @param jGraph
     *            The JUNG Graph maintaining a graph of Node,Transition type
     * @param vizViewer
     *            The viewer we are using to display jGraph.
     */
    public static <Node, Transition> void addLayoutCombo(JPanel panel,
            Graph<Node, Transition> jGraph,
            VisualizationViewer<Node, Transition> vizViewer) {

        Vector<Class<?>> layouts = new Vector<Class<?>>();
        layouts.add(KKLayout.class);
        layouts.add(FRLayout.class);
        layouts.add(CircleLayout.class);
        layouts.add(SpringLayout.class);
        layouts.add(ISOMLayout.class);

        // Add items to the combo-box.
        // (JComboBox is generic in Java 7 but not in Java 6)
        final JComboBox comboBox = new JComboBox(layouts);
        // Select the FRLayout as the default layout.
        comboBox.setSelectedItem(FRLayout.class);
        // Add a listener so that whenever a new item is chosen it hits the
        // LayoutChooser instance.
        LayoutChooser<Node, Transition> chooser = new LayoutChooser<Node, Transition>(
                comboBox, jGraph, vizViewer);
        comboBox.addActionListener(chooser);
        // Add the box to the panel.
        panel.add(comboBox, BorderLayout.NORTH);
    }
}
