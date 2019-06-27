<%@ page language="java"
	import="gov.nih.nlm.semmed.struts.action.VisualizeAction"
	pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean"
	prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html"
	prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic"
	prefix="logic"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display"%>
<%@ taglib uri="http://ajaxanywhere.sourceforge.net/" prefix="aa"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html:html locale="true">
<head>
	<html:base />
	<title>SemMed - Visualization</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="Visualization page">
	<link href="<html:rewrite page="/css/semmed.css" />" rel="stylesheet"
		type="text/css">
	<script type="text/javascript"
		src="<html:rewrite page="/scripts/aa.js"/>"></script>
	<script src="../swf/AC_OETags.js" language="javascript"></script>
	<script language="JavaScript" type="text/javascript">
		<!--
		// -----------------------------------------------------------------------------
		// Globals
		// Major version of Flash required
		var requiredMajorVersion = 9;
		// Minor version of Flash required
		var requiredMinorVersion = 0;
		// Minor version of Flash required
		var requiredRevision = 0;
		// -----------------------------------------------------------------------------
		// -->
	</script>
</head>
<body>
		<logic:present name="key" scope="session">

			<bean:define id="graphKey" name="key" scope="session"
				type="java.lang.String" />

			<logic:present name="maxedPredications" scope="session">
				<script language="javascript" type="text/javascript">
					    <!--
						alert("The original number of predications (<%=session.getAttribute("maxedPredications")%>) has been limited to <%=VisualizeAction.LIMIT%> for purposes of visualization");
						-->
					</script>
			</logic:present>

			<logic:present name="cluster" scope="session">
			<script language="JavaScript" type="text/javascript"
				src="../swf/history.js"></script>
			<script language="JavaScript" type="text/javascript">
				<!--
					// Version check for the Flash Player that has the ability to start Player Product Install (6.0r65)
					var hasProductInstall = DetectFlashVer(6, 0, 65);

					// Version check based upon the values defined in globals
					var hasRequestedVersion = DetectFlashVer(requiredMajorVersion, requiredMinorVersion, requiredRevision);


					// Check to see if a player with Flash Product Install is available and the version does not meet the requirements for playback
					if ( hasProductInstall && !hasRequestedVersion ) {
						// MMdoctitle is the stored document.title value used by the installation process to close the window that started the process
						// This is necessary in order to close browser windows that are still utilizing the older version of the player after installation has completed
						// DO NOT MODIFY THE FOLLOWING FOUR LINES
						// Location visited after installation is complete if installation is required
						var MMPlayerType = (isIE == true) ? "ActiveX" : "PlugIn";
						var MMredirectURL = window.location;
					    document.title = document.title.slice(0, 47) + " - Flash Player Installation";
					    var MMdoctitle = document.title;

						AC_FL_RunContent(
							"src", "playerProductInstall",
							"FlashVars", "graphKey=<c:out value="<%=graphKey%>"/>&MMredirectURL="+MMredirectURL+'&MMplayerType='+MMPlayerType+'&MMdoctitle='+MMdoctitle+"",
							"width", "100%",
							"height", "100%",
							"align", "middle",
							"id", "GraphAppCluster",
							"quality", "high",
							"bgcolor", "#869ca7",
							"name", "GraphAppCluster",
							"allowScriptAccess","sameDomain",
							"type", "application/x-shockwave-flash",
							"pluginspage", "http://www.adobe.com/go/getflashplayer"
						);
					} else if (hasRequestedVersion) {
						// if we've detected an acceptable version
						// embed the Flash Content SWF when all tests are passed
						AC_FL_RunContent(
								"src", "GraphAppCluster",
								"width", "100%",
								"height", "100%",
								"align", "middle",
								"id", "GraphAppCluster",
								"quality", "high",
								"bgcolor", "#869ca7",
								"name", "GraphAppCluster",
								"flashvars",'graphKey=<c:out value="<%=graphKey%>"/>&historyUrl=history.htm%3F&lconid=' + lc_id + '',
								"allowScriptAccess","sameDomain",
								"type", "application/x-shockwave-flash",
								"pluginspage", "http://www.adobe.com/go/getflashplayer"
						);
					  } else {  // flash is too old or we can't detect the plugin
					    var alternateContent = 'Alternate HTML content should be placed here. '
					  	+ 'This content requires the Adobe Flash Player. '
					   	+ '<a href=http://www.adobe.com/go/getflash/>Get Flash</a>';
					    document.write(alternateContent);  // insert non-flash content
					  }
				// -->
				</script>
				<noscript>
				<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
					id="GraphAppCluster" width="100%" height="100%"
					codebase="http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab">
					<param name="movie" value="GraphAppCluster.swf?graphKey=pepe" />
					<param name="quality" value="high" />
					<param name="bgcolor" value="#869ca7" />
					<param name="allowScriptAccess" value="sameDomain" />
					<param name='flashvars'
						value="graphKey=<c:out value="<%=graphKey%>"/>" />
					<embed src="GraphAppCluster.swf" quality="high" bgcolor="#869ca7"
						width="100%" height="100%" name="GraphAppCluster" align="middle"
						play="true" loop="false" quality="high"
						allowScriptAccess="sameDomain"
						type="application/x-shockwave-flash"
						pluginspage="http://www.adobe.com/go/getflashplayer"
						FlashVars="graphKey=<c:out value="<%=graphKey%>"/>">
					</embed>
				</object>
			</noscript>
				</logic:present>
				<logic:notPresent name="cluster" scope="session">
							<script language="JavaScript" type="text/javascript"
				src="../swf/history.js"></script>
			<script language="JavaScript" type="text/javascript">
				<!--
					// Version check for the Flash Player that has the ability to start Player Product Install (6.0r65)
					var hasProductInstall = DetectFlashVer(6, 0, 65);

					// Version check based upon the values defined in globals
					var hasRequestedVersion = DetectFlashVer(requiredMajorVersion, requiredMinorVersion, requiredRevision);


					// Check to see if a player with Flash Product Install is available and the version does not meet the requirements for playback
					if ( hasProductInstall && !hasRequestedVersion ) {
						// MMdoctitle is the stored document.title value used by the installation process to close the window that started the process
						// This is necessary in order to close browser windows that are still utilizing the older version of the player after installation has completed
						// DO NOT MODIFY THE FOLLOWING FOUR LINES
						// Location visited after installation is complete if installation is required
						var MMPlayerType = (isIE == true) ? "ActiveX" : "PlugIn";
						var MMredirectURL = window.location;
					    document.title = document.title.slice(0, 47) + " - Flash Player Installation";
					    var MMdoctitle = document.title;

						AC_FL_RunContent(
							"src", "playerProductInstall",
							"FlashVars", "graphKey=<c:out value="<%=graphKey%>"/>&MMredirectURL="+MMredirectURL+'&MMplayerType='+MMPlayerType+'&MMdoctitle='+MMdoctitle+"",
							"width", "100%",
							"height", "100%",
							"align", "middle",
							"id", "GraphApp",
							"quality", "high",
							"bgcolor", "#869ca7",
							"name", "GraphApp",
							"allowScriptAccess","sameDomain",
							"type", "application/x-shockwave-flash",
							"pluginspage", "http://www.adobe.com/go/getflashplayer"
						);
					} else if (hasRequestedVersion) {
						// if we've detected an acceptable version
						// embed the Flash Content SWF when all tests are passed
						AC_FL_RunContent(
								"src", "GraphApp",
								"width", "100%",
								"height", "100%",
								"align", "middle",
								"id", "GraphAppCluster",
								"quality", "high",
								"bgcolor", "#869ca7",
								"name", "GraphAppCluster",
								"flashvars",'graphKey=<c:out value="<%=graphKey%>"/>&historyUrl=history.htm%3F&lconid=' + lc_id + '',
								"allowScriptAccess","sameDomain",
								"type", "application/x-shockwave-flash",
								"pluginspage", "http://www.adobe.com/go/getflashplayer"
						);
					  } else {  // flash is too old or we can't detect the plugin
					    var alternateContent = 'Alternate HTML content should be placed here. '
					  	+ 'This content requires the Adobe Flash Player. '
					   	+ '<a href=http://www.adobe.com/go/getflash/>Get Flash</a>';
					    document.write(alternateContent);  // insert non-flash content
					  }
				// -->
				</script>
				<noscript>
				<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
					id="GraphAppCluster" width="100%" height="100%"
					codebase="http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab">
					<param name="movie" value="GraphAppCluster.swf?graphKey=pepe" />
					<param name="quality" value="high" />
					<param name="bgcolor" value="#869ca7" />
					<param name="allowScriptAccess" value="sameDomain" />
					<param name='flashvars'
						value="graphKey=<c:out value="<%=graphKey%>"/>" />
					<embed src="GraphApp.swf" quality="high" bgcolor="#869ca7"
						width="100%" height="100%" name="GraphApp" align="middle"
						play="true" loop="false" quality="high"
						allowScriptAccess="sameDomain"
						type="application/x-shockwave-flash"
						pluginspage="http://www.adobe.com/go/getflashplayer"
						FlashVars="graphKey=<c:out value="<%=graphKey%>"/>">
					</embed>
				</object>
			</noscript>
				</logic:notPresent>

			<iframe name="_history" src="../swf/history.htm" frameborder="0"
				scrolling="no" width="22" height="0"></iframe>

		</logic:present>
</body>
</html:html>
