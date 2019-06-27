<%@ page language="java" import="java.util.*,gov.nih.nlm.semmed.model.*"
	pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean"
	prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html"
	prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic"
	prefix="logic"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display"%>
<%@ taglib uri="http://ajaxanywhere.sourceforge.net/" prefix="aa"%>
<%@ taglib uri="pageabletable.tld" prefix="ale"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html:html locale="true">
<head>
	<html:base />

	<title>SemMed - Summarization</title>

	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="Summarization page">
	<link href="<html:rewrite page="/css/semmed.css" />" rel="stylesheet"
		type="text/css">
	<script type="text/javascript"
		src="<html:rewrite page="/scripts/aa.js"/>"></script>
</head>

<body>
		<jsp:include page="/jsp/header.jsp" />
		<ul class="tabs">
		<li>
			<span class="left"><a href="../Welcome.do"><span
						class="center">Search</span> </a></span>
		</li>
		<li>
			<span class="left"><a href="../InitializeSemrep.do"><span
					class="center">SemRep</span> </a> </span>
		</li>
		<li>
			<span class="left"><a href="../InitializeQuestion.do"><span
					class="center">Question Relevance</span> </a> </span>
		</li>
		<li>
			<span class="left"><strong><a href="../InitializeSummary.do"><span
					class="center">Summarization</span> </a></strong> </span>
		</li>
		<li>
			<span class="left"><a href="../InitializeVisualization.do"><span
					class="center">Visualization</span> </a> </span>
		</li>
	</ul>

	<br>
		<div id="content">
			<html:form action="Summary" method="post" focus="file"
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
					<logic:present name="predications" scope="session">
						<tr>
							<td>
								<input type="radio" name="inputType" value="session"
									checked="checked">
								Process Medline abstracts from the current session
								<br>
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
										.getAttribute("citationIDsClinicalTrials")).length%> </b> citations retrieved.,
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
										.getAttribute("citationIDsMedline")).length%> </b> citations retrieved.,
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
									<%= ((List)session.getAttribute("predications")).size() %> predications extracted.)
								</logic:present>
							</td>
						</tr>
					</logic:present>
					<tr>
						<td>
							<input type="radio" name="inputType" value="file">
							Upload File
							<html:file property="uploadFile" />
							<html:submit property="method">
								<bean:message key="summary.button.upload" />
							</html:submit>
						</td>
					</tr>
				</table>
				<%--	    Summarization Filters: --%>
				<table cellspacing="3">
					<tr>
						<td>
							<b>Options:</b>
						</td>
					</tr>
					<tr>
						<bean:define id="summaryTypes" name="summaryTypes"
							scope="application" type="java.util.ArrayList" />
						<bean:define id="sumType" name="defSummaryType"
							scope="application" type="java.lang.String" />
						<logic:present name="summaryType" scope="session">
							<bean:define id="sumType" name="summaryType" scope="session"
								type="java.lang.String" />
						</logic:present>
						<td>
							Summary Type:
							<html:select property="summaryType" value="<%=sumType%>"
								onchange="findRelevant();">
								<html:options collection="summaryTypes" property="label"
									labelProperty="value" />
							</html:select>
						</td>
					</tr>
					<tr>
						<bean:define id="salient" name="defSaliency" scope="application"
							type="java.lang.Boolean" />
						<logic:present name="saliency" scope="session">
							<bean:define id="salient" name="saliency" scope="session"
								type="java.lang.Boolean" />
						</logic:present>
						<td>
							<logic:equal name="salient" value="true">
								<input type="checkbox" name="saliency" checked="checked"
									onclick="ShowHide('opt');">Use Saliency Filter
					</logic:equal>
							<logic:notEqual name="salient" value="true">
								<input type="checkbox" name="saliency"
									onclick="ShowHide('opt');">Use Saliency Filter
					</logic:notEqual>
							&nbsp;&nbsp;&nbsp;Salient Output Type:
							<bean:define id="typeList" name="saliencyTypes"
								scope="application" />
							<bean:define id="type" name="defSaliencyType" scope="application"
								type="java.lang.String" />
							<logic:present name="selectedSaliencyType" scope="session">
								<bean:define id="type" name="selectedSaliencyType"
									scope="session" type="java.lang.String" />
							</logic:present>
							<html:select property="selectedSaliencyType" value="<%=type%>">
								<html:options collection="typeList" property="label"
									labelProperty="value" />
							</html:select>
						</td>
					</tr>
				</table>
				<aa:zone name="relevance">
					<logic:present name="relevantConcs" scope="session">
					Select a UMLS concept to summarize on:<br>
						<bean:define id="relevantConcs" name="relevantConcs"
							scope="session" type="java.util.ArrayList" />
						<bean:define id="selectedSeed" name="selectedSeed" scope="session"
							type="java.lang.String" />
						<html:select property="seed" size="10" value="<%=selectedSeed%>">
							<html:options collection="relevantConcs" property="label"
								labelProperty="value" />
						</html:select>
					</logic:present>
				</aa:zone>
				<div id="">
					<html:submit property="method">
						<bean:message key="summary.button.process" />
					</html:submit>

				</div>
				<logic:present role="SemMedTester">
						<html:submit property="method">
							<bean:message key="summary.button.processTest" />
						</html:submit> <br/>
				</logic:present>



				<logic:present name="summaryPredications" scope="session">
					<html:submit property="method">
						<bean:message key="summary.button.export" />
					</html:submit>
				</logic:present>

					<%
								int count = ((List) session.getAttribute("summaryPredications"))
								.size();
						int pageNumber = 0;
						if (session.getAttribute("pageNumberSummary") != null)
							pageNumber = (Integer) session
							.getAttribute("pageNumberSummary");
					%>

					<div class="searchResults">
						<div>
							<br>
							Found
							<%=count%>
							predications.

							<c:if test="${predsource == 1}">
								<ale:index count="<%=count%>" currentPage="<%=pageNumber%>"
									link="<%="../Summary.do;method=process?method=process&p={}"%>" />
							</c:if>
							<c:if test="${predsource == 2}">
							<ale:index count="<%=count%>" currentPage="<%=pageNumber%>"
									link="<%="../Summary.do;method=processTest?method=processTest&p={}"%>" />
							</c:if>
							<c:set var="count" value="<%=count%>" />

						</div>

						<c:if test="${count>0}">

							<table id="pubmed" rules="rows">
								<tr>
									<th>
										PMID
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
											.getAttribute("displayedSummaryPredications");

									for (APredication pred : myPredications) {

										switch (pred.source) {
										case MEDLINE:
								%>
								<tr>
									<td>
										<a
											href="http://ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=AbstractPlus&list_uids=<%=pred.PMID%>"
											target="_blank>"> <%=pred.PMID%> </a>
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
<script language="javascript" type="text/javascript">
	ajaxAnywhere.formName = "SummaryForm";
	ajaxAnywhere.getZonesToReload = function(url) {
		return "relevance";
	}

	function findRelevant() {
		//ajaxAnywhere.submitAJAX('method=findRelevant');
		//box = document.SummaryForm.summaryType;
		//type = box.options[box.selectedIndex].value;
		//ajaxAnywhere.getAJAX('/SemMed/Summary.do?method=findRelevant&summaryType=' + type);
	   	document.SummaryForm.action = "../Summary.do?method=findRelevant";
	   	document.SummaryForm.submit();
	}

//  	function retrieveRelevantConcs() {
//	   if (document.SummaryForm.relevance.checked) {
//	   	document.SummaryForm.connectivity.checked = true;
//	   //ajaxAnywhere.getAJAX('/SemMed/Summary.do?method=Relevance');
//	   	//document.SummaryForm.action = "/SemMed/Summary.do?method=Relevance";
//	   	//document.SummaryForm.submit();
//	  }
//	  else {
//	  	document.SummaryForm.connectivity.checked = false;
//	  }
// 	 }

	function showHide(Code) {
  		var id = document.getElementById(Code);
	  	id.style.visibility = (id.style.visibility == 'hidden') ?'visible':'hidden';
	}
</script>

</html:html>

