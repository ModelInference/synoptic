# Swing-based GUI #

Synoptic has a GUI that uses the Swing toolkit. Unfortunately, we have frozen our development of this GUI and are instead focusing our efforts on the [GWT-based GUI](DocsWebAppTutorial.md). However, you can still find and run the Swing-based GUI from the repository.


## Running the Swing GUI ##

You can show this GUI from the command line by passing Synoptic the `--showGui` option. Note that for this to work you must supply all the other required arguments -- the input log, the list of regular expressions, etc.


## Features ##

The GUI support different graph layouts, allows step-by-step refinement and coarsening of the graph (to inspect the Synoptic algorithm), and has support for listing the log lines corresponding to a node in the model. Here is what it looks like:

![http://wiki.synoptic.googlecode.com/hg/images/swing-gui-info.jpg](http://wiki.synoptic.googlecode.com/hg/images/swing-gui-info.jpg)