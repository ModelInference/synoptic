/*
 * Stores a graph, its layouter, and its renderer for manipulation of the graph's
 * display.
 */

// Default color for nodes.
var DEFAULT_COLOR = "#fa8";

// Default stroke for border of node.
var DEFAULT_STROKE_WIDTH = 2;

// Default color for initial and terminal nodes.
var INIT_TERM_COLOR = "#808080";

// Color used when highlighting a node.
var HIGHLIGHT_COLOR = "blue";

// Border color for shift+click nodes after "View paths" clicked.
// NOTE: Must also change same constant in ModelTab.java if modified.
var SHIFT_CLICK_BORDER_COLOR = "blue";

// Stroke width for border when node selected.
var SELECT_STROKE_WIDTH = 4;

// Label name that indicates initial node.
var INITIAL = "INITIAL";

// Label name that indicates terminal node.
var TERMINAL = "TERMINAL";

// An assocative array of event node IDs mapped to raphael rectangle objects.
var selectedDraculaNodes = {};

// An array containing all rectangles objects.
var allRects = [];

// The selected node that has log lines displayed.
var selectedNodeLog;

/*
 * A function for clearing the state of the selected nodes.
 * Each node is set back to the default color, border color,
 * and stroke width, and then removed from the set of 
 * selected nodes.
 */
var clearSelectedNodes = function() {
    for (var i in selectedDraculaNodes) {
        selectedDraculaNodes[i].attr({
        	"fill": DEFAULT_COLOR,
        	"stroke": "black",
			"stroke-width": DEFAULT_STROKE_WIDTH
        });
        removeSelectedNode(parseInt(i));
        delete selectedDraculaNodes[i];
    }
}

/*
 * A function for setting the border of all selected
 * nodes to given color. Changes the background color
 * of the nodes to the default color. Default styling
 * to node displaying log lines if not in shift+click
 * set.
 */
var setShiftClickNodesState = function(color) {
	// Whether or not the node displaying log line is in
	// shift+click set.
	var clickNodeInSet = false;
	for (var i in selectedDraculaNodes) {
		if (selectedNodeLog == selectedDraculaNodes[i]) {
			clickNodeInSet = true;
		}
        selectedDraculaNodes[i].attr({
        	"fill": DEFAULT_COLOR,
        	"stroke": color,
			"stroke-width": SELECT_STROKE_WIDTH
        });
    }
	// Set node to default styling.
	if (!clickNodeInSet) {
		selectedNodeLog.attr({
			"stroke": "black",
			"stroke-width": DEFAULT_STROKE_WIDTH
		});
	}
	selectedNodeLog = undefined;
}

/*
 * A function that returns true if the rectangle object
 * being passed is currently selected. Returns false if
 * rectangle object is not selected.
 */
var isSelectedNode = function(rect) {
	for (var i in selectedDraculaNodes) {
		if (selectedDraculaNodes[i] == rect) {
			return true;
		}
	}
	return false;
}

var GRAPH_HANDLER = {
    // Array of graph nodes.
    "currentNodes" : [],
    
    // Array of graph edges.
    "currentEdges" : [],

    // initializes this GRAPH_HANDLER
    "initializeStableIDs" : function(renderer, layouter, g) {
        this.graph = g;
        this.rend = renderer;
        this.layouter = layouter;
    },

    // returns this graph's renderer
    "getRenderer" : function() {
        return this.rend;
    },

    // Returns all of the current nodes.
    "getCurrentNodes" : function() {
        return this.currentNodes;
    },
    
    // Returns all of the current edges.
    "getCurrentEdges" : function() {
        return this.currentEdges;
    },

    // returns this graph's layouter
    "getLayouter" : function() {
        return this.layouter;
    },

    // returns this graph
    "getGraph" : function() {
        return this.graph;
    },

    // provides instructions for how to render a node. accepts the canvas to be
    // drawn on
    // and the node to draw. returns the set of drawn shapes for the node
    // (rectangle and
    // label)
    "render" : function(canvas, node) {
        var rect;
        if (node.label == INITIAL || node.label == TERMINAL) {
            // creates the rectangle to be drawn
            var rect = canvas.rect(node.point[0] - 30, node.point[1] - 13, 122,
                    46).attr({
                "fill" : INIT_TERM_COLOR,
                "stroke-width" : DEFAULT_STROKE_WIDTH,
                r : "40px"
            });
        } else {
            // creates the rectangle to be drawn
            var rect = canvas.rect(node.point[0] - 30, node.point[1] - 13, 122,
                    46).attr({
                "fill" : DEFAULT_COLOR,
                "stroke-width" : DEFAULT_STROKE_WIDTH,
                r : "9px"
            });
            // associate label with rectangle object
            rect.label = node.label;
            allRects[allRects.length] = rect;
        }

        // Adds a function to the given rectangle so that, when clicked,
        // the associated event node is "selected" (shown as blue when clicked)
        // and then the log lines associated with the event are shown in the
        // the model tab (grabbed via a RPC).
        //
        // When clicking the same node again, the node stays selected. When
        // clicking
        // a different node, the previous node is deselected, and the new node
        // is
        // selected.
        //
        // The function will also detect shift events, and toggle
        // more than one node if the shift key is being
        // held down. If the node has been clicked without
        // the shift key being held down all nodes except for the node clicked
        // will be deselected. Holding shift and clicking a selected node
        // will deselect it.
        rect.node.onmouseup = function(event) {
            if (node.label != INITIAL && node.label != TERMINAL) {
            	
                if (!event.shiftKey && selectedNodeLog != rect) {
                    clearSelectedNodes();
                    viewLogLines(parseInt(node.id));
                }
                
                if (selectedDraculaNodes[node.id] == undefined) {
                	// Node associated with log lines listed is
                	// surrounded by red and thick border.
                	if (event.shiftKey) {
                    	rect.attr("fill", HIGHLIGHT_COLOR);
                    	selectedDraculaNodes[node.id] = rect;
                        addSelectedNode(parseInt(node.id));
                	} else {
                		// Remove red border from previous node displaying log lines.
                		if (selectedNodeLog != undefined) {
                			selectedNodeLog.attr({
                				"stroke": "black",
                				"stroke-width": DEFAULT_STROKE_WIDTH
                			});
                		}
                		selectedNodeLog = rect;
	                	rect.attr({
	                		"fill": DEFAULT_COLOR,
	                		"stroke": "red",
	                		"stroke-width": SELECT_STROKE_WIDTH
	                	});
                	}
                	
                } else {
                	if (selectedNodeLog == rect) {
                		rect.attr({
	                    	"fill": DEFAULT_COLOR
	                    });
                	} else { // All nodes except for one displaying log lines.
                		rect.attr({
	                    	"fill": DEFAULT_COLOR,
	                    	"stroke": "black",
	            			"stroke-width": DEFAULT_STROKE_WIDTH
	                    });
                	}
                	delete selectedDraculaNodes[node.id];
                	removeSelectedNode(parseInt(node.id));
                }
            }
        };
        
        // On a mouse hover, highlight that node and other nodes
        // that are of the same type.
        rect.node.onmouseover = function(event) {
        	if (node.label != INITIAL && node.label != TERMINAL) {
        		for (var i = 0; i < allRects.length; i++) {
        			var currRect = allRects[i];
        			if (currRect.label == node.label) {
        				currRect.attr("fill", HIGHLIGHT_COLOR);
        			}
        		}
        	}
        };
        
        // On a mouse hovering out, un-highlight that node and 
        // other nodes that are of the same type.
        rect.node.onmouseout = function(event) {
        	if (node.label != INITIAL && node.label != TERMINAL) {
        		for (var i = 0; i < allRects.length; i++) {
        			var currRect = allRects[i];
        			// Return to default color if the rectangle is
        			// not currently selected. Highlight if node has
        			// colored border after "View paths".
        			if (!isSelectedNode(currRect) || 
        					currRect.attr("stroke") == SHIFT_CLICK_BORDER_COLOR) {
        				currRect.attr("fill", DEFAULT_COLOR);
        			}
        		}
        	}
        };
        
        text = canvas.text(node.point[0] + 30, node.point[1] + 10, node.label)
                .attr({
                    "font-size" : "16px",
                });

        // the Raphael set is obligatory, containing all you want to display
        // draws this node's label
        var set = canvas.set().push(rect).push(text);

        // The text, when clicked should behave as if the rectangle was clicked.
        text.node.onmouseup = rect.node.onmouseup;
        
        // The text, when hovering over and hovering out should behave the same
        // as the rectangle.
        text.node.onmouseout = rect.node.onmouseout;
        text.node.onmouseover = rect.node.onmouseover;
        return set;
        
    },
    
};