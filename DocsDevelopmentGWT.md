# Introduction #

Synoptic has a GWT-based front-end that provides a web-UI to all the functionality found on the command line. This page describes how to get started with building/developing this UI.

Before you begin, make sure that you can clone and build Synoptic, as described [here](DocsDevelopment.md).

<br />
# Getting started with GWT #

---


Google Web Toolkit (GWT) is a web platform that allows you to write a single version of Java code that is then compiled down to Javascript for execution in the browser. For more information consult the following references:
  * [GWT wikipedia page](http://en.wikipedia.org/wiki/Google_Web_Toolkit)
  * [GWT examples](http://code.google.com/webtoolkit/examples/)
  * [GWT Overview](http://code.google.com/webtoolkit/overview.html)

Before starting with the Synoptic GWT application, you need to set up your Eclipse development environment. All the instructions are [here](http://code.google.com/webtoolkit/usingeclipse.html).

You might also want to work through building an example GWT application using [this tutorial](http://code.google.com/webtoolkit/doc/latest/tutorial/gettingstarted.html).


## Working with dependencies ##

All GWT projects depend on the Synoptic project. In particular, the dependency is on synoptic.jar archive, which is generated from the Synoptic project.

<font color='red'>**Important: always maintain up-to-date jar files (including synoptic.jar) in the GWT project you are working on.</font>**

For example, if you switch branches, revisions, or make changes to the Synoptic project then you must re-build the `synoptic.jar`. And, whenever the jars change in the `libs/` folder then you should run `ant libs` within the GWT project directory that you are working on. For example, for the SynopticGWT project you would do:

```
$ cd synoptic/            # Enter the synoptic project dir
$ ant jar                 # Build synoptic.jar
$ cd ../synopticgwt/      # Enter the SynopticGWT project dir
$ ant libs                # Copy the most recent jar libs into the SynopticGWT project
```

<br />
# SynopticGWT #

---


You'll find a `synopticgwt/` folder in Synoptic top level directory of the `default` branch. Follow the following steps:

  1. Import the contained Eclipse project into your workspace, and open it.
  1. As with the main Synoptic project, you will need to create and set the `SYNOPTIC_LIB` variable. Under Project Properties go to Java Build Path -> Libraries. Click on Add Variable to add a SYNOPTIC\_LIB variable. Set its value to be the absolute path of synoptic/lib'
  1. You must also compile the larger Synoptic project and export a `synoptic.jar` file into `synoptic/lib` -- this is a dependency for SynopticGWT. To do this, right click on `build.xml` file (at top level of the Synoptic project), go to "Run As", select "Ant Build..." (note the three dots at the end), make sure that the `jar` target is checked, and click "Run".

At this point, you should be able to `Run` the SynopticGWT application as a "Web Application". You can now access the application from a browser under a URL that looks like:
`http://127.0.0.1:8888/main?gwt.codesvr=127.0.0.1:9997`

## Dependencies ##

  * `synoptic.jar` : this is a core dependency.

  * [Dracula Graph Library](http://www.graphdracula.net/) : used for for visualizing models. This library is built on top of [Raphaël](http://raphaeljs.com/).

<br />
# EmbeddedGWT #

---

Once the SynopticGWT project is ready to be released it needs to be deployed onto a web server. However, some users might not want to connect to a remote server and might instead execute Synoptic as a locally hosted application. The EmbeddedGWT project bundles SynopticGWT with a web server to create a stand-alone self-hosting Synoptic web-application.

If you are developer working on SynopticGWT, you don't need to worry about EmbeddedGWT.

The EmbeddedGWT project is located in the `embeddedgwt/` folder in Synoptic's top level directory. To compile and run this project you must do the following:

  1. Import the contained Eclipse project into your workspace, and open it.
  1. Re-do the above step #2 to set up the `SYNOPTIC_LIB` variable
  1. Re-do the above step #3 to compile and export a `synoptic.jar` on which the EmbeddedGWT project depends.
  1. Finally, you need to export SynopticGWT as a web archive (war) file. To do this, right click on the `build.xml` file found at the top level of the SynopticGWT project and select "Run As" and then "Ant Build..." (note the three dots at the end). This should show a screen in which you can check the `war` target. Once you are done, click "Run". Eclipse will compile the project, create a synopticgwt.war file, and place it into the `EmbeddedGWT/apps/` folder.

You can now launch the EmbeddedGWT application and access it at the following default URL:
`http://127.0.0.1:8080/`


## Dependencies ##

  * [Jetty web server](http://www.eclipse.org/jetty/about.php) : used for hosting SynopticGWT.