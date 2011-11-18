package synopticgwt.client.invariants;

import java.io.Serializable;

import synopticgwt.client.util.Paper;

/** Java wrapper for an arrow on a Raphael canvas */
public class GraphicArrow implements Serializable {

	private static final long serialVersionUID = 1L;
    /** Length of the positive and negative arrowhead segments */
    public static final int HEAD_LENGTH = 10;
    /** Default target buffer */
    public static final int TARGET_BUFFER = 10;
	
    // Non-arrowhead part of the arrow
    private Path body;
    // Part of the arrowhead that has a positive angular offset from the body
    private Path positiveHead;
    // Part of the arrowhead that has a negative angular offset from the body
    private Path negativeHead;
	private Paper paper;

    /** 
     * Draws an arrow from (x1, y1) to (x2, y2) on paper 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param paper raphael canvas
     * @param targetBuffer Distance between terminal end of the arrow  and (x2, y2)
     */

    public GraphicArrow(int x1, int y1, int x2, int y2, Paper paper, int targetBuffer) {
        this.paper = paper;
        constructArrow(x1, y1, x2 - targetBuffer, y2 - targetBuffer);
        setStroke(InvariantsGraph.DEFAULT_STROKE, 
            InvariantsGraph.DEFAULT_STROKE_WIDTH);
    }

    /** Draws and arrow from (x1, y1) to (x2, y2) */
    public void constructArrow(int x1, int y1, int x2, int y2) {
        /* 
         * I conceptually set (x2, y2) to (0, 0) and compute the relevant 
         * x1 and y1 values
         * */
        double xRelativeZero = x1 - x2;
        double yRelativeZero = y1 - y2;
        
        /* 
         * In cartesian coordinates, I am computing the angle that the line 
         * from (xRelativeZero, yRelativeZero) to the origin makes with 
         * some axis.
         *
         * In polar coordinates, I am computing theta given
         * x = xRelativeZero and y = yRelativeZero
         * */
        double theta = Math.atan2(yRelativeZero, xRelativeZero);
        double positiveTheta = theta + Math.PI / 4;
        double negativeTheta = theta - Math.PI / 4;

        /*
         * This computes the coodrinates for the part of the arrowhead that
         * is at a positive angular axis from the arrowbody 
         * */ 
        double relativePositiveHeadX = HEAD_LENGTH * Math.cos(positiveTheta);
        double relativePositiveHeadY = HEAD_LENGTH * Math.sin(positiveTheta);

        /*
         * This computes the coodrinates for the part of the arrowhead that
         * is at a negative angular axis from the arrowbody 
         * */ 
        double relativeNegativeHeadX = HEAD_LENGTH * Math.cos(negativeTheta);
        double relativeNegativeHeadY = HEAD_LENGTH * Math.sin(negativeTheta);

        /*
         * Shift relative positive arrowhead coordinates back to absolute
         * */  
        double positiveHeadX = relativePositiveHeadX + x2;
        double positiveHeadY = relativePositiveHeadY + y2;

        /*
         * Shift relative negative arrowhead coordinates back to absolute
         * */  
        double negativeHeadX = relativeNegativeHeadX + x2;
        double negativeHeadY = relativeNegativeHeadY + y2;

        this.body = new Path(x1, y1, x2, y2, paper);
        this.positiveHead = new Path(x2, y2, positiveHeadX, positiveHeadY, paper);
        this.negativeHead = new Path(x2, y2, negativeHeadX, negativeHeadY, paper);
    }
        

    /** Makes the arrow visible on paper */
    public void show() {
        body.show();
        positiveHead.show();
        negativeHead.show();
    }

    /** Makes the arrow invisible on paper */
    public void hide() {
        body.hide();
        positiveHead.hide();
        negativeHead.hide();
    }

    /** Changes the arrow's color and stroke width to color and width */
    public void setStroke(String color, int width) {
        body.setStroke(color, width);
        positiveHead.setStroke(color, width);
        negativeHead.setStroke(color, width);
    }
}
