package synopticgwt.client.invariants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;

import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;

/**
 * Used to create an invariants graphic in which an invariant inv(x,y) is
 * represented as a line between two vertices x and y. The graph is tripartite
 * graph (three sets of vertices, with no edges between vertices in the same
 * set). The sets have identical sizes and contain the same kinds of vertices --
 * a vertex corresponding to each event type that is part of at least one
 * invariant.
 */
public class InvariantsGraph {
	
	public static String DEFAULT_STROKE = "grey";
    public static String AP_HIGHLIGHT_STROKE = "blue";
    public static String AFBY_HIGHLIGHT_STROKE = "blue";
    public static String NFBY_HIGHLIGHT_STROKE = "red";

    public static int DEFAULT_STROKE_WIDTH = 1;
    public static int HIGHLIGHT_STROKE_WIDTH = 3;

    public static String DEFAULT_FILL = "grey";
    public static String HIGHLIGHT_FILL = "black";

    /** Distance of invariant columns from top of paper */
    public static final int TOP_MARGIN = 20;
    /** Distance of invariant columns from top of paper */
    public static final int DY = 50;
    
    // Raphael paper object
    private JavaScriptObject paper;
    private Set<GraphicInvariant> graphicInvariants;
    private Map<String, GraphicEvent> leftEventCol;
    private Map<String, GraphicEvent> midEventCol;
    private Map<String, GraphicEvent> rightEventCol;
    private List<GraphicInvariant> apInvs;
    private List<GraphicInvariant> afbyInvs;
    private List<GraphicInvariant> nfbyInvs;

    /**
     * Creates an empty InvariantsGraph
     */
    public InvariantsGraph() {
        this.leftEventCol = new HashMap<String, GraphicEvent>();
        this.midEventCol = new HashMap<String, GraphicEvent>();
        this.rightEventCol = new HashMap<String, GraphicEvent>();
        this.apInvs = new ArrayList<GraphicInvariant>();
        this.afbyInvs = new ArrayList<GraphicInvariant>();
        this.nfbyInvs = new ArrayList<GraphicInvariant>();
    }

    /**
     * Creates the invariant graphic corresponding to gwtInvs in a DIV with id
     * indicated by invCanvasId.
     */
    public void createInvariantsGraphic(GWTInvariantSet gwtInvs,
            String invCanvasId) {
        Set<String> invTypes = gwtInvs.getInvTypes();

        Set<String> eventTypesSet = new LinkedHashSet<String>();
        int longestEType = 0;

        // Generate set of eTypes
        for (String invType : invTypes) {
            List<GWTInvariant> invs = gwtInvs.getInvs(invType);

            for (GWTInvariant inv : invs) {
                String src = inv.getSource();
                eventTypesSet.add(src);
                int srcLen = src.length();
                if (srcLen > longestEType) {
                    longestEType = srcLen;
                }

                String dst = inv.getTarget();
                eventTypesSet.add(dst);
                int dstLen = dst.length();
                if (dstLen > longestEType) {
                    longestEType = dstLen;
                }
            }
        }

        List<String> eventTypesList = new ArrayList<String>(eventTypesSet);

        // A little magic to size things right.
        // int lX = (longestEType * 30) / 2 - 110;
        int lX = (longestEType * 30) / 2 - 110 + 50;
        // int mX = lX + (longestEType * 30) - 110;
        int mX = lX + (longestEType * 30) - 110 + 50;
        // int rX = mX + (longestEType * 30) - 110;
        int rX = mX + (longestEType * 30) - 110 + 50;
        int width = rX + 200;
        int height = (eventTypesList.size() + 1) * DY;

        int fontSize = 20; // getFontSize(longestEType);

        this.paper = constructPaper(width, height, invCanvasId);

        // draw graphic event type columns
        for (int i = 0; i < eventTypesList.size(); i++) {
        	String eType = eventTypesList.get(i);
            GraphicEvent leftEvent = new GraphicEvent(lX, DY * i + TOP_MARGIN, 
                fontSize, eType, paper);
            leftEventCol.put(eType, leftEvent);

            GraphicEvent midEvent = new GraphicEvent(mX, DY * i + TOP_MARGIN, 
        		fontSize, eType, paper);
            midEventCol.put(eType, midEvent);

            GraphicEvent rightEvent = new GraphicEvent(rX, DY * i + TOP_MARGIN, 
        		fontSize, eType, paper);
            rightEventCol.put(eType, rightEvent);
        }

        for (String invType : invTypes) {
            List<GWTInvariant> invs = gwtInvs.getInvs(invType);
            if (invType.equals("AP")) {
                List<GraphicInvariant> gInvs = 
                    drawInvariants(invs, leftEventCol, midEventCol);
                apInvs.addAll(gInvs);
            } else if (invType.equals("AFby")) {
                List<GraphicInvariant> gInvs = 
                    drawInvariants(invs, midEventCol, rightEventCol);
                afbyInvs.addAll(gInvs);
            } else if (invType.equals("NFby")) {
                List<GraphicInvariant> gInvs = 
                    drawInvariants(invs, midEventCol, rightEventCol);
                nfbyInvs.addAll(gInvs);
            }
        }
    }

    /** 
     * Takes lists of GWTInvariants, source GraphicEvents, and destination 
     * GraphicEvents and creates/draws the GraphicInvariant representing a
     * GWTInvariant and liking a GraphicEvent from srcCol to dstCol.
     * */
    private List<GraphicInvariant> drawInvariants(List<GWTInvariant> invs, 
            Map<String, GraphicEvent> srcCol,
            Map<String, GraphicEvent> dstCol) {
        List<GraphicInvariant> result = new ArrayList<GraphicInvariant>();
        for (GWTInvariant inv : invs) {
            String srcEventString = inv.getSource();
            GraphicEvent srcEvent = srcCol.get(srcEventString);

            String dstEventString = inv.getTarget();
            GraphicEvent dstEvent = dstCol.get(dstEventString);

            GraphicInvariant gInv = new GraphicInvariant(srcEvent, dstEvent,
                inv, paper);
            srcEvent.addInvariant(gInv);
            dstEvent.addInvariant(gInv);
            result.add(gInv);
        }
        return result;
    }

    /**
     * Determines and returns the font size to use font showing event types in
     * the invariant graphic, based on the length of the longest event type.
     * 
     * <pre>
     * NOTE: this code depends on the invariant graphic being size using:
     *   lX = (longestEType * 30) / 2 - 60;
     *   mX = lX + (longestEType * 30);
     *   rX = mX + (longestEType * 30);
     *   width = rX + 50;
     * </pre>
     * 
     * @param longestEType
     * @return
     */
    private static int getFontSize(int longestEType) {
        // The max font we'll use is 30pt
        int fontSizeMax = 30;
        // The smallest font size we can use is about 10pt
        int fontSizeMin = 10;
        int fontSize = fontSizeMax;
        // The longest event type we can show is "wwwwww" (at 30pt)
        if (longestEType > 6) {
            // When we get above 6, we scale down from 30. The 4.0 is a magic
            // number determined through a few experiments with varying w.+
            // etypes.
            fontSize = (int) (30.0 * (4.0 / (1.0 * longestEType)));
        }
        // If we scale below min font size, then we just use the smallest font
        // -- this won't be pretty, but at least it won't be invisible.
        if (fontSize < fontSizeMin) {
            fontSize = fontSizeMin;
        }
        return fontSize;
    }
    
    /**
     * Creates a new Raphael paper object
     * 
     * @param width width of the Raphael paper object
     * @param height height of the Raphael paper object
     * @param canvasId Document element to put Raphael paper object into
     * @return Raphael paper object
     */
    public native JavaScriptObject constructPaper(int width, int height, 
            String canvasId) /*-{
		var paper = $wnd.Raphael($doc.getElementById(canvasId), width, height);
        return paper;
    }-*/;

    /**
     * Gets the Raphael paper object
     * @return Raphael paper object
     */
    public JavaScriptObject getGraphicPaper() {
        return paper;
    }
}
