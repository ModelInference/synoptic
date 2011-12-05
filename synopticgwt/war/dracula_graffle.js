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
    /* create and return new connection */
    var edge = {/*
        from : obj1,
        to : obj2,
        style : style,*/
        draw : function() {
            /* get bounding boxes of target and source */
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

            /* x-coordinate for label */
            var labelX;
            /* y-coordinate for label */
            var labelY;

            if (isSelfLoop) {
            	/* Source and destination nodes are the same node => draw a self loop. */

            	var x = bb1.x + bb1.width;
            	var startY = bb1.y + (bb1.height / 4);
            	var endY = bb1.y + ((bb1.height * 3) / 4);

            	/* The offset used to calculate coordinates of control points.
            	 * control points are calculated relative to start and ending
            	 * points of self-loop. */
            	/* Increasing value increases length of oval. */
            	var controlPointXOffset = 30;
            	/* Decreasing value increases roundness of oval. */
            	var controlPointYOffset = 5;

            	/* loop */
            	path = ["M", x, startY, "C", x + controlPointXOffset, startY + controlPointYOffset,
            	        x + controlPointXOffset, endY - controlPointYOffset, x, endY].join(",");

            	/* arrow */
            	path = path + ",M"+(x+5)+","+(endY-5)+",L"+x+","+endY+",L"+(x+5)+","+(endY+5);

            	labelX = bb1.x + bb1.width + controlPointXOffset + 15;
            	labelY = bb1.y + (bb1.height / 2);

            } else {
            	/* This branch contains unmodified Dracula code for computing paths. */
                var off1 = 0;
                var off2 = 0;
                
                /* coordinates for potential connection coordinates from bb1 */
                var p_inputs = [
                    {x: bb1.x + bb1.width / 2, y: bb1.y - off1},              /* NORTH 1 */
                	{x: bb1.x + bb1.width / 2, y: bb1.y + bb1.height + off1}, /* SOUTH 1 */
                	{x: bb1.x - off1, y: bb1.y + bb1.height / 2},             /* WEST  1 */
                	{x: bb1.x + bb1.width + off1, y: bb1.y + bb1.height / 2}, /* EAST  1 */
                	{x: bb1.x - off1, y: bb1.y - off1}, /* NORTHWEST 1 */
                	{x: bb1.x + bb1.width + off1, y: bb1.y + bb1.height + off1} /* SOUTHEAST 1 */
                ];
                             
                /* coordinates for potential connection coordinates to bb2 */
                var p_outputs = [
                     {x: bb2.x + bb2.width / 2, y: bb2.y - off2},              /* NORTH 2 */
                     {x: bb2.x + bb2.width / 2, y: bb2.y + bb2.height + off2}, /* SOUTH 2 */
                     {x: bb2.x - off2, y: bb2.y + bb2.height / 2},             /* WEST  2 */
                     {x: bb2.x + bb2.width + off2, y: bb2.y + bb2.height / 2},  /* EAST  2 */
                     {x: bb2.x - off1, y: bb2.y + bb2.height + off1}, /* SOUTHWEST 2 */
                     {x: bb2.x + bb2.width + off1, y: bb1.y - off2} /* NORTHEAST 2 */
                 ];
                                
                /* distances between objects and according coordinates connection */
                var d = {}, dis = [];

                /*
                 * find out the best connection coordinates by trying all possible ways
                 */
                /* loop the first object's connection coordinates */
                for (var i = 0; i < p_inputs.length; i++) {
                	/* loop the second object's connection coordinates */
                	for (var j = 0; j < p_outputs.length; j++) {
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
                /* bezier path */
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

                /* assemble path and arrow */
                path = ["M", x1.toFixed(3), y1.toFixed(3), "C", x2, y2, x3, y3, x4.toFixed(3), y4.toFixed(3)].join(",");
                /* arrow */
                if(style && style.directed) {
                    /* magnitude, length of the last path vector */
                    var mag = Math.sqrt((y4 - y3) * (y4 - y3) + (x4 - x3) * (x4 - x3));
                    /* vector normalisation to specified length  */
                    var norm = function(x,l){return (-x*(l||5)/mag);};
                    /* calculate array coordinates (two lines orthogonal to the path vector) */
                    var arr = [
                        {x:(norm(x4-x3)+norm(y4-y3)+x4).toFixed(3), y:(norm(y4-y3)+norm(x4-x3)+y4).toFixed(3)},
                        {x:(norm(x4-x3)-norm(y4-y3)+x4).toFixed(3), y:(norm(y4-y3)-norm(x4-x3)+y4).toFixed(3)}
                    ];
                    path = path + ",M"+arr[0].x+","+arr[0].y+",L"+x4+","+y4+",L"+arr[1].x+","+arr[1].y;
                }

                labelX = (x1+x4)/2;
                labelY = (y1+y4)/2;
            }

            /* function to be used for moving existent path(s), e.g. animate() or attr() */
            var move = "attr";
            /* applying path(s) */
            edge.fg && edge.fg[move]({path:path})
                || (edge.fg = selfRef.path(path).attr({stroke: style && style.stroke || "#000", fill: "none"}).toBack());
            edge.bg && edge.bg[move]({path:path})
                || style && style.fill && (edge.bg = style.fill.split && selfRef.path(path).attr({stroke: style.fill.split("|")[0], fill: "none", "stroke-width": style.fill.split("|")[1] || 3}).toBack());
            /* setting label */
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