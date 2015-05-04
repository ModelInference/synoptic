# Overview #

Synoptic is written in Java. The standard interface to launch Synoptic is the command line. This is known to work on OS X and Linux, although it should work similarly on other systems. The project also includes a separate web front-end called SynopticGWT, which uses Synoptic as a back-end library.

To install Synoptic, [check out the source](http://code.google.com/p/synoptic/source/checkout), and then build Synoptic on your local system.

You have two options for installing SynopticGWT:
  * Run SynopticGWT as a desktop application
  * [Deploy SynopticGWT](DocsInstallation#Deploying_SynopticGWT.md) onto a stand-alone web-server

Once you have installed Synoptic, you can learn more about how to use it:
  * Command line
    * [tutorial](DocsSynopticCmdLineTutorial.md)
    * [usage](DocsSynopticCmdLineUsage.md)
    * [help screen](DocsSynopticCmdLineHelpScreen.md)
  * SynopticGWT web interface
    * [tutorial](DocsWebAppTutorial.md)

If you plan to modify Synoptic's source code then you might want to read the Synoptic [development](DocsDevelopment.md) guide.

<br />
# External dependencies #

---


Synoptic and SynopticGWT depend on [Graphviz](http://www.graphviz.org/) to render the output graphs. It's best to install Graphviz before you install Synoptic.

Synoptic will try to find the GraphViz `dot` command in a few different locations. You can also specify this path explicitly with the `-d` command line option.

<br />
# Installing Synoptic from source #

---


Make sure that you have installed a copy of **[JRE 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html)** or higher and that the `java` command is in your path.

First, you will need to [check out](http://code.google.com/p/synoptic/source/checkout) the Synoptic source code repository (**warning**: it is over 250MB).

Next, you can build Synoptic from the command line with `ant`, or build it from within [Eclipse](http://www.eclipse.org/) (a Java IDE).

Building from the command line (simpler):
  1. Assuming that you checked out the code into `synoptic/`, there will be a top-level `synoptic/build.xml` file for `ant` to build all of the projects in the repository.
  1. Run `ant synoptic` inside the `synoptic/` directory to build synoptic and all of its dependencies.


Building from within Eclipse (more complex):
  1. Install [Eclipse](http://www.eclipse.org/) and **[JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/index.htm)**.
  1. Assuming that you checked out the code into `synoptic/`, you can now import an "existing project" into Eclipse from `synoptic/synoptic/`.
  1. Import the 'synoptic' project into Eclipse. Eclipse will fail to locate jar dependencies because the project depends on a `SYNOPTIC_LIB` variable. This variable specifies where external jars can be found on the local file system. Define and set this variable by doing the following:
    1. Right click on your project and select Properties
    1. Select Java Build Path and then the Libraries tab
    1. Select Add Variable...
    1. Select Configure Variables...
    1. Select New...
    1. Give the variable the name `SYNOPTIC_LIB`
    1. Select Folder..., and navigate to `synoptic/lib/`
    1. Select OK, and then another OK.
    1. Select your new variable from the list, click on Extend...
    1. Navigate to and include the following jar files from `synoptic/lib/` for: junit, plume, daikon, json
  1. Import the 'daikonizer' project into Eclipse (note: synoptic depends on daikonizer).
    * You will need to follow the steps above to add a `SYNOPTIC_LIB` variable and specify a dependency on `synoptic/lib/daikon.jar`
  1. You should see that the `build.xml` file has errors (if not, skip this step).  Fix this by disabling all buildfile errors: under Preferences -> Ant -> Editor, in the tab Problems, check "Ignore all buildfile problems".
  1. Build both synoptic and daikonizer projects from Eclipse. This should produce .class files in `synoptic/synoptic/bin/`.


Finally, you will want to test your build:
  1. Run one of the following two (equivalent) commands from within `synoptic/`
    * `synoptic.sh -h`
    * `java -cp ./lib/*:./synoptic/bin/ synoptic.main.SynopticMain -h`
  1. Confirm that the execution outputs a help/usage screen

<br />
# Deploying SynopticGWT onto a server #

---


The SynopticGWT project may be compiled into a
[war archive](http://en.wikipedia.org/wiki/WAR_file_format_(Sun)) and
deployed onto a stand-alone server. The following instructions detail
how to do this for the [Jetty](http://wiki.eclipse.org/Jetty/) web
server running on Ubuntu 11.10. Other deployment options are possible
(e.g., [Apache Tomcat](http://tomcat.apache.org/)). The only web server
requirement is that it must be able to read a war archive and include
support for [JSP](http://en.wikipedia.org/wiki/JavaServer_Pages).

To deploy SynopticGWT onto a stand-alone Jetty server instance, on
Ubuntu 11.10, do the following:
  1. Install basic utilities:
    * `apt-get install curl emacs`
  1. Install the JDK:
    * `apt-get install sun-java7-jdk`
  1. Install the [Jetty](http://wiki.eclipse.org/Jetty/) server on the serving host
    * `apt-get install jetty`
  1. Install Jasper JSP support and other extensions to Jetty
    * `apt-get install libjetty-extra`
  1. Install the [Graphviz](http://www.graphviz.org/) package (Synoptic uses the `dot` command for exporting models):
    * `apt-get install graphviz`
  1. Configure Jetty by setting the appropriate IP/port, and JDK\_DIRS:
    * `emacs /etc/default/jetty`
```
JETTY_HOST=ip
JETTY_PORT=port
JDK_DIRS="/usr/lib/jvm/java-7-sun"
```
  1. Whenever you change Jetty's configuration, restart Jetty:
    * `/etc/init.d/jetty restart`
  1. Modify jetty's [webdefault.xml](http://irc.codehaus.org/display/JETTY/webdefault.xml) and change `dirAllowed` to `false` and `welcomeServlets` to `true`. Here is the [rationale](http://docs.codehaus.org/display/JETTY/Welcome+files+not+working) for this change. The init-param snippets in the file should look like this:
```
<init-param>
    <param-name>dirAllowed</param-name>
    <param-value>false</param-value>
</init-param>
<init-param>
    <param-name>welcomeServlets</param-name>
    <param-value>true</param-value>
</init-param>
```
  1. Modify jetty's start.config file located at `/etc/jetty/start.config` and include the following lines at the top of file
```
######## SynopticGWT deployment system property values ########

# [Optional] Derby database location for tracking web-app
# usage. Initially, this directory should not exist, or it must
# contain an existing derby instance that has already been created by
# SynopticGWT.
derbyDBDir=/derby-storage/derby

# Google analytics tracking identifier
analyticsTrackerID=UA-12345678-1

# Directory to use for exporting models (must be accessible by Jetty)
# A relative path is relative w.r.t. the deployed root.war archive
modelExportsDir=/var/lib/jetty/webapps/model-exports/

# The URL prefix for use by clients to access exported model files.
# NOTE: modelExportsURLprefix must map to modelExportsDir
# No trailing slash.
modelExportsURLprefix=model-exports

# Absolute directory path where uploaded log files are stored
logFilesDir=/uploaded-logfiles/
```
  1. Create the absolute directory paths and allow the jetty daemon to access them:
    * `mkdir /var/lib/jetty/webapps/model-exports/`
    * `chown jetty /var/lib/jetty/webapps/model-exports/`
    * `mkdir /uploaded-logfiles/`
    * `chown jetty /uploaded-logfiles/`
  1. Build Synoptic (this generates synoptic.jar in synoptic/libs/):
    * `cd synoptic/synoptic/ && ant jar`
  1. Build a deployment war archive:
    * `cd synoptic/synopticgwt/ && ant deployment-war`
  1. Copy over the generated synopticgwt.war archive into Jetty's webapps dir, rename it to root.war, and restart Jetty
    * `cp synoptic/synopticgwt.war /var/lib/jetty/webapps/root.war`
    * `/etc/init.d/jetty restart`
  1. Try accessing http://host:port/ to check if the SynopticGWT app is running:
    * `curl http://ip:port/`

Troubleshooting resources:
  * [Jetty's debian packages page](http://docs.codehaus.org/display/JETTY/Debian+Packages)
  * [Eclipse Jetty site](http://wiki.eclipse.org/Jetty/)

Resources to customize the basic installation:
  * [Jetty JSP customization](http://docs.codehaus.org/display/JETTY/Jsp+Configuration)
  * [Setting Jetty's Cache-control](http://docs.codehaus.org/display/JETTY/LastModifiedCacheControl)
  * [Jetty's webdefault.xml](http://docs.codehaus.org/display/JETTY/webdefault.xml)