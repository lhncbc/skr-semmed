
<%@ page language="java"
	import="gov.nih.nlm.semmed.model.*,java.util.*,org.apache.struts.util.*"
	pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean"
	prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html"
	prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic"
	prefix="logic"%>
<%@ taglib uri="pageabletable.tld" prefix="ale"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html:html locale="true">
<head>
	<html:base />
	<title>SemMed - Search</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="Search Page">
	<link href="<html:rewrite page="/css/semmed.css" />" rel="stylesheet"
		type="text/css">
	<script type="text/javascript" src="../scripts/boxover.js" /></script>
	<script type="text/javascript" src="../scripts/search.js"></script>
</head>
<body>
	<!--
	<div id="container">
-->
	<jsp:include page="/jsp/header.jsp" />
	<ul class="tabs">
		<li>
			<span class="left"><a href="../Welcome.do"><span
					class="center">Questions</span> </a></span>
		</li>
		<li>
			<span class="left"><a href="../InitializeSearch.do"><span
					class="center">Pubmed Search</span> </span>
		</li>
		<li>
			<span class="left"><strong><a href="../InitializeEssieSearch.do"><span
					class="center">Essie Search</span> </a></strong> </span>
		</li>
		<li>
			<span class="left"><a
					href="../InitializeQuestion.do"><span class="center">Question
							Relevance</span> </a></span>
		</li>
	<br>

	<div id="content">
		<html:form action="EssieSearch" method="POST" focus="file" enctype="multipart/form-data">
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
			<c:catch var="exception">
				<table>

					<tr>
						<td colspan="5">
					   <b> Essie Search: </b>
					   </td>
					  </tr>
					<tr>
						<td colspan="1">
							Query:
						</td>
						<bean:define id="term" name="query" property="term"
							scope="session" type="java.lang.String" />


						<td colspan="3" nowrap="nowrap">
							<html:text size="50" property="term" value="<%=term%>" />
						</td>
						<td colspan="1">
							<html:submit property="method">
								<bean:message key="search.button.search" />
							</html:submit>
						</td>
						<td colspan="1">
							<html:submit property="method">
								<bean:message key="search.button.reset" />
							</html:submit>
						</td>
					</tr>
				<tr>
					<td>
						<b>Options:</b>
					</td>
				</tr>
				<tr>
					<td>
						Maximum Rank:
						<bean:define id="maxRankList" name="citCounts" scope="application" />
						<html:select property="selectedMaxRank"
							value="<%=((Query) session.getAttribute("query")).getOption(
							"MEDLINEMax").toString()%>">
							<html:options collection="maxRankList" property="label"
								labelProperty="value" />
						</html:select>
					</td>
				</tr>
					<tr> <td>&nbsp; &nbsp; &nbsp; </td> </tr>
				   <tr>
						<td>
							<b>Sample from grant applications:</b>
						</td>
					</tr>
					<tr>

					</tr>

					<input type=hidden name="selectedSource" value="essie" >
				</table>
			</c:catch>

			<c:if test="${exception!=null}">
				<script type="text/javascript">alert("Your session has expired.");</script>
				<c:redirect url="Welcome.do">Your session has expired</c:redirect>
			</c:if>



			<!-- <logic:notPresent name="pubmedExtraOptions" scope="session"> -->
											<!--  <form method="POST" action="../Search.do;method=toggleLimits"> -->
											<%-- We are relying in the fact that the other buttons will override the method argument --%>
												<input type="hidden" name="method" value="search">
		   <!-- </logic:notPresent> -->


			<logic:present name="displayedCitations" scope="session">
				<br>
				<%
				String[] sourceNames = new String[] { "ESSIE" };
				%>
				<logic:equal name="source" value="opasi">
					<ul class="tabs" style="margin-left:-10px;">
						<li>
							<span class="left"><strong><a href="javascript:void(null);"><span
								class="center">OPASI grants</span> </a> </strong> </span>
						</li>
					</ul>
				</logic:equal>

				<!--
				<div sytle="position:fix; right:0px">
					<html:submit property="method">
						<bean:message key="search.button.export" />
					</html:submit>
					<html:submit property="method">
						<bean:message key="search.button.update" />
					</html:submit>
				</div> -->
				<div class="searchResults">
					<%
							for (String sourceName : sourceNames) {

							if (session.getAttribute("displayedCitations" + sourceName) == null)
								continue;

							int count = (Integer) session
							.getAttribute("count" + sourceName);
							int pageNumber = 0;
							if (session.getAttribute("pageNumber" + sourceName) != null)
								pageNumber = (Integer) session.getAttribute("pageNumber"
								+ sourceName);

							if (sourceName.equals(session.getAttribute("currentSource"))) {
					%>
					<div id="<%=sourceName%>">

						<%
						} else {
						%>
						<div id="<%=sourceName%>" style="display:none">
							<%
							}
							%>
							<div>
								<br>
								Found
								<%=count%>
								grant applications.

								<ale:index count="<%=count%>" currentPage="<%=pageNumber%>"
									link="<%="../Search.do;method=search?method=search&s="
						+ sourceName + "&p={}"%>" />

								<c:set var="count" value="<%=count%>" />

							</div>


							<c:if test="${count>0}">

								<table id="pubmed" rules="rows">
									<tr><th>Grant Applications</th><th></th></tr>

									<%
											List myCitations = (List) session
											.getAttribute("displayedCitations" + sourceName);

											for (Object o : myCitations) {
												SemMedDocument doc = (SemMedDocument) o;
												// HLJ SourceName is OPASI now
												if (sourceName.equals("ESSIE")) {
									%>
									<tr>
										<td>
											<c:out value="<%=doc.getTitleText()%>" />
											<br>
											<A href="../EssieSearch.do;method=search?method=search&PMID=<%= doc.getId() %>" target="_blank>" > <%= doc.getId() %> </A>
										</td>
									</tr>

									<%
									} else if (sourceName.equals("ClinicalTrials")) {
									%>
									<tr>
										<td>
											<a href="http://clinicaltrials.gov/show/<%=doc.getId()%>"
												target="_blank>"><%=doc.getTitleText()%> </a>
											<br>
											Condition:
											<c:out value="<%=((ClinicalStudy) doc).getCondition()%>" />
											<br>
											<%=doc.getId()%>
										</td>
										<td>
											<a href="javascript:void(0)"
												title="header=[Abstract] body=[<c:out value="<%=doc.getAbstractText()%>"/>]">Abstract</a>
										</td>
									</tr>

									<%
											}
											}
									%>

								</table>
							</c:if>
						</div>
						<%
						}
						%>
				</div>
			</logic:present>
			<logic:present name="selectedGrantRange" scope="session">
					<%
							int count = (Integer) session
							.getAttribute("countEssie");
					%>
					<div>
								Found
								<%=count%>
								 applications.
					</div>
			</logic:present>
		</html:form>
	</div>
	<jsp:include page="/jsp/footer.jsp" />

	<!-- </div>-->
</body>
<script type="text/javascript">
	toggleOptions();
</script>

</html:html>
