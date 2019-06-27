
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
	<div align="right" >
		<a href="SMNHLBI_documentation.pdf">SemMedNHLBI Documentation</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	</div>
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
		<html:form action="Search" method="POST" focus="file" enctype="multipart/form-data">
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
			Question selected:
			<div style="border:1px solid black;background-color:yellow">
					<c:out value="${chosenQuestion}"/>

			</div>
			<br/>
			<table>
					<tr>
						<td>
							Upload Citation File
						</td>
						<td colspan ="4">
							<html:file property="uploadCitationFile" />
						</td>
						<td>
							<html:submit property="method">
								<bean:message key="search.button.uploadcitation" />
							</html:submit>
						</td>
					</tr>
					<% if(session.getAttribute("citationIDsMedline") != null){%>
					<tr>
					<td colspan="6">Upload citation file is completed! </td>
					</tr>
					<% } %>
			</table>

			<c:if test="${exception!=null}">
				<script type="text/javascript">alert("Your session has expired.");</script>
				<c:redirect url="Welcome.do">Your session has expired</c:redirect>
			</c:if>

			<!-- <logic:present role="SemMedTester">
			<table>
					<tr>
						<td>
							Upload Citation File
						</td>
						<td colspan ="4">
							<html:file property="uploadCitationFile" />
						</td>
						<td>
							<html:submit property="method">
								<bean:message key="search.button.uploadcitation" />
							</html:submit>
						</td>
					</tr>
					<% if(session.getAttribute("citationIDsMedline") != null){%>
					<tr>
					<td colspan="6">Upload citation file is completed! </td>
					</tr>
					<% } %>
				</table>
			 </logic:present> -->

			<logic:present name="displayedCitations" scope="session">
				<br>
				<%
				String[] sourceNames = new String[] { "Medline", "ClinicalTrials" };
				%>
				<logic:equal name="source" value="medline">
					<ul class="tabs" style="margin-left:-10px;">
						<li>
							<span class="left"><strong><a href="javascript:void(null);"><span
								class="center">MEDLINE Citations</span> </a> </strong> </span>
						</li>
					</ul>
				</logic:equal>
				<logic:equal name="source" value="ctrials">
					<ul class="tabs" style="margin-left:-10px;">
						<li>
							<span class="left"><strong><a href="javascript:void(null);"><span
								class="center">Clinical Trials</span> </a> </strong> </span>
						</li>
					</ul>
				</logic:equal>
				<logic:equal name="source" value="both">
					<ul class="tabs" style="margin-left:-10px">
						<li>
							<% if (session.getAttribute("currentSource").equals("Medline")){%>
							<span class="left"><a href="javascript:void(null);" onclick="tab('Medline')"><span
								class="center" id="medlineTab" style="font-weight:bold">Medline Articles</span> </a> </span>
							<%}else{ %>
								<span class="left"><a href="javascript:void(null);" onclick="tab('Medline')"><span
									class="center" id="medlineTab">Medline Articles</span> </a> </span>
							<%} %>
						</li>
						<li>
						 	<% if (session.getAttribute("currentSource").equals("ClinicalTrials")){%>
							<span class="left"><a href="javascript:void(null);" onclick="tab('ClinicalTrials')"><span
								class="center" id="ctTab" style="font-weight:bold">Clinical Trials</span> </a> </span>
							<%}else{ %>
							<span class="left"><a href="javascript:void(null);" onclick="tab('ClinicalTrials')"><span
								class="center" id="ctTab">Clinical Trials</span> </a> </span>
							<%} %>
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
								citations.

								<ale:index count="<%=count%>" currentPage="<%=pageNumber%>"
									link="<%="../Search.do;method=search?method=search&s="
						+ sourceName + "&p={}"%>" />

								<c:set var="count" value="<%=count%>" />

							</div>


							<c:if test="${count>0}">

								<table id="pubmed" rules="rows">
									<tr><th>Citations</th><th></th></tr>

									<%
											List myCitations = (List) session
											.getAttribute("displayedCitations" + sourceName);

											for (Object o : myCitations) {
												SemMedDocument doc = (SemMedDocument) o;

												if (sourceName.equals("Medline")) {
									%>
									<tr>
										<td>
											<a
												href="http://ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=AbstractPlus&list_uids=<%=doc.getId()%>"
												target="_blank>"> <%=((PubmedArticle) doc).getAuthorList()%>
											</a>
											<br>
											<c:out value="<%=doc.getTitleText()%>" />
											<br>
											<%=doc.getId()%>
										</td>
										<td>
											<a href="javascript:void(0)" onclick="addBox('<%=doc.getAbstractText().replaceAll("'","\\\\'").replaceAll("\"","\\\\'")%>')">
											Abstract</a>
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
		</html:form>
	</div>
	<jsp:include page="/jsp/footer.jsp" />

	<!-- </div>-->
</body>
<script type="text/javascript">
	toggleOptions();
</script>

</html:html>
