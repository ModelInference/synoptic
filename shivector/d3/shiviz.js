/**
 * Node class
 */
function Node(log, hostId, clock) {
  this.log = log;
  this.hostId = hostId;
  this.clock = clock;
  this.time = clock[hostId];
  this.index = -1;
}

Node.prototype.id = function() {
  return this.hostId + ":" + this.time;
}

Node.prototype.getClock = function() {
  return this.clock;
}

Node.prototype.getHostId = function() {
  return this.hostId;
}

Node.prototype.getTime = function() {
  return this.time;
}

Node.prototype.getLog = function() {
  return this.log;
}

Node.prototype.setIndex = function(index) {
  this.index = index;
}

Node.prototype.getIndex = function() {
  return this.index;
}

/**
 * Edge class
 */
function Edge(src, dest) {
  this.src = src;
  this.dest = dest;
}

Edge.prototype.getSrc = function() {
  return this.src;
}

Edge.prototype.getDest = function() {
  return this.dest;
}

function Edges() {
  this.edges = [];
}

Edges.prototype.add = function(edge) {
  this.edges.push(edge);
}

function isHidden(node) {

}

Edges.prototype.toLiteral = function(hiddenHosts) {
  var literal = [];
  for (var i = 0; i < this.edges.length; i++) {
    // TODO: fix this to handle transitive causation later
    if (hiddenHosts.indexOf(this.edges[i].getSrc().getHostId()) >= 0 ||
          hiddenHosts.indexOf(this.edges[i].getDest().getHostId()) >= 0) {
      continue;
    }
    var edge = {};
    edge["source"] = this.edges[i].getSrc().getIndex();
    edge["target"] = this.edges[i].getDest().getIndex();
    literal.push(edge);
  }
  return literal;
}

/**
 * Graph class
 */
function Graph() {
  this.nodes = new Nodes();
  this.edges = new Edges();
  this.hasEdges = false;
}

Graph.prototype.parseLog = function(logLines) {
  if (this.hasEdges) {
    console.log("error, cannot parse log, edges already generated");
    return;
  }

  // assumes log lines come in pairs of log, timestamp
  // and that timestamp format is hostId {hostId_1:time_1, ... ,
  // hostId_n:time_n}

  try {
    for (var i = 0; i < logLines.length; i+=2) {
      var log = logLines[i];
      if (log.length == 0) {
        break;
      }
      var stamp = logLines[i+1];
      var spacer = stamp.indexOf(" ");
      var host = stamp.substring(0, spacer);
      var clock = JSON.parse(stamp.substring(spacer));
      
      var index = log.indexOf("INFO");
      if (index == -1) {
        index = log.indexOf("WARN");
      }
      var displayLog = log.substring(index + 4);
      this.nodes.add(new Node(displayLog, host, clock));
    }
  }catch (err) {
    alert("Error parsing input, malformed logs");
    clearText();
    return false;
  }
  return true;
}

Graph.prototype.toLiteral = function(hiddenHosts) {
  if (!this.hasEdges) {
    console.log("error, cannot return literal -- edges not generated"); 
    return {}; 
  }
  hiddenHosts = hiddenHosts || [];

  var literal = {};
  literal["nodes"] = this.nodes.toLiteral(hiddenHosts);
  literal["links"] = this.edges.toLiteral(hiddenHosts);
 
  var sortedHosts = this.nodes.getSortedHosts();
  for (var i = 0; i < hiddenHosts.length; i++) {
    sortedHosts.splice(sortedHosts.indexOf(hiddenHosts[i]), 1);
  }
  literal["hosts"] = sortedHosts; 
  return literal;
}

Graph.prototype.generateEdges = function() {
  if (this.hasEdges) {
    console.log("error, edges should not be generated twice");
    return;
  }
  this.hasEdges = true;

  var hosts = this.nodes.getHosts();
  for (var i = 0; i < hosts.length; i++) {
    var host = hosts[i];
    var name = "Host: " + host.substring(host.indexOf("[") + 1, host.indexOf("]"));
    var startClock = {};
    startClock[host] = 0;
    var startNode = new Node(name, host, startClock);
    this.nodes.add(startNode);
  }

  for (var i = 0; i < hosts.length; i++) {
    var host = hosts[i];
    var clock = {};
    var curNode = this.nodes.get(host, 0);
    var prevNode = null;
    while (curNode != null) {
      if (prevNode != null) {
        // curNode has a parent on this host
        this.edges.add(new Edge(prevNode, curNode));
      }
      clock[host] = curNode.getTime();
      var candidates = [];
      var curClock = curNode.getClock();
      for (var otherHost in curClock) {
        var time = curClock[otherHost];
        if (!clock.hasOwnProperty(otherHost) || clock[otherHost] < time) {
          // This otherHost may be a parent
          clock[otherHost] = time;
          var candidate = this.nodes.get(otherHost, time);
          candidates.push(candidate);
        }
      }

      // Determine which of candidates are 'necessary'
      var sourceNodes = {}; 
      for (var j = 0; j < candidates.length; j++) {
        var candidate = candidates[j];
        sourceNodes[candidate.id()] = candidate;
      }

      for (var j = 0; j < candidates.length; j++) {
        canClock = candidates[j].getClock();
        for (var otherHost in canClock) {
          if (otherHost != candidates[j].getHostId()) {
            var id = otherHost + ":" + canClock[otherHost];
            delete sourceNodes[id];
          }
        }
      }

      for (var id in sourceNodes) {
        this.edges.add(new Edge(sourceNodes[id], curNode));
      }

      prevNode = curNode;
      curNode = this.nodes.getNext(host, curNode.getTime() + 1);
    }
  }
  return this;
}

/**
 * Nodes container
 */
function Nodes() {
  this.hosts = {};
}

Nodes.prototype.toLiteral = function(hiddenHosts) {
  var literal = [];
  var index = 0;
  for (var host in this.hosts) {
    if (hiddenHosts.indexOf(host) >= 0) {
      continue;
    }
    var arr = this.hosts[host]['times'];
    for (var i = 0; i < arr.length; i++) {
      var obj = this.get(host, arr[i]);
      var node = {};
      node["name"] = obj.getLog();
      node["group"] = host;
      if (obj.getTime() == 0) {
        node["startNode"] = true;
      }
      obj.setIndex(index);
      index += 1;
      literal.push(node);
    }
  }
  return literal;
}

Nodes.prototype.get = function(hostId, time) {
  var node = this.hosts[hostId][time];
  if (node === undefined) {
    return null;
  }
  return node;
}

Nodes.prototype.getNext = function(hostId, startTime) {
  var candidate = this.get(hostId, startTime);

  if (candidate == null) {
    var arr = this.hosts[hostId]['times'];
    if (!this.hosts[hostId]['sorted']) {
      this.hosts[hostId]['sorted'] = true;
      arr.sort();
    }
    for (var i = 0; i < arr.length; i++) {
      if (arr[i] > startTime) {
        return this.get(hostId, arr[i]);
      }
    }
  }
  return candidate;
}

Nodes.prototype.add = function(node) {
  var hostId = node.getHostId();
  var time = node.getTime();
  if (!this.hosts.hasOwnProperty(hostId)) {
    this.hosts[hostId] = {};
    this.hosts[hostId]['times'] = [];
  }
  this.hosts[hostId][time] = node;
  this.hosts[hostId]['times'].push(time);
  this.hosts[hostId]['sorted'] = false;
}

Nodes.prototype.getSortedHosts = function () {
  var hostCopy = this.hosts;
  return this.getHosts().sort(function(a, b) {
    return hostCopy[b]['times'].length - hostCopy[a]['times'].length;
  });
}

Nodes.prototype.getHosts = function() {
  return Object.keys(this.hosts);
}

var get = function (id) {
  return document.getElementById(id);
};

spaceTimeLayout = function () {
  var spaceTime = {},
      nodes = [],
      links = [],
      width = 960,
      height = 0,
      hosts = [];

  spaceTime.nodes = function(x) {
    if (!arguments.length) return nodes;
    nodes = x;
    return spaceTime;
  };

  spaceTime.links = function (x) {
    if (!arguments.length) return links;
    links = x;
    return spaceTime;
  };

  spaceTime.width = function (x) {
    if (!arguments.length) return width;
    width = x;
    return spaceTime;
  };

  spaceTime.height = function (x) {
    if (!arguments.length) return height;
    height = x;
    return spaceTime;
  };

  spaceTime.hosts = function (x) {
    if (!arguments.length) return hosts;
    hosts = x;
    return spaceTime;
  };

  spaceTime.start = function () {

    // This is the amount of vertical spacing between nodes
    var delta = 45;

    var n = nodes.length,
        m = links.length,
        o;

    // Assign each node an index
    for (i = 0; i < n; ++i) {
      o = nodes[i];
      o.index = i;
      o.y = delta;
      o.children = [];
      o.parents = 0;
    }

    // All vertical positions are initialized to delta 
    height = delta;

    // Give each link the source/target node objects based on indices computed
    // above
    for (i = 0; i < m; ++i) {
      o = links[i];
      if (typeof o.source == "number") o.source = nodes[o.source];
      if (typeof o.target == "number") o.target = nodes[o.target];
      o.source.children.push(o.target);
      o.target.parents++;
    }

    // Going to want to sort the nodes by increasing number of parents and put
    // them in a priority queue
    var remainingNodes = [];

    for (i = 0; i < n; ++i) {
      remainingNodes.push(nodes[i]);
    }

    while (remainingNodes.length > 0) {
      remainingNodes.sort(function(a, b) {
        return b.parents - a.parents;
      });

      o = remainingNodes.pop();

      horizontalPos = width / hosts.length * hosts.indexOf(o.group) + (width / hosts.length / 2);
      o.x = horizontalPos;
      for (i = 0; i < o.children.length; ++i) { 
        o.children[i].parents--;
        // Increment child position by delta
        if (o.y + delta > o.children[i].y) {
          o.children[i].y = o.y + delta;
          height = Math.max(height, o.y + delta);
        }
      }
    }

    height += delta;

    return spaceTime;
  };

  return spaceTime;
};

function clearText() {
  get("logField").value = "";
  get("logField").disabled = false;
  get("vizButton").disabled = false;
  get("curNode").innerHTML = "(click to view)"

  get("graph").hidden = true;
  d3.select("svg").remove();
}

get("clearButton").onclick = function() {
  clearText();
}

var spaceGraph;
var collapsedNodes;
var hiddenHosts;
var hostColors;

get("vizButton").onclick = function() {
  var textBox = get("logField");
  var lines = textBox.value.split('\n');

  // Initialize state 
  spaceGraph = new Graph();
  collapsedNodes = [];
  hiddenHosts = [];
  hostColors = {};

  if (!spaceGraph.parseLog(lines)) {
    // TODO: display error message
    return;
  }

  get("logField").disabled=true;
  get("vizButton").disabled=true;
  get("graph").hidden = false;

  var graphObj = spaceGraph.generateEdges().toLiteral();

  var color = d3.scale.category20();
  for (var i = 0; i < graphObj.hosts.length; i++) {
    var host = graphObj.hosts[i];
    hostColors[host] = color(host);
  }
  graph(graphObj);
};

function graph(graph) {
  var spaceTime = spaceTimeLayout();

  spaceTime
      .hosts(graph.hosts)
      .nodes(graph.nodes)
      .links(graph.links)
      .start();

  var svg = d3.select("#spaceTime").append("svg");

  var link = svg.selectAll(".link")
      .data(graph.links)
      .enter().append("line")
      .attr("class", "link")
      .style("stroke-width", function(d) { return 1; });

  link.attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { 
        if (d.source.hasOwnProperty("startNode") &&
            d.source.x != d.target.x) {
          return d.source.y + 10;   
        }
        return d.source.y;
      })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  var node = svg.selectAll(".node")
    .data(graph.nodes)
    .enter().append("circle")
    .on("click", function(e) { get("curNode").innerHTML = e.name; })
    .on("dblclick", function(e) { collapseEvent(e); })
    .attr("class", "node")
    .style("fill", function(d) { return hostColors[d.group]; })
    .attr("cx", function(d) { return d.x; });

  var standardNodes = node.filter(function(d) {
    return !d.hasOwnProperty("startNode");
  });

  var startNodes = node.filter(function(d) {
    return d.hasOwnProperty("startNode");
  });

  standardNodes.attr("cy", function(d) { return d.y; })
      .attr("r", function(d) { return 5; });

  startNodes.attr("cy", function(d) { return d.y - 20; })
      .attr("r", function(d) { return 15; });


  node.append("title")
      .text(function(d) { return d.name; });

  svg.attr("height", spaceTime.height());
  svg.attr("width", spaceTime.width());
}



function hideHost(e) {
  hiddenHosts.push(e.group);
  var graphObj = spaceGraph.toLiteral(hiddenHosts);
  d3.select("svg").remove();
  graph(graphObj);
}

function collapseEvent(e) {
  hideHost(e);
}
