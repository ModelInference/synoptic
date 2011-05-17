var CUSTOM = {
		"currentNodes" : [],
		"initializeStableIDs"  : function  (nodes, edges, renderer, g)  {
		    	for (var i = 0; i < nodes.length; i+= 2) {
		    		this.currentNodes[nodes[i]] = nodes[i+1];
		    	}
		    	this.graph = g;
		    	this.rend = renderer;
			},
		"getRenderer" : function () {
				return this.rend;
			},
		"getGraph" : function () {
				return this.graph;
			},
		"render" : function(r, n) {
				// the Raphael set is obligatory, containing all you want to display
				var set = r.set().push(
				// custom objects go here
				r.rect(n.point[0] - 30, n.point[1] - 13, 62, 86).attr({
					"fill" : "#fa8",
					"stroke-width" : 2,
					r : "9px"
				})).push(r.text(n.point[0], n.point[1] + 30, n.label).attr({
					"font-size" : "12px"
				}));
				return set;
			},
		"updateGraph" : function(nodes, edges, splitNodeID) {	
			var refinedNode = this.graph.nodes[splitNodeID];
			this.graph.removeNode(splitNodeID);
			delete this.currentNodes[splitNodeID];
			
			var newNodes = [];
			for ( var i = 0; i < nodes.length; i += 2) {
				if (!this.currentNodes[nodes[i]]) {
					this.currentNodes[nodes[i]] = nodes[i+1];
					newNodes[nodes[i]] = true;
					var node = this.graph.addNode(nodes[i], {
						label : nodes[i + 1],
						render : this.render,
						layoutPosX : refinedNode.layoutPosX,
		        		layoutPosY : refinedNode.layoutPosY
					});
				}
			}
			this.rend.draw();
			for ( var i = 0; i < edges.length; i += 2) {
				var source = edges[i];
				var dest = edges[i+1];
				if (newNodes[source] || newNodes[dest]) {
					this.graph.addEdge(source, dest);
				}
			}
		}
};