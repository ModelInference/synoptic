<!doctype html>
<!-- The DOCTYPE declaration above will set the    -->
<!-- browser's rendering engine into               -->
<!-- "Standards Mode". Replacing this declaration  -->
<!-- with a "Quirks Mode" doctype may lead to some -->
<!-- differences in layout.                        -->

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">


    <!--                                                               -->
    <!-- Consider inlining CSS to reduce the number of requested files -->
    <!--                                                               -->
    <link type="text/css" rel="stylesheet" href="SynopticGWT.css">

    <title>Synoptic</title>

    <!--                                           -->
    <!-- This script loads your compiled module.   -->
    <!-- If you add any GWT meta tags, they must   -->
    <!-- be added before this line.                -->
    <!--                                           -->
    <script type="text/javascript" language="javascript" src="/synopticgwt/synopticgwt.nocache.js"></script>

    <script type="text/javascript" src="/raphael-min.js"></script>

    <script type="text/javascript" src="/dracula_graffle.js"></script>
    <script type="text/javascript" src="/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="/dracula_graph.js"></script>
    <script type="text/javascript" src="/dracula_algorithms.js"></script>
    <script type="text/javascript" src="/seedrandom.js"></script>
    <script type="text/javascript">Math.seedrandom("any string you like, different");</script>
 	<script type="text/javascript" src="/graphhandler.js"></script>

    <!-- Analytics -->
    <script type="text/javascript">

    <%
    String analyticsTrackerID = (String) session.getAttribute("analyticsTrackerID");

    // Retrieve the analytics tracker ID property and, if it exists,
 	// includes the appropriate analytics JS snippet and initializes tracking.
 	// String analyticsTrackerID = System.getProperty("analyticsTrackerID");
 	if (analyticsTrackerID != null) {
 	%>
	    var analyticsTrackerID = '<%= analyticsTrackerID %>';
	    var _gaq = _gaq || [];
	    _gaq.push(['_setAccount', '<%= analyticsTrackerID %>']);

	    (function() {
	      var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
	      ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
	      var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
	    })();
	<%
	} else {
	    // No analytics ID specified => no tracking.
    %>
	    var analyticsTrackerID = null;
    <%
	}
 	%>
    </script>

  </head>

  <!--                                           -->
  <!-- The body can have arbitrary html, or      -->
  <!-- you can leave the body empty if you want  -->
  <!-- to create a completely dynamic UI.        -->
  <!--                                           -->
  <body>


    <!-- OPTIONAL: include this if you want history support -->
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>

    <!-- RECOMMENDED if your web app will not function without JavaScript enabled -->
    <noscript>
      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled
        in order for this application to display correctly.
      </div>
    </noscript>


	<div id="div-globalContainer">

		<div id="div-top-bar">
	    	<a id="a-logo-title" href="http://synoptic.googlecode.com">Synoptic</a> <br/>
	        <a href="http://synoptic.googlecode.com">Main site</a> |
	        <a href="http://code.google.com/p/synoptic/wiki/DocsWebAppTutorial">Tutorial</a> |
	    	<a href="http://code.google.com/p/synoptic/issues/entry">Report issue</a>
	    	<br/>
	    </div>


	    <div id="progressWheelDiv"></div>
	    
	    <div id="ErrorDiv"></div>
            
	    <div id="mainDiv"></div>

    </div>

    <div id="StackTraceDiv"></div>

    <div id="div-footer-bar">
    	Synoptic Changeset <%= session.getAttribute("synopticChangesetID") %> <br />
		SynopticGWT Changeset <%= session.getAttribute("synopticGWTChangesetID") %>
    </div>

  </body>
</html>
