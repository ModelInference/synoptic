package synopticgwt.client.model;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

import synopticgwt.client.util.Paper;

/**
 * A wrapper class for a JavaScript node implemented with the Dracula Graph
 * library.
 * 
 */
public class DraculaNodeRenderer implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Returns an anonymous function that takes two parameters,
     * and is capable of rendering a node, given that the anonymous
     * function returned is given a Raphael canvas and a Dracula JS node
     * (not a GWT node like this one).
     * 
     * @return
     */
    private static native JavaScriptObject getRendererFunction() /*-{
		return function(canvas, node) {
			var rect;
			if (node.label == "INITIAL" || node.label == "TERMINAL") {
				// creates the rectangle to be drawn
				var rect = canvas.rect(node.point[0] - 30, node.point[1] - 13,
						122, 46).attr({
					"fill" : "#808080",
					"stroke-width" : 2,
					r : "40px"
				});
			} else {
				// creates the rectangle to be drawn
				var rect = canvas.rect(node.point[0] - 30, node.point[1] - 13,
						122, 46).attr({
					"fill" : "#fa8",
					"stroke-width" : 2,
					r : "9px"
				});
			}

			// Add an onclick event to the rectangle to have it change color to
			// "blue".
			// Also, whenever a node is made blue, the node that was clicked on
			// previously
			// is turned back to its original color. We keep track of previously
			// clicked
			// node with the currentSelectedRect var.
			rect.node.onmouseup = function() {
				if (currentSelectedRect != "") {
					if (currentSelectedNode.label == "INITIAL"
							|| currentSelectedNode.label == "TERMINAL") {
						currentSelectedRect.attr("fill", "#808080");
					} else {
						currentSelectedRect.attr("fill", "#fa8");
					}

				}
				currentSelectedRect = rect;
				currentSelectedNode = node;
				
				////////////////////////////
				// TODO: Chop this off!!!
				// Needs to make a call to a modelTab instance somewhere else.
				//
				viewLogLines(parseInt(node.id));
				//
				//
				///////////////////////////
				
				rect.attr("fill", "blue");
			};

			text = canvas.text(node.point[0] + 30, node.point[1] + 10,
					node.label).attr({
				"font-size" : "16px",
			});

			// the Raphael set is obligatory, containing all you want to display
			// draws this node's label
			var set = canvas.set().push(rect).push(text);

			// The text, when clicked should behave as if the rectangle was clicked.
			text.node.onmouseup = rect.node.onmouseup;
			return set;
		}
    }-*/;
}
