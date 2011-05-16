var CUSTOM = {
		// mapping from stable id # to label
		"stableIDs" : [],
		"labelToIDs" : [],
		"initializeStableIDs"  : function  (nodes, edges, renderer, g)  {
		    	for (var i = 0; i < nodes.length; i+= 2) {
		    		this.stableIDs[nodes[i]] = nodes[i+1];
		    		this.labelToIDs[nodes[i+1]] = [];
		    		this.labelToIDs[nodes[i+1]].push(nodes[i]);
		    	}
		    	this.nextID = this.stableIDs.length;
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
		"updateGraph"  : function (allNodes, allEdges, newNodes, newEdges) {
				var tempToStable = this.assignStableIDs(allNodes, allEdges, newNodes);

				var splitNodeID = tempToStable[newNodes[0]];
				
				var removeOldID = function (array, oldID) {
					for (var i = 0; i < array.length; i++) {
						if (array[i] === oldID) {
							array.splice(i, 1);
							break;
						}
					}
				}
				removeOldID(this.labelToIDs[newNodes[1]], splitNodeID);
				
				var refinedNode = this.graph.nodes[splitNodeID];
				for ( var i = 0; i < newNodes.length; i += 2) {
					tempToStable[newNodes[i]] = this.nextID;
					this.stableIDs[this.nextID] = newNodes[i+1];
					this.labelToIDs[newNodes[i+1]].push(this.nextID);
					var node = this.graph.addNode(this.nextID, {
						label : newNodes[i + 1],
						render : this.render,
						layoutPosX : refinedNode.layoutPosX,
			        	layoutPosY : refinedNode.layoutPosY
					});
					this.nextID++;
				}
			    this.rend.draw();
				for ( var i = 0; i < newEdges.length; i += 2) {
					this.graph.addEdge(tempToStable[newEdges[i]], tempToStable[newEdges[i+1]]);
				}
				this.graph.removeNode(splitNodeID);
				delete this.stableIDs[splitNodeID];
			},
		"assignStableIDs" : function (allNodes, allEdges, newNodes) {
				var tempIDtoStableID = [];
				
				var tempIDtoInfo = [];
				for (var i = 0; i < allNodes.length; i+=2) {
					tempIDtoInfo[allNodes[i]] = {
						"label" : allNodes[i+1],
						"destinations" : [],
						"sources" : []
					};
				}
				
				for (var i = 0; i < allEdges.length; i+=2) {
					var source = allEdges[i];
					var dest = allEdges[i+1];
					tempIDtoInfo[source].destinations.push(tempIDtoInfo[dest].label);
					tempIDtoInfo[dest].sources.push(tempIDtoInfo[source].label);
				}
				
				var duplicatesUsed = [];
				
				var contains = function (array, value) {
					for (var i = 0; i < array.length; i++) {
						if (array[i] === value) {
							return true;
						}
					}
					return false;
				};
				
				var lookup = function (candidateStableID, tempObj, graph, stableIDs) {
					// check all edges of the candidate, confirm that the temp contains the same source and targets
					for (var j = 0; j < graph.nodes[candidateStableID].edges.length; j++) {
						var edge = graph.nodes[candidateStableID].edges[j];
						if (edge.source.id === candidateStableID) { // candidate node is source
							if (!contains(tempObj.destinations, stableIDs[edge.target.id])){
								return false;
							}
						} else { // candidate node is destination, must find label in src
							if (!contains(tempObj.sources, stableIDs[edge.source.id])) {
								return false;
							}
						}
					}
					return true;
				};
				
				for (var i = 0; i < allNodes.length; i+=2) {
					var assigned = false;
					if (this.labelToIDs[allNodes[i+1]].length === 1) {
						tempIDtoStableID[allNodes[i]] = this.labelToIDs[allNodes[i+1]][0];
						assigned = true;
					} else {
						for (var j = 0; j < this.labelToIDs[allNodes[i+1]].length; j++) {
							var possibleID = this.labelToIDs[allNodes[i+1]][j];
							if (duplicatesUsed[possibleID]) {
								continue;
							} else {
								var currentInfo = tempIDtoInfo[allNodes[i]];
								if (lookup(possibleID, currentInfo, this.graph, this.stableIDs)) {
									duplicatesUsed[possibleID] = true;
									assigned = true;
									tempIDtoStableID[allNodes[i]] = possibleID;
									break;
								}
							}
						}
					}
				}
				
				return tempIDtoStableID;
			}
};