package synoptic.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.algorithms.graph.PartitionSplit;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.RelationPath;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.Transition;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * Shows how to create a graph editor with JUNG. Mouse modes and actions are
 * explained in the help text. The application version of GraphEditorDemo
 * provides a File menu with an option to save the visible graph as a jpeg file.
 * 
 * @author Tom Nelson Edits to display our graphs.
 */
public class JungGui extends JApplet implements Printable {

    static final Class<?>[] constructorArgsWanted = { Graph.class };

    private static final long serialVersionUID = -2023243689258876709L;

    /**
     * Java frame used by the gui.
     */
    private final JFrame frame;

    /**
     * The partition graph maintained by Synoptic.
     */
    private final PartitionGraph pGraph;

    /**
     * Buttons for refinement options
     */
    private JButton refineOption;
    private JButton totalRefine;
    /**
     * The visual representation of pGraph, displayed by the Applet.
     */
    DirectedGraph<INode<Partition>, ITransition<Partition>> jGraph;

    /**
     * The layout used to display the graph.
     */
    Layout<INode<Partition>, ITransition<Partition>> layout;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<INode<Partition>, ITransition<Partition>> vizViewer;

    /**
     * The currently selected path to highlight
     */
    private Set<ITransition<Partition>> currentPath;

    static String instructions = "<html>"
            + "<h3>Edges:</h3>"
            + "<ul>"
            + "<li>Labels denote the number of log events that were observed to make the transition along the edge</li>"
            + "</ul>"
            + "<h3>Node Colors:</h3>"
            + "<ul>"
            + "<li>Grey: initial and terminal nodes</li>"
            + "<li>Yellow: nodes that did not change in the last refinement step</li>"
            + "<li>Dark Green: nodes that <b>changed</b> in the last refinement step</li>"
            + "<li>Light Green: nodes that <b>created</b> by the last refinement step</li>"
            + "</ul>"
            + "<h3>All Modes:</h3>"
            + "<ul>"
            + "<li>Mousewheel scales with a crossover value of 1.0.<p>"
            + "     - scales the graph layout when the combined scale is greater than 1<p>"
            + "     - scales the graph view when the combined scale is less than 1"
            + "</ul>"
            + "<h3>Transforming Mode: (right mouse clicks)</h3>"
            + "<ul>"
            + "<li>Mouse3+drag pans the graph"
            + "<li>Mouse3+Shift+drag rotates the graph"
            + "<li>Mouse3+CTRL(or Command)+drag shears the graph"
            // +
            // "<li>Mouse3 double-click on a vertex or edge allows you to edit the label"
            + "</ul>"
            + "<h3>Picking Mode: (left mouse clicks)</h3>"
            + "<ul>"
            + "<li>Mouse1 on a Vertex selects the vertex"
            + "<li>Mouse1 elsewhere unselects all Vertices"
            + "<li>Mouse1+Shift on a Vertex adds/removes Vertex selection"
            + "<li>Mouse1+drag on a Vertex moves all selected Vertices"
            + "<li>Mouse1+drag elsewhere selects Vertices in a region"
            + "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"
            + "<li>Mouse1+CTRL on a Vertex selects the vertex and centers the display on it"
            + "<li>Mouse1 double-click on a vertex allows you to view the log lines represented by that node"
            // +
            // "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"
            + "</ul>" + "</html>";

    Set<ITemporalInvariant> unsatisfiedInvariants;
    int numSplitSteps = 0;

    /**
     * Partitions that are known from the prior refinement step are mapped to
     * the number of LogEvents they contain.
     */
    LinkedHashMap<Partition, Integer> oldPartitions;
    /**
     * Partitions that were created in the latest refinement step are mapped to
     * the number of LogEvents they contain.
     */
    LinkedHashMap<Partition, Integer> newPartitions;

    static final Color lightGreenColor;
    static {
        float[] hsb = Color.RGBtoHSB(50, 190, 50, null);
        lightGreenColor = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    /**
     * Creates a new JApplet based on a given PartitionGraph.
     * 
     * @param pGraph
     * @throws Exception
     */
    public JungGui(PartitionGraph pGraph) throws Exception {

        unsatisfiedInvariants = new LinkedHashSet<ITemporalInvariant>();
        unsatisfiedInvariants.addAll(pGraph.getInvariants().getSet());

        this.pGraph = pGraph;

        newPartitions = new LinkedHashMap<Partition, Integer>();
        for (Partition p : pGraph.getNodes()) {
            newPartitions.put(p, p.getEvents().size());
        }
        oldPartitions = newPartitions;

        currentPath = new HashSet<ITransition<Partition>>();

        jGraph = getJGraph();

        frame = new JFrame();

        frame.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizePanel();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        setUpGui();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        addMenuBar(frame);

        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Makes File menu
     * 
     * @param frame
     */
    public void addMenuBar(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        createFileMenu(fileMenu);
        menuBar.add(fileMenu);

        // JMenu actionsMenu = new JMenu("Synoptic Actions");

        JMenu graphLayouts = new JMenu("Graph Layouts");
        createLayoutsMenu(graphLayouts);
        menuBar.add(graphLayouts);

        menuBar.add(createRefineButton());
        menuBar.add(createTotalRefineButton());
        menuBar.add(createPathsMenu());

        frame.setJMenuBar(menuBar);

    }

    @SuppressWarnings("serial")
    public void createFileMenu(JMenu fileMenu) {
        fileMenu.add(new AbstractAction("Make Image") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int option = chooser.showSaveDialog(JungGui.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    JungGui.this.writeJPEGImage(file);
                }
            }
        });

        fileMenu.add(new AbstractAction("Print") {
            @Override
            public void actionPerformed(ActionEvent e) {
                PrinterJob printJob = PrinterJob.getPrinterJob();
                printJob.setPrintable(JungGui.this);
                if (printJob.printDialog()) {
                    try {
                        printJob.print();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JMenuItem help = new JMenuItem("Help");
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(vizViewer, instructions);
            }
        });
        fileMenu.add(help);

        fileMenu.add(new AbstractAction("Export as Graphviz dot file and PNG") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = JOptionPane
                        .showInputDialog("Enter name for this export:");
                if (filename != null && filename.length() > 0) {
                    GraphVizExporter exporter = new GraphVizExporter();
                    if (Main.outputPathPrefix != null) {
                        try {
                            exporter.exportAsDotAndPngFast(
                                    Main.outputPathPrefix + "." + filename
                                            + ".dot", pGraph, false);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        JOptionPane
                                .showMessageDialog(
                                        frame,
                                        "Cannot output "
                                                + filename
                                                + "graph. Specify output path prefix using:\n\t"
                                                + Main.getCmdLineOptDesc("outputPathPrefix"));
                    }
                }
            }
        });
    }

    public JButton createPathsMenu() {
        JButton traces = new JButton("View paths through selected vertices");
        traces.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PickedState<INode<Partition>> pState = vizViewer
                        .getPickedVertexState();
                Set<INode<Partition>> pickedVertices = pState.getPicked();
                if (pickedVertices.isEmpty()) {
                    JOptionPane
                            .showMessageDialog(frame, "No vertices selected");
                } else {
                    List<Set<Integer>> partitionIDs = new ArrayList<Set<Integer>>();
                    Set<Integer> temp;
                    for (INode<Partition> v : pickedVertices) {
                        temp = new HashSet<Integer>();
                        for (EventNode event : ((Partition) v).getEvents()) {
                            if (event.getTraceID() != 0) {
                                temp.add(event.getTraceID());
                            }
                        }
                        if (!temp.isEmpty()) {
                            partitionIDs.add(temp);
                        }
                    }
                    if (partitionIDs.isEmpty()) {
                        JOptionPane.showMessageDialog(frame,
                                "No events observed in the selected vertices");
                    } else {
                        Set<Integer> intersectionOfIDs = partitionIDs.get(0);
                        for (int i = 1; i < partitionIDs.size(); i++) {
                            intersectionOfIDs.retainAll(partitionIDs.get(i));
                        }

                        // Now intersectionOfIDs is a set intersection of the
                        // traceIDs for the current selected vertices
                        if (intersectionOfIDs.isEmpty()) {
                            JOptionPane
                                    .showMessageDialog(frame,
                                            "No traces observed through all selected vertices");
                        } else {
                            JFrame optionsFrame = new JFrame("Possible traces");
                            JLabel message = new JLabel("Select path to view",
                                    SwingConstants.CENTER);
                            JPanel panel = new JPanel();
                            final Map<Integer, Set<ITransition<Partition>>> paths = new HashMap<Integer, Set<ITransition<Partition>>>();
                            // System.out.println(intersectionOfIDs);
                            for (Partition p : pGraph.getInitialNodes()) {
                                for (EventNode event : p.getEvents()) {
                                    for (Transition<EventNode> t : event
                                            .getTransitions()) {
                                        int traceID = t.getTarget()
                                                .getTraceID();
                                        if (intersectionOfIDs.contains(traceID)) {
                                            Set<ITransition<Partition>> currentPath = new HashSet<ITransition<Partition>>();
                                            ITransition<Partition> trans = p
                                                    .getTransition(t
                                                            .getTarget()
                                                            .getParent(), t
                                                            .getRelation());
                                            currentPath.add(trans);
                                            traverse(t.getTarget(), currentPath);
                                            paths.put(traceID, currentPath);
                                        }
                                    }
                                }
                            }
                            ButtonGroup traceButtonGroup = new ButtonGroup();
                            for (final Integer trace : intersectionOfIDs) {
                                JRadioButton button = new JRadioButton(
                                        "Trace ID " + trace);
                                button.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        currentPath = paths.get(trace);
                                        displayPath();
                                    }
                                });
                                panel.add(button);
                                traceButtonGroup.add(button);
                                optionsFrame
                                        .addWindowListener(new WindowAdapter() {
                                            @Override
                                            public void windowClosing(
                                                    WindowEvent ev) {
                                                currentPath.clear();
                                                displayPath();
                                            }
                                        });
                                optionsFrame
                                        .setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                                optionsFrame.add(message, BorderLayout.NORTH);
                                optionsFrame.add(panel, BorderLayout.SOUTH);
                                optionsFrame.pack();
                                optionsFrame.setVisible(true);
                            }
                        }
                    }
                }
            }
        });
        return traces;
    }

    private void traverse(EventNode event, Set<ITransition<Partition>> path) {
        // Should be a single transition for totally ordered logs
        for (Transition<EventNode> transition : event.getTransitions()) {
            path.add(event.getParent().getTransition(
                    transition.getTarget().getParent(),
                    transition.getRelation()));
            traverse(transition.getTarget(), path);
        }
    }

    public JButton createTotalRefineButton() {
        totalRefine = new JButton("Completely refine");
        totalRefine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<RelationPath<Partition>> counterExampleTraces = null;
                // Retrieve the counter-examples for the unsatisfied invariants.
                counterExampleTraces = new TemporalInvariantSet(
                        unsatisfiedInvariants).getAllCounterExamples(pGraph);
                unsatisfiedInvariants.clear();
                while (counterExampleTraces != null
                        && counterExampleTraces.size() > 0) {

                    refine(counterExampleTraces);
                    counterExampleTraces = new TemporalInvariantSet(
                            unsatisfiedInvariants)
                            .getAllCounterExamples(pGraph);
                }
                disableRefinement();
            }
        });
        return totalRefine;
    }

    public JButton createRefineButton() {
        refineOption = new JButton("Refine once");
        refineOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<RelationPath<Partition>> counterExampleTraces = null;
                // Retrieve the counter-examples for the unsatisfied invariants.
                counterExampleTraces = new TemporalInvariantSet(
                        unsatisfiedInvariants).getAllCounterExamples(pGraph);
                unsatisfiedInvariants.clear();
                if (counterExampleTraces != null
                        && counterExampleTraces.size() > 0) {
                    refine(counterExampleTraces);

                } else {
                    disableRefinement();
                }
            }
        });
        return refineOption;
    }

    private void disableRefinement() {
        // Set all partitions to 'old'.
        oldPartitions = newPartitions;
        refineOption.setEnabled(false);
        totalRefine.setEnabled(false);
        // Refresh the graphics state.
        JungGui.this.repaint();
    }

    private void refine(List<RelationPath<Partition>> counterExampleTraces) {
        // Perform a single refinement step.
        numSplitSteps = Bisimulation.performOneSplitPartitionsStep(
                numSplitSteps, pGraph, counterExampleTraces);

        // Update the old\new partition maps.
        oldPartitions = newPartitions;
        newPartitions = new LinkedHashMap<Partition, Integer>();
        for (Partition p : pGraph.getNodes()) {
            newPartitions.put(p, p.getEvents().size());
        }

        vizViewer.getGraphLayout().setGraph(JungGui.this.getJGraph());
        // TODO: there must be a better way for the vizViewer to
        // refresh its state..
        vizViewer.setGraphLayout(vizViewer.getGraphLayout());
        JungGui.this.repaint();

        for (RelationPath<Partition> relPath : counterExampleTraces) {
            unsatisfiedInvariants.add(relPath.invariant);
        }
    }

    private void displayPath() {
        vizViewer.getGraphLayout().setGraph(JungGui.this.getJGraph());
        vizViewer.setGraphLayout(vizViewer.getGraphLayout());
        JungGui.this.repaint();
    }

    public void createLayoutsMenu(JMenu graphLayouts) {

        // Map names to layout classes
        final Map<String, Class<?>> labels = new HashMap<String, Class<?>>();
        labels.put("Kamada-Kawai", KKLayout.class);
        labels.put("Force-Directed", FRLayout.class);
        labels.put("Circle", CircleLayout.class);
        labels.put("Spring", SpringLayout.class);
        labels.put("ISOM", ISOMLayout.class);

        ButtonGroup layoutButtonGroup = new ButtonGroup();
        for (final String layout : labels.keySet()) {
            JRadioButton temp = new JRadioButton(layout);
            temp.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectLayout(labels.get(layout));
                }
            });

            if (layout.equals("Force-Directed")) {
                temp.setSelected(true);
            }
            graphLayouts.add(temp);
            layoutButtonGroup.add(temp);
        }

    }

    @SuppressWarnings("unchecked")
    public void selectLayout(Class<?> choice) {
        Object[] constructorArgs = { jGraph };
        Class<?> layoutC = choice;
        try {
            Constructor<?> constructor = layoutC
                    .getConstructor(constructorArgsWanted);
            // Create a new layout instance.
            Object o = constructor.newInstance(constructorArgs);
            // Double check that the item in the combo-box is a valid Layout.
            if (o instanceof Layout<?, ?>) {
                // Initialize the layout with the current layout's graph.
                layout = (Layout<INode<Partition>, ITransition<Partition>>) o;
                layout.setGraph(vizViewer.getGraphLayout().getGraph());
                // Tell the viewer to use the new layout.
                vizViewer.setGraphLayout(layout);
            }
        } catch (Exception e) {
            throw new InternalSynopticException("Could not load layout "
                    + layoutC);
        }
    }

    public DirectedGraph<INode<Partition>, ITransition<Partition>> getJGraph() {
        DirectedSparseMultigraph<INode<Partition>, ITransition<Partition>> newGraph = new DirectedSparseMultigraph<INode<Partition>, ITransition<Partition>>();

        for (INode<Partition> node : pGraph.getNodes()) {
            newGraph.addVertex(node);
        }

        for (INode<Partition> node : pGraph.getNodes()) {
            for (ITransition<Partition> t : node.getTransitionsIterator()) {
                newGraph.addEdge(t, t.getSource(), t.getTarget(),
                        EdgeType.DIRECTED);
            }
        }

        return newGraph;
    }

    public void setUpGui() throws Exception {
        layout = new FRLayout<INode<Partition>, ITransition<Partition>>(jGraph);
        vizViewer = new VisualizationViewer<INode<Partition>, ITransition<Partition>>(
                layout);
        vizViewer.setBackground(Color.white);
        Transformer<INode<Partition>, String> nodeLabeller = new Transformer<INode<Partition>, String>() {
            @Override
            public String transform(INode<Partition> node) {
                return node.getEType().toString();
            }
        };

        vizViewer.getRenderContext().setVertexLabelTransformer(
                MapTransformer.<INode<Partition>, String> getInstance(LazyMap
                        .<INode<Partition>, String> decorate(
                                new HashMap<INode<Partition>, String>(),
                                nodeLabeller)));

        Transformer<ITransition<Partition>, String> transitionLabeller = new Transformer<ITransition<Partition>, String>() {
            @Override
            public String transform(ITransition<Partition> trans) {
                // Compute the label for trans as the number of log events
                // that were observed along the transition edge.
                PartitionSplit s = trans.getSource()
                        .getCandidateDivisionBasedOnOutgoing(trans);
                if (s == null) {
                    s = PartitionSplit.newSplitWithAllEvents(trans.getSource());
                }
                int numOutgoing = s.getSplitEvents().size();
                return String.valueOf(numOutgoing);
            }
        };

        vizViewer
                .getRenderContext()
                .setEdgeLabelTransformer(
                        MapTransformer
                                .<ITransition<Partition>, String> getInstance(LazyMap
                                        .<ITransition<Partition>, String> decorate(
                                                new HashMap<ITransition<Partition>, String>(),
                                                transitionLabeller)));

        vizViewer.setVertexToolTipTransformer(vizViewer.getRenderContext()
                .getVertexLabelTransformer());
        vizViewer.getRenderContext().setVertexShapeTransformer(
                new Transformer<INode<Partition>, Shape>() {
                    @Override
                    public Shape transform(INode<Partition> node) {
                        return new Ellipse2D.Float(-10, -10, 70, 35);
                    }
                });

        vizViewer.getRenderContext().setVertexFillPaintTransformer(
                new Transformer<INode<Partition>, Paint>() {
                    @Override
                    public Paint transform(INode<Partition> node) {
                        // Specially color TERMINAL and INITIAL nodes.
                        if (node.getEType().isInitialEventType()
                                || node.getEType().isTerminalEventType()) {
                            return Color.lightGray;
                        }
                        // Discriminate between:
                        // 1. new nodes that have been created
                        // 2. old nodes that have changed (shrunk in their
                        // message set because of a split)
                        // 3. old nodes that have not changed
                        boolean inOld = oldPartitions.containsKey(node);
                        boolean inNew = newPartitions.containsKey(node);

                        if (inOld && inNew) {
                            if (oldPartitions.get(node) != newPartitions
                                    .get(node)) {
                                // An old node that changed.
                                return lightGreenColor;
                            }
                            // An old node that hasn't changed (but was also not
                            // deleted).
                            return Color.YELLOW;
                        }

                        if (inNew) {
                            // A newly created node.
                            return Color.GREEN;
                        }

                        throw new InternalSynopticException(
                                "Found a node that is neither old nor new OR has been deleted but is being shown.");
                    }
                });

        vizViewer.getRenderContext().setEdgeDrawPaintTransformer(
                new Transformer<ITransition<Partition>, Paint>() {
                    @Override
                    public Paint transform(ITransition<Partition> transition) {
                        // Discriminate between:
                        // 1. edges in the path
                        // 2. edges not in the path
                        return currentPath.contains(transition) ? Color.red
                                : Color.black;
                    }
                });

        vizViewer
                .getRenderer()
                .setVertexLabelRenderer(
                        new BasicVertexLabelRenderer<INode<Partition>, ITransition<Partition>>(
                                Position.CNTR));

        vizViewer.getRenderer().setVertexRenderer(new GuiVertex());

        Container content = getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vizViewer);
        content.add(panel);

        final ModalMousePlugin graphMouse = new ModalMousePlugin(
                vizViewer.getRenderContext());

        vizViewer.setGraphMouse(graphMouse);
        vizViewer.addKeyListener(graphMouse.getModeKeyListener());

        JPanel logLineWindow = new JPanel(new BorderLayout());

        CustomMousePlugin mousePlugIn = new CustomMousePlugin(logLineWindow);
        vizViewer.addMouseListener(mousePlugIn);
        frame.add(logLineWindow, BorderLayout.SOUTH);
    }

    /**
     * copy the visible part of the graph to a file as a jpeg image
     * 
     * @param file
     */
    public void writeJPEGImage(File file) {
        int width = vizViewer.getWidth();
        int height = vizViewer.getHeight();

        BufferedImage bi = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();
        vizViewer.paint(graphics);
        graphics.dispose();

        try {
            ImageIO.write(bi, "jpeg", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int print(java.awt.Graphics graphics,
            java.awt.print.PageFormat pageFormat, int pageIndex)
            throws java.awt.print.PrinterException {
        if (pageIndex > 0) {
            return Printable.NO_SUCH_PAGE;
        } else {
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
            vizViewer.setDoubleBuffered(false);
            g2d.translate(pageFormat.getImageableX(),
                    pageFormat.getImageableY());

            vizViewer.paint(g2d);
            vizViewer.setDoubleBuffered(true);

            return Printable.PAGE_EXISTS;
        }
    }

    // questionable
    private JTable table;
    private final int scrollBarWidth = 28;
    private TableColumnAdjuster adjuster;

    protected class CustomMousePlugin implements MouseListener {
        private final Dimension defaultSize = new Dimension(600, 100);
        private final LogLineTableModel dataModel;

        // private TableColumnAdjuster adjuster;
        // private Table table;

        public CustomMousePlugin(JPanel logLineWindow) {
            dataModel = new LogLineTableModel(new Object[0][0]);
            table = new JTable(dataModel);

            table.getColumnModel().getColumn(0).setHeaderValue("Line #");
            table.getColumnModel().getColumn(1).setHeaderValue("Line");
            table.getColumnModel().getColumn(2).setHeaderValue("File");

            table.setMinimumSize(defaultSize);
            table.setPreferredSize(defaultSize);
            table.setPreferredScrollableViewportSize(defaultSize);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setFillsViewportHeight(true);

            JScrollPane scrollPane = new JScrollPane(table);

            scrollPane.setViewportView(table);
            scrollPane
                    .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane
                    .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

            adjuster = new TableColumnAdjuster(table);
            logLineWindow.add(scrollPane);

        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                final Point2D p = e.getPoint();
                GraphElementAccessor<INode<Partition>, ITransition<Partition>> location = vizViewer
                        .getPickSupport();

                if (location != null) {
                    final Partition vertex = (Partition) location.getVertex(
                            layout, p.getX(), p.getY());

                    if (vertex != null) {

                        ArrayList<String[]> validLines = new ArrayList<String[]>();
                        for (EventNode event : vertex.getEvents()) {
                            if (event.getLine() != null) {
                                validLines.add(new String[] {
                                        event.getLineNum(), event.getLine(),
                                        event.getShortFileName() });
                            }
                        }
                        Object[][] data = validLines
                                .toArray(new Object[validLines.size()][3]);
                        dataModel.setData(data);
                        resizePanel();
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    private void resizePanel() {
        adjuster.adjustColumns();
        int width = Math.max(frame.getWidth() - scrollBarWidth, table
                .getColumnModel().getTotalColumnWidth());
        table.setPreferredSize(new Dimension(width,
                (table.getRowHeight() + table.getRowMargin())
                        * table.getRowCount()));
    }
}
