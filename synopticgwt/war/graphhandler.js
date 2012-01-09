/*
 * Stores a graph, its layouter, and its renderer for manipulation of the graph's
 * display.
 */

// An assocative array of event node IDs mapped to raphael rectangle objects.
var draculaNodeMap = {};

// A function for clearing the "selected" state of the
// dracula nodes in the GRAPH_HANDLER object. The graph
// handler object must have had the initializeStableIDs
// function run at least once before calling this function
// subsequently.
var clearSelectedNodes = function() {
    // Grab the nodes from the main dracula graph.
    var nodes = GRAPH_HANDLER.getGraph().nodes;

    // Deselect all of the nodes.
    for ( var i in nodes) {
        var n = nodes[i];
        // If the node is selected,
        // deselect it.
        if (n.selected) {
            n.toggleSelected();
        }
    }
}

var GRAPH_HANDLER = {
    // array of graph nodes
    "currentNodes" : [],

    // initializes this GRAPH_HANDLER
    "initializeStableIDs" : function(nodes, edges, renderer, layouter, g) {
        for ( var i = 0; i < nodes.length; i += 2) {
            this.currentNodes[nodes[i]] = nodes[i + 1];
        }
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
        if (node.label == "INITIAL" || node.label == "TERMINAL") {
            // creates the rectangle to be drawn
            var rect = canvas.rect(node.point[0] - 30, node.point[1] - 13, 122,
                    46).attr({
                "fill" : "#808080",
                "stroke-width" : 2,
                r : "40px"
            });
        } else {
            // creates the rectangle to be drawn
            var rect = canvas.rect(node.point[0] - 30, node.point[1] - 13, 122,
                    46).attr({
                "fill" : "#fa8",
                "stroke-width" : 2,
                r : "9px"
            });
        }

        if (draculaNodeMap[node.id] == undefined) {
            draculaNodeMap[node.id] = rect;
        }

        // Toggles whether the node has been selected.
        // more details on what qualifies as "selected"
        // in the next function (defined for mouse click events).
        // TODO: Add a reference to the selected node to ModelTab.java
        // or ModelGraphic.java
        node.toggleSelected = function() {
            // Add the "selected" field to the object
            // if it doesn't already exist (initialized
            // as false so as to make the rest of the function
            // work properly).
            if (this.selected == null) {
                this.selected = false;
            }

            // Toggle selection of the node
            this.selected = !this.selected;

            // Fill the rectangle with the designated color.
            // If selected, turn the node blue, else change back
            // to default color.
            var rectangle = draculaNodeMap[this.id];
            if (this.selected) {
                rectangle.attr("fill", "blue");
            } else {
                rectangle.attr("fill", "#fa8");
            }
        };

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
            if (node.label != "INITIAL" && node.label != "TERMINAL") {
                // TODO: When selecting a node to view log lines that has
                // already
                // been selected (and the log lines are currently in view),
                // don't bother making another RPC (since it's unnecessary).
                if (!event.shiftKey) {
                    clearSelectedNodes();
                    viewLogLines(parseInt(node.id));
                }

                node.toggleSelected();
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
        return set;
    },

    // updates the graph by removing the node with the splitNodeID and adding
    // (plus drawing)
    // all newly refined nodes at the position of the removed node. returns an
    // array of the
    // new nodes
    "updateRefinedGraph" : function(nodes, edges, splitNodeID) {
        // fetch the refined node
        var refinedNode = this.graph.nodes[splitNodeID];

        // remove the refined node and all its edges from the graph
        this.graph.removeNode(splitNodeID);
        delete this.currentNodes[splitNodeID];
        delete draculaNodeMap[splitNodeID];

        // tracks which new nodes are added to update edges below
        var newNodes = [];

        // loop over all given nodes, find and add new nodes to the graph
        for ( var i = 0; i < nodes.length; i += 2) {
            if (!this.currentNodes[nodes[i]]) {
                this.currentNodes[nodes[i]] = nodes[i + 1];
                newNodes[nodes[i]] = true;
                this.graph.addNode(nodes[i], {
                    label : nodes[i + 1],
                    render : this.render,
                    layoutPosX : refinedNode.layoutPosX,
                    layoutPosY : refinedNode.layoutPosY
                });
            }
        }

        // re-draw the graph, adding new nodes to the canvas
        this.rend.draw();

        // loop over all given edges, finding ones connected to the new
        // nodes that need to be added to the graph
        for ( var i = 0; i < edges.length; i += 3) {
            var source = edges[i];
            var dest = edges[i + 1];
            var weight = edges[i + 2];
            if (newNodes[source] || newNodes[dest]) {
                this.graph.addEdge(source, dest, {
                    label : weight
                });
            }
        }

        // return the set of new nodes
        return newNodes;
    }
};