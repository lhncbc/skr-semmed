<%@ page language="java"
	import="gov.nih.nlm.semmed.model.*,java.util.List" pageEncoding="UTF-8"%>

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
	<script type="text/javascript" src="<html:rewrite page="/scripts/aa.js"/>"></script>
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
	<jsp:include page="/jsp/header.jsp" />
	<ul class="tabs" id="mytabs">
		<li>
			<span class="left"><a href="../Welcome.do"><span
					class="center">Questions</span> </a></span>
		</li>
		<li>
			<span class="left"><strong><a href="../InitializeSearch.do"><span
					class="center">Search</span> </a></strong> </span>
		</li>
		<li>
			<span class="left"><a
					href="../InitializeQuestion.do"><span class="center">Question
							Relevance</span> </a></span>
		</li>
	</ul>
	<br>
	<div id="content">
		<html:form action="Visualize" method="post" focus="file"
			enctype="multipart/form-data">

			<logic:messagesPresent>
				<span id="errorsHeader"><bean:message key="errors.header" />
				</span>
				<html:messages id="emsg">
					<li>
						<bean:write name="emsg" />
					</li>
				</html:messages>
				<span id="errorsFooter"><bean:message key="errors.footer" />
				</span>
			</logic:messagesPresent>

			<table cellpadding="3">
				<logic:present name="summaryPredications" scope="session">
					<tr>
						<td>
							<input type="radio" name="inputType" value="summary"
								checked="checked">
							Process SemRep summary predications from the current session
						</td>
					</tr>
					<logic:present name="lang" scope="session">
						<tr>
							<td>
								<input type="radio" name="inputType" value="translation">
								Process TRANSLATED SemRep summary predications from the current
								session (
								<b>Target Language:</b>
								<bean:write name="lang" scope="session" />
								)
							</td>
						</tr>
					</logic:present>
					<tr>
						<td>
							<logic:present name="query" scope="session">
									(<b>Search Term:</b>
								<bean:write name="query" property="term" scope="session" />,
									<bean:define id="source" name="query" property="source"
									scope="session" type="java.lang.String" />
								<logic:equal name="source" value="ctrials">
									<b>Source:</b> clinicaltrials.gov,
										<b>Maximum Rank:</b>
									<%=((Query) session.getAttribute("query"))
							.getOption("clinicalTrialMax")%>,
										<b>Include trials that are no longer recruiting:</b> Yes,
										<b><%=((int[]) session
							.getAttribute("citationIDsClinicalTrials")).length%> </b> citations retrieved,
									</logic:equal>
								<logic:equal name="source" value="medline">
									<b>Source:</b> Medline,
										<b>Most Recent:</b>
									<%=((Query) session.getAttribute("query"))
							.getOption("pubmedMax")%>,
										<b>Start Date:</b>
									<%=((Query) session.getAttribute("query"))
							.getOption("pubmedStartDate")%>,
										<b>End Date:</b>
									<%=((Query) session.getAttribute("query"))
							.getOption("pubmedEndDate")%>,
										<b><%=((int[]) session
									.getAttribute("citationIDsMedline")).length%> </b> citations retrieved,
									</logic:equal>
								<logic:equal name="source" value="both">
									<b>Source:</b> Both sources,
										<b>Medline Most Recent:</b>
									<%=((Query) session.getAttribute("query"))
							.getOption("pubmedMax")%>,
										<b>Medline Start Date:</b>
									<%=((Query) session.getAttribute("query"))
							.getOption("pubmedStartDate")%>,
										<b>Medline End Date:</b>
									<%=((Query) session.getAttribute("query"))
							.getOption("pubmedEndDate")%>,
										<b>Clinical Trials Maximum Rank:</b>
									<%=((Query) session.getAttribute("query"))
							.getOption("clinicalTrialMax")%>,
										<b>Include trials that are no longer recruiting:</b> Yes,
										<b><%=((int[]) session
									.getAttribute("citationIDsClinicalTrials")).length
							+ ((int[]) session
									.getAttribute("citationIDsMedline")).length%> </b> citations retrieved,
									</logic:equal>
								<%=((List) session.getAttribute("summaryPredications"))
							.size()%> predications summarized.)
								</logic:present>
						</td>
					</tr>
				</logic:present>
				<tr>
					<td>
						<logic:notPresent name="summaryPredications" scope="session">
							<tr>
								<td>
									<input type="radio" name="inputType" value="file"
										checked="checked" />
									Upload File
						</logic:notPresent>
						<logic:present name="summaryPredications" scope="session">
							<tr>
								<td>
									<input type="radio" name="inputType" value="file" />
									Upload File
						</logic:present>
						<html:file property="uploadFile" />
						<html:submit property="method">
							<bean:message key="visualize.button.upload" />
						</html:submit>
					</td>
				</tr>
				<logic:present name="summaryPredications" scope="session">
					<tr>
						<td>
							<input type="checkbox" name="umls" />
							Include UMLS relations
						</td>
					</tr>
				</logic:present>
				<tr>
					<td>
						<html:submit property="method">
							<bean:message key="visualize.button.process" />
						</html:submit>
					</td>
				</tr>
				<logic:present role="SemMedTester">
				<tr>
					<td>
						<html:submit property="method">
							<bean:message key="visualize.button.processTest" />
						</html:submit>
					</td>
				</tr>
				</logic:present>
			</table>

<%--
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
								"id", "GraphApp",
								"quality", "high",
								"bgcolor", "#869ca7",
								"name", "GraphApp",
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
						id="GraphApp" width="100%" height="100%"
						codebase="http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab">
						<param name="movie" value="GraphApp.swf?graphKey=pepe" />
						<param name="quality" value="high" />
						<param name="bgcolor" value="#869ca7" />
						<param name="allowScriptAccess" value="sameDomain" />
						<param name='flashvars' value="graphKey=<c:out value="<%=graphKey%>"/>"/>
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
				<iframe name="_history" src="../swf/history.htm" frameborder="0"
					scrolling="no" width="22" height="0"></iframe>




							</logic:present>
			--%>
		</html:form>
	</div>
	<jsp:include page="/jsp/footer.jsp" />
</body>
<script language="javascript" type="text/javascript">
	</script>
</html:html>
