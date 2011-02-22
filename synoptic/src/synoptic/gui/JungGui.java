package synoptic.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.algorithms.graph.PartitionSplit;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.RelationPath;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
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

    private static final long serialVersionUID = -2023243689258876709L;

    /**
     * Java frame used by the gui.
     */
    JFrame frame;

    /**
     * The partition graph maintained by Synoptic.
     */
    PartitionGraph pGraph;

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
            + "<h3>Transforming Mode:</h3>"
            + "<ul>"
            + "<li>Mouse1+drag pans the graph"
            + "<li>Mouse1+Shift+drag rotates the graph"
            + "<li>Mouse1+CTRL(or Command)+drag shears the graph"
            + "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"
            + "</ul>"
            + "<h3>Picking Mode:</h3>"
            + "<ul>"
            + "<li>Mouse1 on a Vertex selects the vertex"
            + "<li>Mouse1 elsewhere unselects all Vertices"
            + "<li>Mouse1+Shift on a Vertex adds/removes Vertex selection"
            + "<li>Mouse1+drag on a Vertex moves all selected Vertices"
            + "<li>Mouse1+drag elsewhere selects Vertices in a region"
            + "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"
            + "<li>Mouse1+CTRL on a Vertex selects the vertex and centers the display on it"
            + "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"
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
            newPartitions.put(p, p.getMessages().size());
        }
        oldPartitions = newPartitions;

        jGraph = getJGraph();
        setUpGui();
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addMenuBar(frame);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);
    }

    @SuppressWarnings("serial")
    public void addMenuBar(JFrame frame) {
        JMenu menu = new JMenu("File");
        menu.add(new AbstractAction("Make Image") {
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
        menu.add(new AbstractAction("Print") {
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
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
    }

    public DirectedGraph<INode<Partition>, ITransition<Partition>> getJGraph() {
        DirectedSparseGraph<INode<Partition>, ITransition<Partition>> newGraph = new DirectedSparseGraph<INode<Partition>, ITransition<Partition>>();

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
                return node.toStringConcise();
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

                // toSTringConcise() returns 0.00 because transition
                // weight\frequencies are not updated during Bisimulation.
                // return trans.toStringConcise();
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
                        if (node.getLabel().equals(Main.initialNodeLabel)
                                || node.getLabel().equals(
                                        Main.terminalNodeLabel)) {
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

        vizViewer
                .getRenderer()
                .setVertexLabelRenderer(
                        new BasicVertexLabelRenderer<INode<Partition>, ITransition<Partition>>(
                                Position.CNTR));

        vizViewer.getRenderer().setVertexRenderer(new GuiVertex());

        Container content = getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vizViewer);
        content.add(panel);

        final EditingModalGraphMouse<INode<Partition>, ITransition<Partition>> graphMouse = new EditingModalGraphMouse<INode<Partition>, ITransition<Partition>>(
                vizViewer.getRenderContext(), null, null);

        vizViewer.setGraphMouse(graphMouse);
        vizViewer.addKeyListener(graphMouse.getModeKeyListener());

        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

        final JButton refineButton = new JButton("Refine");
        refineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<RelationPath<Partition>> counterExampleTraces = null;
                // Retrieve the counter-examples for the unsatisfied invariants.
                counterExampleTraces = new TemporalInvariantSet(
                        unsatisfiedInvariants).getAllCounterExamples(pGraph);
                unsatisfiedInvariants.clear();
                if (counterExampleTraces != null
                        && counterExampleTraces.size() > 0) {
                    // Perform a single refinement step.
                    numSplitSteps = Bisimulation.performOneSplitPartitionsStep(
                            numSplitSteps, pGraph, counterExampleTraces);

                    // Update the old\new partition maps.
                    oldPartitions = newPartitions;
                    newPartitions = new LinkedHashMap<Partition, Integer>();
                    for (Partition p : pGraph.getNodes()) {
                        newPartitions.put(p, p.getMessages().size());
                    }

                    vizViewer.getGraphLayout().setGraph(
                            JungGui.this.getJGraph());
                    // TODO: there must be a better way for the vizViewer to
                    // refresh its state..
                    vizViewer.setGraphLayout(vizViewer.getGraphLayout());
                    JungGui.this.repaint();

                    for (RelationPath<Partition> relPath : counterExampleTraces) {
                        unsatisfiedInvariants.add(relPath.invariant);
                    }

                } else {
                    // Set all partitions to 'old'.
                    oldPartitions = newPartitions;
                    refineButton.setEnabled(false);
                    // Refresh the graphics state.
                    JungGui.this.repaint();
                }
            }
        });

        JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(vizViewer, instructions);
            }
        });

        JPanel controls = new JPanel();
        controls.add(refineButton);
        JComboBox modeBox = graphMouse.getModeComboBox();
        controls.add(modeBox);

        // Remove ANNOTATING and EDITING modeBox items:
        modeBox.removeItemAt(2);
        modeBox.removeItemAt(2);

        // Temporarily hide the layout chooser combo box
        // LayoutChooser.addLayoutCombo(controls, jGraph, vizViewer);

        controls.add(help);

        content.add(controls, BorderLayout.SOUTH);
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
}
