<%@ page language="java" pageEncoding="UTF-8"
	import="gov.nih.nlm.semmed.model.*,java.util.List"%>

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
	<script type="text/javascript" src="../scripts/question.js"></script>
	<script type="text/javascript" src="../scripts/boxover.js" /></script>
</head>
<body>

	<jsp:include page="/jsp/header.jsp" />
	<div align="right" >
		<a href="SMNHLBI_documentation.pdf">SemMedNHLBI Documentation</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	</div>
	<ul class="tabs" id="mytabs">
		<li>
			<span class="left"><a href="../Welcome.do"><span
					class="center">Questions</span> </a> </span>
		</li>
		<li>
			<span class="left"><a href="../InitializeSearch.do"><span
					class="center">Search</span> </a> </span>
		</li>
		<li>
			<span class="left"><strong><a
					href="../InitializeQuestion.do"><span class="center">Question
							Relevance</span> </a></strong></span>
		</li>
	</ul>

	<br>

	<div id="content">
		<html:form action="Question"  method="post" enctype="multipart/form-data">
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
								<logic:equal name="source" value="ctrials">
									<b>Source:</b> clinicaltrials.gov,
										<b>Maximum Rank:</b>
									<%=((Query) session.getAttribute("query"))
							.getOption("clinicalTrialMax")%>,
										<b>Include trials that are no longer recruiting:</b> Yes,
										<b><%=((int[]) session
							.getAttribute("citationIDsClinicalTrials")).length%> </b> citations retrieved.)
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
									.getAttribute("citationIDsMedline")).length%> </b> citations retrieved.)
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
									.getAttribute("citationIDsMedline")).length%> </b> citations retrieved.)
									</logic:equal>
							</logic:present>
						</td>
					</tr>
				</logic:present>
			</table>
			Question selected:
			<div style="border:1px solid black;background-color:yellow">
					<c:out value="${chosenQuestion}"/>

			</div>
			<br/>
			<br />
			 <table>
					<tr>
						<td>
							<html:submit property="method">
								<bean:message key="question.button.findcitationfromdb" />
							</html:submit>
							&nbsp; &nbsp; OR
						</td>
						</tr>
						<tr>
						<td colspan ="3">
							 Locate Include file &nbsp; &nbsp;
							<html:file property="uploadIncludeFile" />
						</td>
						<td colspan ="3">
							 Locate Exclude file &nbsp; &nbsp;
							<html:file property="uploadExcludeFile" />
						</td>
						<td>
							<html:submit property="method">
								<bean:message key="question.button.uploadincludeexclude" />
							</html:submit>
						</td>
					</tr>
			</table>
		</html:form>

			<logic:present name="relevantQuestionrCitations" scope="session">
				<html:form action="Visualize" method="post">
					<table>
						<tr>
							<td>
								<html:submit property="method">
								<bean:message key="visualize.button.processTest" />
							</html:submit>
							</td>
							<td>
								<input type="checkbox" name="saliency" value="off"/>More relations.
							</td>
						</tr>

						<tr>
						  <td>
							<html:submit property="method">
								<bean:message key="visualize.button.exportCitationToXml" />
							</html:submit>
							</td>
						</tr>
				 </table>
				</html:form>
				<br>
				<div style="float:left">
					<ul class="tabs" style="margin-left:-10px">
						<li>
							<span class="left"><a href="javascript:void(null);"
								onclick="tab('rCitations')"><span class="center"
									id="rcTab"
									>Relevant
										Citations</span> </a> </span>
						</li>
						<li>
							<span class="left"><a href="javascript:void(null);"
								onclick="tab('rPredications')"><span class="center"
									id="rpTab">Relevant Predications</span> </a> </span>
						</li>
					</ul>
				</div>
				<div style="float:right">
					<ul class="tabs" style="margin-right:20px">
						<li>
							<span class="left"><a href="javascript:void(null);"
								onclick="tab('nrCitations')"><span class="center" id="nrcTab">Nonrelevant
										Citations</span> </a> </span>
						</li>
						<li>
							<span class="left"><a href="javascript:void(null);"
								onclick="tab('nrPredications')"><span class="center"
									id="nrpTab">Nonrelevant Predications</span> </a> </span>
						</li>
					</ul>
				</div>

				<%
							String[] tabs = new String[] { "rCitations", "rPredications",
							"nrCitations", "nrPredications" };

					for (String tabName : tabs) {

						//if (session.getAttribute("displayedQuestion" + tabName) == null)
						//	continue;

						int count = ((List) session.getAttribute("relevantQuestion"
						+ tabName)).size();
						int pageNumber = 0;
						if (session.getAttribute("pageNumber" + tabName) != null)
							pageNumber = (Integer) session.getAttribute("pageNumber"
							+ tabName);
				%>


				<div class="searchResults" id="<%=tabName%>">
					<br>
					Found
					<%=count%>
					items.
					<c:if test="${predsource == 1}">
						<ale:index count="<%=count%>" currentPage="<%=pageNumber%>"
						link="<%="../Question.do;method=findcitation?method=findcitation&p={}&display="
								+ tabName%>" />
					</c:if>
					<c:if test="${predsource == 2}">
						<ale:index count="<%=count%>" currentPage="<%=pageNumber%>"
											link="<%="../Question.do;method=findcitationfromdb?method=findcitationfromdb&p={}&display="
								+ tabName%>" />
					</c:if>
					<c:set var="count" value="<%=count%>" />

					<c:if test="${count>0}">

						<table id="pubmed" rules="rows">

							<%
							if (tabName.endsWith("Predications")) {
							%>
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
									.getAttribute("displayed" + tabName);

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
							} else {
							%>
								<tr><th>Citations</th>
								<th>Quality of Evidence</th>
								<th>Impact Factor</th>
								<th></th>
								</tr>
							<%
								List myCitations = (List) session.getAttribute("displayed"
									+ tabName);
								List<APredication> sPredications = null;
								if (tabName.compareTo("rCitations") == 0)
									sPredications = (List<APredication>) session.getAttribute("relevantQuestionrPredications");
								else if (tabName.compareTo("nrCitations") == 0)
									sPredications = (List<APredication>) session.getAttribute("relevantQuestionnrPredications");

								LitePredicationList liteList = new LitePredicationList(sPredications);

								for (Object o : myCitations) {
									if (o instanceof PubmedArticle){
										PubmedArticle doc = (PubmedArticle)o;
							%>
									<tr>
										<td>
											<a
												href="http://ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=AbstractPlus&list_uids=<%=doc.getId()%>"
												target="_blank>"> <%=doc.getAuthorList()%>
											</a>
											<br>
											<c:out value="<%=doc.getTitleText()%>" />
											<br>
											<%=doc.getId()%>
										</td>
										<td><c:out value="<%=doc.getQualityEvidence()%>" /></td>
										<td><c:out value="<%=doc.getImpactFactor()%>" /></td>
										<td>
											<a href="javascript:void(0)" onclick="addBox('<%=doc.getAbstractText().replaceAll("'","\\\\'").replaceAll("\"","\\\\'")%>')">Abstract</a>
										</td>
										<logic:present role="SemMedTester">
											<td>
												<a href="javascript:void(0)" onclick="addPredicationBox('<%=liteList.getSubList(doc.getId()).toHTMLString()%>')">Predications</a>
											</td>
										</logic:present>
									</tr>

									<%
									}
								}
							}
							%>

						</table>

					</c:if>


				</div>

				<%
				}
				%>
		</logic:present>
	</div>
	<jsp:include page="/jsp/footer.jsp" />
</body>
				<script type="text/javascript">
					tab('<%=session.getAttribute("currentQuestionDisplay")%>');
				</script>

</html:html>
