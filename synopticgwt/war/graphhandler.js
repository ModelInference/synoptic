/*
 * Stores a graph, its layouter, and its renderer for manipulation of the graph's
 * display.
 */

// The model node that is currently selected (has a different  color).
// This node either has been single clicked, OR double clicked last.
var currentSelectedRect = ""
var currentSelectedNode = ""

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

        // Add an onclick event to the rectangle to have it change color to
        // "blue".
        // Also, whenever a node is made blue, the node that was clicked on
        // previously
        // is turned back to its original color. We keep track of previously
        // clicked
        // node with the currentSelectedRect var.
        rect.node.onclick = function() {
            if (currentSelectedRect != "") {
                if (currentSelectedNode.label == "INITIAL"
                        || currentSelectedNode.label == "TERMINAL") {
                    currentSelectedRect.attr("fill", "#808080");
                } else {
                    currentSelectedRect.attr("fill", "#fa8");
                }

            }
            currentSelectedRect = rect
            currentSelectedNode = node
            viewLogLines(parseInt(node.id));
            rect.attr("fill", "blue");
        };

        text = canvas.text(node.point[0] + 30, node.point[1] + 10, node.label)
                .attr({
                    "font-size" : "16px",
                });

        // the Raphael set is obligatory, containing all you want to display
        // draws this node's label
        var set = canvas.set().push(rect).push(text);

        // The text, when clicked should behave as if the rectangle was clicked.
        text.node.onclick = rect.node.onclick;
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