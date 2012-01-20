package synopticgwt.client.invariants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synopticgwt.client.invariants.model.Arrow;
import synopticgwt.client.invariants.model.Event;
import synopticgwt.client.invariants.model.ACPartition;
import synopticgwt.client.invariants.model.NCPartition;
import synopticgwt.client.invariants.model.POInvariant;
import synopticgwt.client.invariants.model.TOInvariant;
import synopticgwt.client.util.Paper;
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

    public static final String DEFAULT_STROKE = "grey";
    public static final String AP_HIGHLIGHT_STROKE = "blue";
    public static final String AFBY_HIGHLIGHT_STROKE = "blue";
    public static final String NFBY_HIGHLIGHT_STROKE = "red";

    public static final int DEFAULT_STROKE_WIDTH = 1;
    public static final int HIGHLIGHT_STROKE_WIDTH = 3;

    public static final String DEFAULT_FILL = "grey";
    public static final String ORDERED_FILL = "black";
    public static final String CONCURRENT_FILL = "blue";
    public static final String NEVER_CONCURRENT_FILL = "red";

    /** Distance of invariant columns from top of paper */
    public static final int TOP_MARGIN = 20;
    /** Distance of invariant columns from top of paper */
    public static final int EVENT_PADDING = 50;
    
    public static final int MIN_LABEL_WIDTH = 100;
    public static final int MIN_LABEL_HEIGHT = 20;
    public static final int MIN_HORIZONTAL_ARROW_SPACE = 100;
    public static final int MIN_VERTICAL_LABEL_BUFFER = 20;
    
    public static final int EVENT_COLUMNS = 3;
    
    public static final int ARROW_LABEL_BUFFER = 20;

    /** Wrapped raphael canvas */
    private Paper paper;
    
    /** Arrow indicating time axis */
    private Arrow timeArrow;
    /** Label for time arrow */
    private Label timeLabel;
    
    private List<String> eventTypesList;

    private Map<String, Event> leftEventCol;
    private Map<String, Event> midEventCol;
    private Map<String, Event> rightEventCol;

    private List<TOInvariant> apInvs;
    private List<TOInvariant> afbyInvs;
    private List<TOInvariant> nfbyInvs;

    /*
     * Concurrency Partitions, as of 12/4/11 these are all currently unused,
     * however I am maintaining references to them here since InvariantsGraph is
     * their primary client and the only other reference to them is in the Event
     * classes. If someone wants access to these data structures, this is
     * probably where they should get it.
     */
    @SuppressWarnings("unused")
    private List<ACPartition> leftACPartitions;
    @SuppressWarnings("unused")
    private List<ACPartition> midACPartitions;
    @SuppressWarnings("unused")
    private List<ACPartition> rightACPartitions;
    @SuppressWarnings("unused")
    private List<NCPartition> leftNCPartitions;
    @SuppressWarnings("unused")
    private List<NCPartition> midNCPartitions;
    @SuppressWarnings("unused")
    private List<NCPartition> rightNCPartitions;

    // TODO: Ideally this would refer to
    // synoptic.mode.EventType.initialNodeLabel. However, since this code runs
    // on the client as JS, it won't be able to access this value. An
    // alternative is to communicate this value via an RPC code to the server.
    // This is a bit heavy weight, but is at least a maintainable solution.
    public static final String INITIAL_EVENT_LABEL = "INITIAL";

    /**
     * Creates an empty InvariantsGraph
     */
    public InvariantsGraph() {
        this.leftEventCol = new HashMap<String, Event>();
        this.midEventCol = new HashMap<String, Event>();
        this.rightEventCol = new HashMap<String, Event>();
        this.apInvs = new ArrayList<TOInvariant>();
        this.afbyInvs = new ArrayList<TOInvariant>();
        this.nfbyInvs = new ArrayList<TOInvariant>();
    }

    /**
     * Creates the invariant graphic corresponding to gwtInvs in a DIV with id
     * indicated by invCanvasId.
     */
    public void createInvariantsGraphic(GWTInvariantSet gwtInvs,
            String invCanvasId,
            Map<GWTInvariant, InvariantGridLabel> gwtInvToIGridLabel) {
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

        eventTypesList = new ArrayList<String>(eventTypesSet);

        // A little magic to size things right.
        // int lX = (longestEType * 30) / 2 - 110;
        int lX = (longestEType * 30) / 2 - 110 + 50;
        // int mX = lX + (longestEType * 30) - 110;
        int mX = lX + (longestEType * 30) - 110 + 50;
        // int rX = mX + (longestEType * 30) - 110;
        int rX = mX + (longestEType * 30) - 110 + 50;
        int width = rX + 200;
        // 2 is a little magical here, need it for time arrow/label
        int height = (eventTypesList.size() + 2) * EVENT_PADDING;

        int fontSize = 20; // getFontSize(longestEType);

        // Used to offset arrow heads and bases from overlapping event labels
        int labelOffset = 0;

        this.paper = new Paper(width, height, invCanvasId);

        // Sort eventTypesList alphabetically
        Collections.sort(eventTypesList);

        // Put initial at the head of the list
        if (eventTypesList.contains(INITIAL_EVENT_LABEL)) {
            int initialETypeIndex = eventTypesList.indexOf(INITIAL_EVENT_LABEL);
            eventTypesList.remove(initialETypeIndex);
            eventTypesList.add(0, INITIAL_EVENT_LABEL);
        }

        // draw graphic event type columns
        for (int i = 0; i < eventTypesList.size(); i++) {
            String eType = eventTypesList.get(i);
            Event leftEvent = new Event(lX, EVENT_PADDING * i + TOP_MARGIN,
                    fontSize, eType, paper);
            leftEventCol.put(eType, leftEvent);

            Event midEvent = new Event(mX, EVENT_PADDING * i + TOP_MARGIN,
                    fontSize, eType, paper);
            midEventCol.put(eType, midEvent);

            Event rightEvent = new Event(rX, EVENT_PADDING * i + TOP_MARGIN,
                    fontSize, eType, paper);
            rightEventCol.put(eType, rightEvent);
        }

        for (String invType : invTypes) {
            List<GWTInvariant> invs = gwtInvs.getInvs(invType);
            if (invType.equals("AP")) {
                List<TOInvariant> gInvs = drawTOInvariants(invs, leftEventCol,
                        midEventCol, gwtInvToIGridLabel, labelOffset);
                apInvs.addAll(gInvs);
            } else if (invType.equals("AFby")) {
                List<TOInvariant> gInvs = drawTOInvariants(invs, midEventCol,
                        rightEventCol, gwtInvToIGridLabel, labelOffset);
                afbyInvs.addAll(gInvs);
            } else if (invType.equals("NFby")) {
                List<TOInvariant> gInvs = drawTOInvariants(invs, midEventCol,
                        rightEventCol, gwtInvToIGridLabel, labelOffset);
                nfbyInvs.addAll(gInvs);
            } else if (invType.equals("ACwith")) {
                leftACPartitions = generateACPartitions(drawPOInvariants(invs,
                        leftEventCol, gwtInvToIGridLabel));
                midACPartitions = generateACPartitions(drawPOInvariants(invs,
                        midEventCol, gwtInvToIGridLabel));
                rightACPartitions = generateACPartitions(drawPOInvariants(invs,
                        rightEventCol, gwtInvToIGridLabel));
            } else if (invType.equals("NCwith")) {
                leftNCPartitions = generateNCPartitions(drawPOInvariants(invs,
                        leftEventCol, gwtInvToIGridLabel));
                midNCPartitions = generateNCPartitions(drawPOInvariants(invs,
                        midEventCol, gwtInvToIGridLabel));
                rightNCPartitions = generateNCPartitions(drawPOInvariants(invs,
                        rightEventCol, gwtInvToIGridLabel));

            }
        }

        /*
         * Draws a time arrow and label below the GraphicEvents from the left
         * column to the right column with a little magic and hard-coding to
         * make things pretty
         */
        int timeArrowYCoord = TOP_MARGIN + EVENT_PADDING
                * eventTypesList.size() - 25;
        timeArrow = new Arrow(lX, timeArrowYCoord, rX, timeArrowYCoord,
                paper, 0);
        timeArrow.setStroke("green", HIGHLIGHT_STROKE_WIDTH);
        int timeLabelYCoord = timeArrowYCoord + 25;
        timeLabel = new Label(paper, mX, timeLabelYCoord, fontSize - 5,
                "Time", DEFAULT_FILL);
    }
    
    /**
     * Resizes the InvariantsGraph to fit the input window dimensions,
     * assumes Paper is rendered
     *   
     * @param height
     * @param width
     */
    public void resize(int paperHeight, int paperWidth) {
        int timeArrowBuffer = 50;
        int arrowColumns = EVENT_COLUMNS - 1;
        double labelWidth = paperWidth / (EVENT_COLUMNS + arrowColumns);
        if (labelWidth < MIN_LABEL_WIDTH) {
            labelWidth = MIN_LABEL_WIDTH;
        }
        double arrowWidth = labelWidth;
        
        int rows = eventTypesList.size();
        int rowBuffers = rows - 1;
        int rowBufferHeight = rowBuffers * MIN_VERTICAL_LABEL_BUFFER;
        double labelHeight = (paperHeight  - timeArrowBuffer - rowBufferHeight) / rows;
        if (labelHeight < MIN_LABEL_HEIGHT) {
            labelHeight = MIN_LABEL_HEIGHT;
        }
        
        Event initialEvent = leftEventCol.get("INITIAL");
        int initialFontSize = initialEvent.getFontSize();
        Event finalEvent = getMaxFontSize(labelHeight, labelWidth, leftEventCol);
        int targetFontSize = finalEvent.getFontSize();
        labelWidth = MIN_LABEL_WIDTH > finalEvent.getWidth() ? MIN_LABEL_WIDTH : finalEvent.getWidth() + 30;
        labelHeight = MIN_LABEL_HEIGHT > finalEvent.getHeight() ? MIN_LABEL_HEIGHT : finalEvent.getHeight() + 30;
        finalEvent.setFont(initialFontSize);
        
        paper.setSize((arrowWidth * arrowColumns) + (labelWidth * EVENT_COLUMNS), 
                (labelHeight * rows) + rowBufferHeight + timeArrowBuffer);
        
        
        
        // Maybe the event columns should go in a data structure so we don't
        // have to do things like this
        double leftColX = labelWidth / 2;
        double midColX = labelWidth / 2 + labelWidth + arrowWidth;
        double rightColX = labelWidth / 2 + 2 * (labelWidth + arrowWidth);
        translateAndScaleEvents(leftEventCol, labelHeight, targetFontSize, 
        		leftColX);
        translateAndScaleEvents(midEventCol, labelHeight, targetFontSize,
        		midColX);
        translateAndScaleEvents(rightEventCol, labelHeight, targetFontSize,
                rightColX);
        
        translateAndScaleArrows(apInvs, arrowWidth, 
                labelWidth + ARROW_LABEL_BUFFER);
        translateAndScaleArrows(afbyInvs, arrowWidth,
                2 * labelWidth + ARROW_LABEL_BUFFER + arrowWidth);
        translateAndScaleArrows(nfbyInvs, arrowWidth,
                2 * labelWidth + ARROW_LABEL_BUFFER + arrowWidth);
        
        double timeArrowTargetY = (labelHeight * rows) + rowBufferHeight +  timeArrowBuffer / 2;
        double timeArrowdx = midColX - timeArrow.getCenterX();
        double timeArrowdy = timeArrowTargetY - timeArrow.getCenterY();
        timeArrow.scale(rightColX - leftColX, timeArrow.getHeight());
        timeArrow.translate(timeArrowdx, timeArrowdy);

        double timeLabelTargetX = midColX + 10;
        double timeLabelTargetY = timeArrowTargetY + 10;
        double timeLabeldx = timeLabelTargetX - timeLabel.getCenterX();
        double timeLabeldy = timeLabelTargetY - timeLabel.getCenterY();
        timeLabel.translate(timeLabeldx, timeLabeldy);
        
    }
    
    // This is a bottleneck
    public Event getMaxFontSize(double maxHeight, double maxWidth, Map<String, Event> typeToEvent) {
        final int[] fonts = {20, 30, 40, 50};
        
        String longestType = "";
        
        for (int i = 0; i < eventTypesList.size(); i++) {
            String type = eventTypesList.get(i);
            if (type.length() > longestType.length()) {
                longestType = type;
            }
        }
        
        Event longestEvent = typeToEvent.get(longestType);
        
        int initialFont = longestEvent.getFontSize();
        int currentFont = initialFont;
        
        
        boolean heightGreater = longestEvent.getHeight() > maxHeight;
        boolean widthGreater = longestEvent.getWidth() > maxWidth;
        
        int i = fonts.length - 1;
        while ((heightGreater || widthGreater) && i >= 0) {
            int testFont = fonts[i];
            if (testFont < currentFont) {
                currentFont = testFont;
                longestEvent.setFont(currentFont);
                heightGreater = longestEvent.getHeight() > maxHeight;
                widthGreater = longestEvent.getWidth() > maxWidth;
            }
            i--;
        }
        
        i = 0;
        while (!(heightGreater || widthGreater) && i < fonts.length) {
            int testFont = fonts[i];
            if (testFont > currentFont) {
                int prevFont = currentFont;
                currentFont = testFont;
                longestEvent.setFont(currentFont);
                heightGreater = longestEvent.getHeight() > maxHeight;
                widthGreater = longestEvent.getWidth() > maxWidth;
                if (heightGreater || widthGreater) {
                    currentFont = prevFont;
                    longestEvent.setFont(currentFont);
                }
            }
            i++;
        }
        
        return longestEvent;
    }
    
    /* Maybe this should be in TOInvariant? I feel like I'm breaking
     * abstraction barriers here by digging at the arrow of the inv
     */
    private void translateAndScaleArrows(List<TOInvariant> arrows, 
    		double maxWidth, double targetX) {
        
        for (int i = 0; i < arrows.size(); i++) {
            
            TOInvariant inv = arrows.get(i);
            
            double targetWidth = maxWidth - 2 * ARROW_LABEL_BUFFER;
            double targetHeight = inv.getEventHeightDifference();
            
            inv.scale(targetWidth, targetHeight);
            
            double targetY;
            if (inv.arrowSrcIsTopLeft()) {
                targetY = inv.getSrcY();
            } else {
                targetY = inv.getDstY();
            }
            
            double dx = targetX - inv.getBBoxX();
            double dy = targetY - inv.getBBoxY();
            inv.translate(dx, dy);
            
        }
        
    }
    
    /* Maybe this should be in TOInvariant? I feel like I'm breaking
     * some abstraction barriers here as well
     */
    private void translateAndScaleEvents(Map<String, Event> typeToEvent, 
    		double maxHeight, int fontSize, double targetX) {
        
        for (int i = 0; i < eventTypesList.size(); i++) {
            String eventString = eventTypesList.get(i);
            Event event = typeToEvent.get(eventString);
            
            double targetY = maxHeight / 2 + i *
                    (maxHeight + MIN_VERTICAL_LABEL_BUFFER);
            
            double initialX = event.getCenterX();
            double initialY = event.getCenterY();
            
            double dx = targetX - initialX;
            double dy = targetY - initialY;
            
            event.translate(dx, dy);
            
            event.setFont(fontSize);
                        
        }
        
    }

    /**
     * Takes a list of NCWith invariants and partitions them into sets of
     * invariants that are never concurrent with respect to a base event type
     * 
     * @param ncwithInvs
     * @return
     */
    private List<NCPartition> generateNCPartitions(List<POInvariant> ncwithInvs) {
        List<NCPartition> ncPartitions = new ArrayList<NCPartition>();
        Set<Event> ncEvents = new HashSet<Event>();
        for (POInvariant ncInv : ncwithInvs) {
            ncEvents.add(ncInv.getA());
            ncEvents.add(ncInv.getB());
        }
        for (Event ge : ncEvents) {
            NCPartition ncPart = new NCPartition(ge);
            for (POInvariant ncInv : ncwithInvs) {
                if (ncPart.isInvariantOverBaseEvent(ncInv)) {
                    ncPart.add(ncInv);
                }
            }
            ncPartitions.add(ncPart);
        }
        return ncPartitions;
    }

    /**
     * Takes a list of ACWith invariants and partitions them into sets of
     * invariants that are closed under transitivity.
     * 
     * @param ncwithInvs
     * @return
     */
    private List<ACPartition> generateACPartitions(List<POInvariant> acwithInvs) {
        List<ACPartition> acPartitions = new ArrayList<ACPartition>();
        for (POInvariant acInv : acwithInvs) {
            boolean inserted = false;
            for (ACPartition acPart : acPartitions) {
                if (acPart.isTransitive(acInv)) {
                    acPart.add(acInv);
                    inserted = true;
                }
            }
            if (!inserted) {
                ACPartition singlePart = new ACPartition();
                singlePart.add(acInv);
                acPartitions.add(singlePart);
            }
        }
        return acPartitions;
    }

    /**
     * Draws TO invariants from srcCol to dstCol on paper and links the
     * generated invariants to their corresponding grid labels.
     * 
     * @param invs
     * @param srcCol
     * @param dstCol
     * @param gwtInvToIGridLabel
     * @param labelOffset
     *            Used to offset arrow heads and bases from overlapping event
     *            labels
     * @return
     */
    private List<TOInvariant> drawTOInvariants(List<GWTInvariant> invs,
            Map<String, Event> srcCol, Map<String, Event> dstCol,
            Map<GWTInvariant, InvariantGridLabel> gwtInvToIGridLabel,
            int labelOffset) {
        List<TOInvariant> result = new ArrayList<TOInvariant>();
        for (GWTInvariant inv : invs) {
            String srcEventString = inv.getSource();
            Event srcEvent = srcCol.get(srcEventString);

            String dstEventString = inv.getTarget();
            Event dstEvent = dstCol.get(dstEventString);

            InvariantGridLabel iGridLabel = gwtInvToIGridLabel.get(inv);

            TOInvariant gInv = new TOInvariant(srcEvent, dstEvent, inv, paper,
                    iGridLabel, labelOffset);

            iGridLabel.setGraphicInvariant(gInv);

            srcEvent.addInvariant(gInv);
            dstEvent.addInvariant(gInv);
            result.add(gInv);
        }
        return result;
    }

    /**
     * Draws PO invariants within a column on paper and links the generated
     * invariants to their corresponding grid labels.
     * 
     * @param invs
     * @param col
     * @param gwtInvToIGridLabel
     * @return
     */
    private List<POInvariant> drawPOInvariants(List<GWTInvariant> invs,
            Map<String, Event> col,
            Map<GWTInvariant, InvariantGridLabel> gwtInvToIGridLabel) {
        List<POInvariant> result = new ArrayList<POInvariant>();
        for (GWTInvariant inv : invs) {
            String srcEventString = inv.getSource();
            Event srcEvent = col.get(srcEventString);

            String dstEventString = inv.getTarget();
            Event dstEvent = col.get(dstEventString);

            InvariantGridLabel iGridLabel = gwtInvToIGridLabel.get(inv);

            POInvariant gInv = new POInvariant(srcEvent, dstEvent, inv,
                    iGridLabel);

            iGridLabel.setGraphicInvariant(gInv);

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
     * Returns the Raphael canvas wrapper
     * 
     * @return Raphael canvas wrapper
     */
    public Paper getGraphicPaper() {
        return this.paper;
    }

}
