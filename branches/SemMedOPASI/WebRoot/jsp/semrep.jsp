
<%@ page language="java"
	import="java.util.List,gov.nih.nlm.semmed.model.*" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
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

	<title>SemMed - Semrep</title>

	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="SemRep page">
	<link href="<html:rewrite page="/css/semmed.css" />" rel="stylesheet"
		type="text/css">
</head>
<body>
		<jsp:include page="/jsp/header.jsp" />
		<ul class="tabs">
			<li>
				<span class="left"><a href="../Welcome.do"><span
							class="center">Search</span> </a></span>
			</li>
			<logic:present role="OPASITester">
			<li>
				<span class="left"><strong><a href="../InitializeSemrep.do"><span
						class="center">SemRep</span> </a></strong> </span>
			</li>
			</logic:present>
			<!--  <li>
				<span class="left"><a href="../InitializeQuestion.do"><span
						class="center">Question Relevance</span> </a> </span>
			</li> -->
			<li>
				<span class="left"><a href="../InitializeSummary.do"><span
						class="center">Summarization</span> </a> </span>
			</li>
			<!--  <li>
				<span class="left"><a href="../InitializeVisualization.do"><span
						class="center">Visualization</span> </a> </span>
			</li> -->
		</ul>

	<br>
		<div id="content">
			<html:form action="Semrep" method="post" focus="file"
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
				<table cellpadding="4">
					<logic:present name="displayedCitations" scope="session">
						<tr>
							<td>
								<logic:present name="query" scope="session">
									(<b>Query:</b>
									<bean:write name="query" property="term" scope="session" />,
									<bean:define id="source" name="query" property="source"
										scope="session" type="java.lang.String" />
										<b>Source:</b> OPASI,
										<b><%=((int[]) session
										.getAttribute("citationIDsOPASI")).length%> </b> grant applications retrieved.)
								</logic:present>
							</td>
						</tr>
					</logic:present>
					<table cellspacing="3">
					<tr>
						<td>
							<html:submit property="method">
								<bean:message key="semrep.button.process" />
							</html:submit>
						</td>
					</tr>
					<logic:present role="OPASITester">
					<tr>
						<td>
							<input type="radio" name="inputType" value="file">
							Upload File
							<html:file property="uploadFile" />
							<html:submit property="method">
								<bean:message key="semrep.button.upload" />
							</html:submit>
						</td>
					</tr>
					<tr>
						<td>
							Upload Predication from Test Database
							<html:submit property="method">
								<bean:message key="semrep.button.uploaddb" />
							</html:submit>
						</td>
					</tr>
					<tr>
					 	<td>
							<logic:present name="predications" scope="session">
							<html:submit property="method">
								<bean:message key="semrep.button.export" />
							</html:submit>
							</logic:present>
						</td>
					</tr>
					</logic:present>
				</table>



				<logic:present name="predications" scope="session">
					<%
						int count = ((List) session.getAttribute("predications")).size();
						int pageNumber = 0;
						if (session.getAttribute("pageNumberSemrep") != null)
							pageNumber = (Integer) session.getAttribute("pageNumberSemrep");
					%>
					<div class="searchResults">
						<div>
							<br>
							Found
							<%=count%>
							predications.

							<c:if test="${predsource == 1}">
								<ale:index count="<%=count%>" currentPage="<%=pageNumber%>"
								link="<%="../Semrep.do;method=process?method=process&p={}"%>" />
							</c:if>
							<c:if test="${predsource == 2}">
								<ale:index count="<%=count%>" currentPage="<%=pageNumber%>"
								link="<%="../Semrep.do;method=uploaddb?method=uploaddb&p={}"%>" />
							</c:if>
							<c:set var="count" value="<%=count%>" />

						</div>

						<c:if test="${count>0}">

							<table id="pubmed" rules="rows">
								<tr>
									<th>
										ID
									</th>
									<th>
										Sentence
									</th>
									<th>
										Subject
									</th>
									<th>
										Predicate
									</th>
									<th>
										Object
									</th>
								</tr>

								<%
											APredicationList myPredications = (APredicationList) session
											.getAttribute("displayedPredications");

									for (APredication pred : myPredications) {

										switch (pred.source) {
										case MEDLINE:
								%>
								<tr>
									<td>
										<a href="../Search.do;method=search?method=search&PMID=<%= pred.PMID %>" target="_blank>" > <%= pred.PMID %> </a>
										<%
												break;
												case CLINICAL_TRIALS:
										%>

								<tr>
									<td>
										<a
											href="http://clinicaltrials.gov/show/<%="NTCS" + pred.PMID%>"
											target="_blank>"> <%="NTCS" + pred.PMID%> </a>
										<%
												break;
												case OPASI:
										%>
										<tr>
									<td>
											<a href="../Search.do;method=search?method=search&PMID=<%= pred.PMID %>" target="_blank>" > <%= pred.PMID %> </a>
										<%
											}
										%>
									</td>
									<td>
										<c:out value="<%=pred.sentence%>"></c:out>
									</td>
									<td>
										<c:out value="<%=pred.subject%>" />
									</td>
									<td>
										<c:out value="<%=pred.predicate%>"></c:out>
									</td>
									<td>
										<c:out value="<%=pred.object%>"></c:out>
									</td>
								</tr>
								<%
								}
								%>
							</table>

						</c:if>
					</div>

				</logic:present>
			</html:form>
		</div>
		<jsp:include page="/jsp/footer.jsp" />
</body>
</html:html>
