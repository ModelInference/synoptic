package synopticgwt.client.invariants;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;

import synopticgwt.client.util.JsniUtil;
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

    // Raphael paper object
    JavaScriptObject paper;
    Set<GraphicInvariant> graphicInvariants;

    /**
     * Creates the invariant graphic corresponding to gwtInvs in a DIV with id
     * indicated by invCanvasId.
     */
    public static void createInvariantsGraphic(GWTInvariantSet gwtInvs,
            String invCanvasId) {
        JavaScriptObject eventTypesJS = JavaScriptObject.createArray();
        JavaScriptObject AFbyJS = JavaScriptObject.createArray();
        JavaScriptObject NFbyJS = JavaScriptObject.createArray();
        JavaScriptObject APJS = JavaScriptObject.createArray();

        Set<String> invTypes = gwtInvs.getInvTypes();
        int eTypesCnt = 0;

        Set<String> eventTypes = new LinkedHashSet<String>();
        int longestEType = 0;

        // Iterate through all invariants to create the JS objects for drawing
        // the invariants graphic.
        for (String invType : invTypes) {
            final List<GWTInvariant> invs = gwtInvs.getInvs(invType);

            for (GWTInvariant inv : invs) {
                if (!eventTypes.contains(inv.getSource())) {
                    JsniUtil.pushArray(eventTypesJS, inv.getSource());
                    eventTypes.add(inv.getSource());
                    if (inv.getSource().length() > longestEType) {
                        longestEType = inv.getSource().length();
                    }
                    eTypesCnt++;
                }
                if (!eventTypes.contains(inv.getTarget())) {
                    JsniUtil.pushArray(eventTypesJS, inv.getTarget());
                    eventTypes.add(inv.getTarget());
                    if (inv.getTarget().length() > longestEType) {
                        longestEType = inv.getTarget().length();
                    }
                    eTypesCnt++;
                }

                String x = inv.getSource();
                String y = inv.getTarget();
                if (invType.equals("AFby")) {
                    JsniUtil.addToKeyInArray(AFbyJS, x, y);
                } else if (invType.equals("NFby")) {
                    JsniUtil.addToKeyInArray(NFbyJS, x, y);
                } else if (invType.equals("AP")) {
                    JsniUtil.addToKeyInArray(APJS, x, y);
                }
            }
        }

        // A little magic to size things right.
        // int lX = (longestEType * 30) / 2 - 110;
        int lX = (longestEType * 30) / 2 - 110 + 50;
        // int mX = lX + (longestEType * 30) - 110;
        int mX = lX + (longestEType * 30) - 110 + 50;
        // int rX = mX + (longestEType * 30) - 110;
        int rX = mX + (longestEType * 30) - 110 + 50;
        int width = rX + 200;

        int fontSize = 20; // getFontSize(longestEType);

        // Pass the created JavaScript structures to the native call that will
        // create the graphic.
        InvariantsGraph.createInvariantsGraphic(AFbyJS, NFbyJS, APJS,
                eventTypesJS, width, (eTypesCnt + 1) * 50, lX, mX, rX,
                fontSize, invCanvasId);
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

    // //////////////////////////////////////////////////////////////////////////
    // JSNI methods -- JavaScript Native Interface methods. The method body of
    // these calls is pure JavaScript.

    /**
     * A JSNI method to create and display an invariants graphic.
     * 
     * @param AFby
     *            associative array with AFby relations
     * @param NFby
     *            associative array with NFby relations
     * @param AP
     *            associative array with AP relations
     * @param eTypes
     *            array of all event types
     * @param width
     *            width of graphic
     * @param height
     *            height of graphic
     * @param lX
     *            the x value of the left most column
     * @param mX
     *            the x value of the middle column
     * @param rX
     *            the x value of the right most column
     * @param canvasId
     *            the div id where to draw the graphic
     */
    private static native void createInvariantsGraphic(JavaScriptObject AFby,
            JavaScriptObject NFby, JavaScriptObject AP,
            JavaScriptObject eTypes, int width, int height, int lX, int mX,
            int rX, int fontSize, String canvasId) /*-{

		var paper = $wnd.Raphael($doc.getElementById(canvasId), width, height);


		var topMargin = 20;
		var dY = 50;

		var lines = new Array();

		// These will contain text labels in the middle/right/left columns:
		var tMiddlesArr = [];
		var tRightsArr = [];
		var tLeftsArr = [];

		var ypos = new Array();

		// Create the three columns of text labels.
		for ( var i = 0; i < eTypes.length; i++) {
			var eType = eTypes[i]

			var tMiddle = paper.text(mX, dY * i + topMargin, eType);
			tMiddlesArr.push(tMiddle);
			tMiddle.attr({
				'font-size' : fontSize + "px",
				fill : "grey"
			});

			// Remember the y position of every row of labels.
			ypos[eType] = dY * i + 10;

			// Do not create the INITIAL labels on the left/right
			if (eType == "INITIAL") {
				continue;
			}

			var tLeft = paper.text(lX, dY * i + topMargin, eType);
			tLeft.attr({
				'font-size' : fontSize + "px",
				fill : "grey"
			});
			tLeftsArr[eType] = tLeft;

			var tRight = paper.text(rX, dY * i + topMargin, eType);
			tRight.attr({
				'font-size' : fontSize + "px",
				fill : "grey"
			});
			tRightsArr[eType] = tRight;
		}

		// Create all the lines by iterating through labels in the middle column.
		for ( var i = 0; i < eTypes.length; i++) {
			var eType = eTypes[i]
			lines[eType] = []
		}

		for ( var i = 0; i < eTypes.length; i++) {
			var eType = eTypes[i]
			var tMiddle = tMiddlesArr[i];

			// AP:
			for ( var j in AP[eType]) {
				var line = paper.path(("M" + mX + " " + ypos[AP[eType][j]]
						+ "L" + lX + " " + ypos[eType]));
				line.attr({
					stroke : "grey",
					highlight : "blue",
					dest : tLeftsArr[eType]
				});
				// NOTE: we associate the middle label destination of the arrow, not the left label source.
				lines[AP[eType][j]].push(line);
			}

			// AFby:
			for ( var j in AFby[eType]) {
				var line = paper.path(("M" + mX + " " + ypos[eType] + "L" + rX
						+ " " + ypos[AFby[eType][j]]));
				line.attr({
					stroke : "grey",
					highlight : "blue",
					dest : tRightsArr[AFby[eType][j]]
				});
				lines[eType].push(line);
			}

			// NFby:
			for ( var j in NFby[eType]) {
				var line = paper.path(("M" + mX + " " + ypos[eType] + "L" + rX
						+ " " + ypos[NFby[eType][j]]));
				line.attr({
					stroke : "grey",
					highlight : "red",
					dest : tRightsArr[NFby[eType][j]]
				});
				lines[eType].push(line);
			}

			// Function to execute when the tMiddle label is pointed-to.
			tMiddle.mouseover(function(y) {
				return function(e) {
					// y is tMiddle
					for ( var line in lines[y.attr('text')]) {
						lines[y.attr('text')][line].attr({
							'stroke-width' : '3'
						});
						lines[y.attr('text')][line].attr({
							stroke : lines[y.attr('text')][line]
									.attr('highlight')
						});
						lines[y.attr('text')][line].attr('dest').attr({
							fill : "black"
						});
					}
					y.attr({
						fill : "black"
					});

				};
			}(tMiddle));

			// Function to execute when the tMiddle label is not pointed-to.
			tMiddle.mouseout(function(y) {
				return function(e) {
					for ( var line in lines[y.attr('text')]) {
						lines[y.attr('text')][line].attr({
							'stroke-width' : '1'
						});
						lines[y.attr('text')][line].attr({
							stroke : "grey"
						});
						lines[y.attr('text')][line].attr('dest').attr({
							fill : "grey"
						});
					}
					y.attr({
						fill : "grey"
					});
				};
			}(tMiddle));
		}
    }-*/;

    // </JSNI methods>
    // //////////////////////////////////////////////////////////////////////////

    public Set<GraphicInvariant> getGraphicInvariants() {
        return new HashSet<GraphicInvariant>(graphicInvariants);
    }

    public void showElement(JavaScriptObject element) {
    }
    
    public void hideElement(JavaScriptObject element) {
    }
}
