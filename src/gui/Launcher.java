/*                                                                                     
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of           
 * California All rights reserved.                                                     
 *                                                                                     
 * This software is open-source under the BSD license; see either "license.txt"        
 * or http://jung.sourceforge.net/license.txt for a description.                       
 *                                                                                     
 */                                                                                    
package gui;                                                      

import java.awt.BorderLayout;
import java.awt.Color;       
import java.awt.Container;   
import java.awt.Dimension;   
import java.awt.Graphics2D;  
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage; 
import java.awt.print.Printable;     
import java.awt.print.PrinterJob;    
import java.io.File;                 
import java.io.IOException;
import java.util.HashMap;            

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

import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;
import model.input.PetersonReader;
import model.interfaces.INode;
import model.interfaces.ITransition;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;            

import algorithms.bisim.Bisimulation;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;  
import edu.uci.ics.jung.graph.Graph;                     
import edu.uci.ics.jung.graph.SparseMultigraph;          
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.annotations.AnnotationControls;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse; 
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;        
import edu.uci.ics.jung.visualization.control.ScalingControl;         
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;    

/**
 * Shows how  to create a graph editor with JUNG.
 * Mouse modes and actions are explained in the help text.
 * The application version of GraphEditorDemo provides a  
 * File menu with an option to save the visible graph as  
 * a jpeg file.                                           
 *                                                        
 * @author Tom Nelson                                     
 *                                                        
 */                                                       
public class Launcher extends JApplet implements Printable {

    /**
         * 
         */
        private static final long serialVersionUID = -2023243689258876709L;

        /**
     * the graph
     */         
    SparseMultigraph<INode, ITransition> graph;
                               
    Layout<INode, ITransition> layout;

    /**
     * the visual component and renderer for the graph
     */                                               
    VisualizationViewer<INode, ITransition> vv;            
                                                      
    String instructions =                             
        "<html>"+                                     
        "<h3>All Modes:</h3>"+                        
        "<ul>"+                                       
        "<li>Right-click an empty area for <b>Create Vertex</b> popup"+
        "<li>Right-click on a Vertex for <b>Delete Vertex</b> popup"+  
        "<li>Right-click on a Vertex for <b>Add Edge</b> menus <br>(if there are selected Vertices)"+
        "<li>Right-click on an Edge for <b>Delete Edge</b> popup"+                                   
        "<li>Mousewheel scales with a crossover value of 1.0.<p>"+                                   
        "     - scales the graph layout when the combined scale is greater than 1<p>"+               
        "     - scales the graph view when the combined scale is less than 1"+                       

        "</ul>"+
        "<h3>Editing Mode:</h3>"+
        "<ul>"+                  
        "<li>Left-click an empty area to create a new Vertex"+
        "<li>Left-click on a Vertex and drag to another Vertex to create an Undirected Edge"+
        "<li>Shift+Left-click on a Vertex and drag to another Vertex to create a Directed Edge"+
        "</ul>"+                                                                                
        "<h3>Picking Mode:</h3>"+                                                               
        "<ul>"+                                                                                 
        "<li>Mouse1 on a Vertex selects the vertex"+                                            
        "<li>Mouse1 elsewhere unselects all Vertices"+                                          
        "<li>Mouse1+Shift on a Vertex adds/removes Vertex selection"+                           
        "<li>Mouse1+drag on a Vertex moves all selected Vertices"+                              
        "<li>Mouse1+drag elsewhere selects Vertices in a region"+                               
        "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"+                     
        "<li>Mouse1+CTRL on a Vertex selects the vertex and centers the display on it"+         
        "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"+             
        "</ul>"+                                                                                
        "<h3>Transforming Mode:</h3>"+                                                          
        "<ul>"+                                                                                 
        "<li>Mouse1+drag pans the graph"+                                                       
        "<li>Mouse1+Shift+drag rotates the graph"+                                              
        "<li>Mouse1+CTRL(or Command)+drag shears the graph"+                                    
        "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"+             
        "</ul>"+                                                                                
        "<h3>Annotation Mode:</h3>"+                                                            
        "<ul>"+                                                                                 
        "<li>Mouse1 begins drawing of a Rectangle"+                                             
        "<li>Mouse1+drag defines the Rectangle shape"+                                          
        "<li>Mouse1 release adds the Rectangle as an annotation"+                               
        "<li>Mouse1+Shift begins drawing of an Ellipse"+                                        
        "<li>Mouse1+Shift+drag defines the Ellipse shape"+                                      
        "<li>Mouse1+Shift release adds the Ellipse as an annotation"+                           
        "<li>Mouse3 shows a popup to input text, which will become"+                            
        "<li>a text annotation on the graph at the mouse location"+                             
        "</ul>"+                                                                                
        "</html>";                                                                              
                                                                                                
    /**                                                                                         
     * create an instance of a simple graph with popup controls to                              
     * create a graph.                                                                          
     * @throws InterruptedException 
     * @throws IOException 
     *                                                                                          
     */                                                                                         
    public Launcher() throws IOException, InterruptedException {                                                                  
                                                                                                
        // create a simple graph for the demo                                                   
        graph = new SparseMultigraph<model.interfaces.INode,model.interfaces.ITransition>();                                          
        loadGraph();
        
        this.layout = new ISOMLayout<INode,ITransition>(graph);                     
                                                             
        vv =  new VisualizationViewer<INode,ITransition>(layout);
        vv.setBackground(Color.white);                       
        Transformer<INode, String> labeller2 = new Transformer<INode, String>() {
			@Override
			public String transform(INode arg0) {
				return arg0.toStringConcise();
			}
		};
        vv.getRenderContext().setVertexLabelTransformer(MapTransformer.<INode,String>getInstance(
                        LazyMap.<INode,String>decorate(new HashMap<INode,String>(), labeller2)));
                                                                                                                        
        Transformer<ITransition, String> labeller = new Transformer<ITransition, String>() {

			@Override
			public String transform(ITransition arg0) {
				return arg0.toStringConcise();
			}
		};
		
		 
		
		vv.getRenderContext().setEdgeLabelTransformer(MapTransformer.<ITransition,String>getInstance(                        
                        LazyMap.<ITransition,String>decorate(new HashMap<ITransition,String>(), labeller)));

        vv.setVertexToolTipTransformer(vv.getRenderContext().getVertexLabelTransformer());
                                                                                          

        Container content = getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);                                           
        Factory<INode> vertexFactory = new VertexFactory();          
        Factory<ITransition> edgeFactory = new EdgeFactory();              
                                                                      
        final EditingModalGraphMouse<INode,ITransition> graphMouse =      
                new EditingModalGraphMouse<INode,ITransition>(vv.getRenderContext(), vertexFactory, edgeFactory);
                                                                                                             
        // the EditingGraphMouse will pass mouse event coordinates to the                                    
        // vertexLocations function to set the locations of the vertices as                                  
        // they are created                                                                                  
//        graphMouse.setVertexLocations(vertexLocations);                                                    
        vv.setGraphMouse(graphMouse);                                                                        
        vv.addKeyListener(graphMouse.getModeKeyListener());                                                  

        graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
                                                         
        final ScalingControl scaler = new CrossoverScalingControl();
        JButton plus = new JButton("+");                            
        plus.addActionListener(new ActionListener() {               
            public void actionPerformed(ActionEvent e) {            
                scaler.scale(vv, 1.1f, vv.getCenter());             
            }                                                       
        });                                                         
        JButton minus = new JButton("-");                           
        minus.addActionListener(new ActionListener() {              
            public void actionPerformed(ActionEvent e) {            
                scaler.scale(vv, 1/1.1f, vv.getCenter());           
            }                                                       
        });                                                         
                                                                    
        JButton help = new JButton("Help");                         
        help.addActionListener(new ActionListener() {               

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(vv, instructions);
            }});                                                

        AnnotationControls<INode,ITransition> annotationControls = 
                new AnnotationControls<INode,ITransition>(graphMouse.getAnnotatingPlugin());
        JPanel controls = new JPanel();                                                 
        controls.add(plus);                                                             
        controls.add(minus);                                                            
        JComboBox modeBox = graphMouse.getModeComboBox();                               
        controls.add(modeBox);                                                          
        controls.add(annotationControls.getAnnotationsToolBar());                       
        controls.add(help);                                                             
        content.add(controls, BorderLayout.SOUTH);                                      
    }                                                                                   
                                                                                        
    /**                                                                                 
     * copy the visible part of the graph to a file as a jpeg image                     
     * @param file                                                                      
     */                                                                                 
    public void writeJPEGImage(File file) {                                             
        int width = vv.getWidth();                                                      
        int height = vv.getHeight();                                                    

        BufferedImage bi = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);               
        Graphics2D graphics = bi.createGraphics();         
        vv.paint(graphics);                                
        graphics.dispose();                                
                                                           
        try {                                              
            ImageIO.write(bi, "jpeg", file);               
        } catch (Exception e) {                            
            e.printStackTrace();                           
        }                                                  
    }                                                      
                                                           
    public int print(java.awt.Graphics graphics,           
            java.awt.print.PageFormat pageFormat, int pageIndex)
            throws java.awt.print.PrinterException {            
        if (pageIndex > 0) {                                    
            return (Printable.NO_SUCH_PAGE);                    
        } else {                                                
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
            vv.setDoubleBuffered(false);                             
            g2d.translate(pageFormat.getImageableX(), pageFormat     
                    .getImageableY());                               

            vv.paint(g2d);
            vv.setDoubleBuffered(true);

            return (Printable.PAGE_EXISTS);
        }                                  
    }                                      
                                           
    class VertexFactory implements Factory<INode> {
                public INode create() {
                        return null;
                }                       
    }                                   
                                        
    class EdgeFactory implements Factory<ITransition> {
                public ITransition create() {
                        return null;     
                }
    }
    
    public void loadGraph() throws IOException, InterruptedException {
    	GraphBuilder b = new GraphBuilder();
		PetersonReader<MessageEvent> r = new PetersonReader<MessageEvent>(b);
		r.readGraphSet("traces/PetersonLeaderElection/generated_traces/no-rand-1.trace", 10);
		model.Graph<MessageEvent> g = b.getRawGraph();
		PartitionGraph pg = Bisimulation.getRefinedSystem(g);
		copyFrom(pg);
    }

    private void copyFrom(PartitionGraph pg) {
		for (model.interfaces.INode node : pg.getNodes()) {
			graph.addVertex(node);
		}
		
		for (model.interfaces.INode<Partition> node : pg.getNodes()) {
			for (ITransition<Partition> t : node.getTransitionsIterator()) {
				graph.addEdge(t, t.getSource(), t.getTarget(), EdgeType.DIRECTED);
			}
		}
	}

	/**
     * a driver for this demo
	 * @throws InterruptedException 
	 * @throws IOException 
     */
    @SuppressWarnings("serial")
        public static void main(String[] args) throws IOException, InterruptedException {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final Launcher demo = new Launcher();

        JMenu menu = new JMenu("File");
        menu.add(new AbstractAction("Make Image") {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser  = new JFileChooser();
                int option = chooser.showSaveDialog(demo);
                if(option == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    demo.writeJPEGImage(file);
                }
            }});
        menu.add(new AbstractAction("Print") {
            public void actionPerformed(ActionEvent e) {
                    PrinterJob printJob = PrinterJob.getPrinterJob();
                    printJob.setPrintable(demo);
                    if (printJob.printDialog()) {
                        try {
                            printJob.print();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
            }});
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
        frame.getContentPane().add(demo);
        frame.pack();
        frame.setVisible(true);
    }
}
