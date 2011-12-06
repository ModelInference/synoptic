/**
 * Originally grabbed from the official RaphaelJS Documentation
 * http://raphaeljs.com/graffle.html
 * Adopted (arrows) and commented by Philipp Strathausen http://blog.ameisenbar.de
 * Licenced under the MIT licence.
 */

/**
 * Usage:
 * connect two shapes
 * parameters:
 *      source shape [or connection for redrawing],
 *      target shape,
 *      style with { fg : linecolor, bg : background color, directed: boolean }
 * returns:
 *      connection { draw = function() }
 */
Raphael.fn.connection = function (obj1, obj2, style) {
    var selfRef = this;
    /* Create and return new connection. */
    var edge = {/*
        from : obj1,
        to : obj2,
        style : style,*/
        draw : function() {
            /* Get bounding boxes of target and source. */
            var bb1 = obj1.getBBox();
            var bb2 = obj2.getBBox();
            var isSelfLoop = false;

            /* If the x-coordinate and y-coordinate of the two boxes
             * are equivalent, then assume the two boxes are the same
             * and a self-loop is used to connect them. */
            if (bb1.x == bb2.x && bb1.y == bb2.y) {
            	isSelfLoop = true;
            }

            var path;
            
            /* Input points are displaced 1/6 of edge length clockwise from mid-point 
             * of each edge. Output points are displaced 1/6 of edge length 
             * counter-clockwise from mid-point of each edge. */
            /* The offset amount from the mid-point of a horizontal edge. */
            var horizOffset = bb1.width / 6;
            /* The offset amount from the mid-point of a vertical edge. */
            var vertOffset = bb1.height / 6;

            /* x-coordinate for label */
            var labelX;
            /* y-coordinate for label */
            var labelY;
            
            /* Coordinates for potential connection input points from bb1. */
            var p_inputs = [
                {x: bb1.x + bb1.width / 2 - horizOffset, y: bb1.y},              	/* NORTH in */
            	{x: bb1.x + bb1.width / 2  + horizOffset, y: bb1.y + bb1.height}, 	/* SOUTH in */
            	{x: bb1.x, y: bb1.y + bb1.height / 2 + vertOffset},             	/* WEST  in */
            	{x: bb1.x + bb1.width, y: bb1.y + bb1.height / 2 - vertOffset}, 	/* EAST  in */
            ];
                         
            /* Coordinates for potential connection output points to bb2. */
            var p_outputs = [
             	{x: bb2.x + bb2.width / 2 + horizOffset, y: bb2.y},              /* NORTH out */
             	{x: bb2.x + bb2.width / 2 - horizOffset, y: bb2.y + bb2.height}, /* SOUTH out */
             	{x: bb2.x, y: bb2.y + bb2.height / 2 - vertOffset},              /* WEST  out */
             	{x: bb2.x + bb2.width, y: bb2.y + bb2.height / 2 + vertOffset},  /* EAST  out */
             ];

            if (isSelfLoop) {
            	/* Source and destination nodes are the same node => draw a self loop. */
            	
            	/* Self-loops currently hard-coded to be EAST of object */
            	var loopX = p_inputs[3].x;
            	var startY = p_inputs[3].y;
            	var endY = p_outputs[3].y;

            	/* The offset used to calculate coordinates of control points.
            	 * control points are calculated relative to start and ending
            	 * points of self-loop. */
            	/* Increasing value increases length of oval. */
            	var controlPointXOffset = 30;
            	/* Decreasing value increases roundness of oval. */
            	var controlPointYOffset = 0;

            	/* Loop */
            	path = ["M", loopX, startY, "C", loopX + controlPointXOffset, startY + controlPointYOffset,
            	        loopX + controlPointXOffset, endY - controlPointYOffset, loopX, endY].join(",");

            	/* Arrow */
            	path = path + ",M"+(loopX+5)+","+(endY-5)+",L"+loopX+","+endY+",L"+(loopX+5)+","+(endY+5);

            	/* Label directly to right of self-loop */
            	labelX = loopX + controlPointXOffset + 15;
            	labelY = bb1.y + (bb1.height / 2);

            } else {
            	/* This branch contains unmodified Dracula code for computing paths. */
                                
                /* Distances between objects and according coordinates connection. */
                var d = {}, dis = [];

                /*
                 * Find out the best connection coordinates by trying all possible ways.
                 */
                /* Loop the first object's connection coordinates. */
                for (var i = 0; i < 4; i++) {
                	/* Loop the second object's connection coordinates. */
                	for (var j = 0; j < 4; j++) {
                		var dx = Math.abs(p_inputs[i].x - p_outputs[j].x),
                			dy = Math.abs(p_inputs[i].y - p_outputs[j].y);
                		if ((i == j) || (((i != 3 && j != 2) || p_inputs[i].x < p_outputs[j].x) && ((i != 2 && j != 3) || p_inputs[i].x > p_outputs[j].x) && ((i != 0 && j != 1) || p_inputs[i].y > p_outputs[j].y) && ((i != 1 && j != 0) || p_inputs[i].y < p_outputs[j].y))) {
                			dis.push(dx + dy);
                			d[dis[dis.length - 1].toFixed(3)] = [i, j + 4];
                		}
                	}
                }
                
                var disIsEmpty = dis.length == 0;
                var res = disIsEmpty ? [0, 4] : d[Math.min.apply(Math, dis).toFixed(3)];
                /* Bezier path. */
                var x1 = p_inputs[res[0]].x,
                	y1 = p_inputs[res[0]].y,
                	x4 = p_outputs[disIsEmpty ? res[1] : res[1] - 4].x,
                	y4 = p_outputs[disIsEmpty ? res[1] : res[1] - 4].y,
                	dx = Math.max(Math.abs(x1 - x4) / 2, 10),
                	dy = Math.max(Math.abs(y1 - y4) / 2, 10),
                	x2 = [x1, x1, x1 - dx, x1 + dx][res[0]].toFixed(3),
                	y2 = [y1 - dy, y1 + dy, y1, y1][res[0]].toFixed(3),  
                	x3 = [0, 0, 0, 0, x4, x4, x4 - dx, x4 + dx][res[1]].toFixed(3),
                	y3 = [0, 0, 0, 0, y1 + dy, y1 - dy, y4, y4][res[1]].toFixed(3);

                /* Assemble path and arrow. */
                path = ["M", x1.toFixed(3), y1.toFixed(3), "C", x2, y2, x3, y3, x4.toFixed(3), y4.toFixed(3)].join(",");
                /* Arrow */
                if(style && style.directed) {
                    /* Magnitude, length of the last path vector. */
                    var mag = Math.sqrt((y4 - y3) * (y4 - y3) + (x4 - x3) * (x4 - x3));
                    /* Vector normalisation to specified length.  */
                    var norm = function(x,l){return (-x*(l||5)/mag);};
                    /* Calculate array coordinates (two lines orthogonal to the path vector). */
                    var arr = [
                        {x:(norm(x4-x3)+norm(y4-y3)+x4).toFixed(3), y:(norm(y4-y3)+norm(x4-x3)+y4).toFixed(3)},
                        {x:(norm(x4-x3)-norm(y4-y3)+x4).toFixed(3), y:(norm(y4-y3)-norm(x4-x3)+y4).toFixed(3)}
                    ];
                    path = path + ",M"+arr[0].x+","+arr[0].y+",L"+x4+","+y4+",L"+arr[1].x+","+arr[1].y;
                }

                labelX = (x1+x4)/2;
                labelY = (y1+y4)/2;
            }

            /* Function to be used for moving existent path(s), e.g. animate() or attr(). */
            var move = "attr";
            /* Applying path(s). */
            edge.fg && edge.fg[move]({path:path})
                || (edge.fg = selfRef.path(path).attr({stroke: style && style.stroke || "#000", fill: "none"}).toBack());
            edge.bg && edge.bg[move]({path:path})
                || style && style.fill && (edge.bg = style.fill.split && selfRef.path(path).attr({stroke: style.fill.split("|")[0], fill: "none", "stroke-width": style.fill.split("|")[1] || 3}).toBack());
            /* Setting label. */
            style && style.label
                && (edge.label && edge.label.attr({x: labelX, y: labelY})
                    || (edge.label = selfRef.text(labelX, labelY, style.label).attr({fill: "#000", "font-size": style["font-size"] || "12px"})));
            style && style.label && style["label-style"] && edge.label && edge.label.attr(style["label-style"]);
            style && style.callback && style.callback(edge);
        }
    }
    edge.draw();
    return edge;
};
//Raphael.prototype.set.prototype.dodo=function(){console.log("works");};