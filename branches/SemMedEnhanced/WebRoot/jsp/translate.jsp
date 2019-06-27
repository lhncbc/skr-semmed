
<%@ page language="java" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean"
	prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html"
	prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic"
	prefix="logic"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html:html locale="true">
<head>
	<html:base />

	<title>SemMed - Translation</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="Translation page">
	<link href="<html:rewrite page="/css/semmed.css" />" rel="stylesheet"
		type="text/css">
</head>

<body>
	<div id="container">
		<jsp:include page="/jsp/header.jsp" />
		<div id="topnav">
			<ul>
				<li>
					<html:link action="/Welcome">Search</html:link>
				</li>
				<li>
					<html:link action="/InitializeSemrep">SemRep</html:link>
				</li>
				<li>
					<html:link action="/InitializeSummary">Summarization</html:link>
				</li>
				<li>
					<strong><html:link action="/InitializeTranslate">Translation</html:link>
					</strong>
				</li>
				<li>
					<html:link action="/InitializeVisualization">Visualization</html:link>
				</li>
			</ul>
		</div>
		<div id="content">
			<html:form action="Translate" method="post" focus="file" enctype="multipart/form-data">
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
					<logic:present name="summaryPredications" scope="session">
						<tr>
							<td>
								<input type="radio" name="inputType" value="session"
									checked="checked">
								Process SemRep summary predications from the current session
								<br>
								(
								<b>Search Term:</b>
								<bean:write name="query" property="term" scope="session" />
								,
								<bean:define id="source" name="query" property="source"
									scope="session" type="java.lang.String" />
								<logic:equal name="source" value="ctrials">
									<b>Source:</b> clinicaltrials.gov,
									<b>Maximum Rank:</b>
									<bean:write name="query" property="options.maxRank"
										scope="session" />,
	 								<bean:define id="recruiting" name="query"
										property="options.includeNonRecruiting" scope="session"
										type="java.lang.Boolean" />
									<logic:equal name="recruiting" value="true">
										<b>Include trials that are no longer recruiting:</b> Yes,	
									</logic:equal>
									<logic:notEqual name="recruiting" value="true">
										<b>Include trials that are no longer recruiting:</b> No,							
									</logic:notEqual>
								</logic:equal>
								<logic:equal name="source" value="medline">
									<b>Source:</b> Medline,												
									<b>Most Recent:</b>
									<bean:write name="query" property="options.mostRecent"
										scope="session" />,
									<b>Start Date:</b>
									<bean:write name="query" property="options.startDate"
										scope="session" />,
									<b>End Date:</b>
									<bean:write name="query" property="options.endDate"
										scope="session" />,
								</logic:equal>
								<logic:equal name="source" value="both">
									<b>Source:</b> Both sources,
									<b>Medline Most Recent:</b>
									<bean:write name="query"
										property="options.pubmedQueryOptions.mostRecent"
										scope="session" />,
									<b>Medline Start Date:</b>
									<bean:write name="query"
										property="options.pubmedQueryOptions.startDate"
										scope="session" />,
									<b>Medline End Date:</b>
									<bean:write name="query"
										property="options.pubmedQueryOptions.endDate" scope="session" />,	
									<b>Clinical Trials Maximum Rank:</b>
									<bean:write name="query"
										property="options.essieQueryOptions.maxRank" scope="session" />,
		 							<bean:define id="recruiting" name="query"
										property="options.essieQueryOptions.includeNonRecruiting"
										scope="session" type="java.lang.Boolean" />
									<logic:equal name="recruiting" value="true">
										<b>Include trials that are no longer recruiting:</b> Yes,	
									</logic:equal>
									<logic:notEqual name="recruiting" value="true">
										<b>Include trials that are no longer recruiting:</b> No,							
									</logic:notEqual>
								</logic:equal>
								<%--<b>Predication Type:</b> <bean:write name="predicationType" scope="session"/>,--%>
								<b>Summarization Type:</b>
								<bean:write name="summaryType" scope="session" />
								,
								<b>Saliency Filter:</b>
								<bean:write name="saliency" scope="session" />
								,
								<logic:present name="saliency" scope="session">
									<bean:define id="salient" name="saliency" scope="session"
										type="java.lang.Boolean" />
								</logic:present>
								<logic:equal name="salient" value="true">
									<b>Salient Output Type:</b>
									<bean:write name="selectedSaliencyType" scope="session" />,
								</logic:equal>
								<b><c:out
										value="${fn:length(summaryPredications.sentencePredications)}" />
								</b> predications extracted with summarization.)
							</td>
						</tr>
					</logic:present>
					<tr>
						<td>
							<input type="radio" name="inputType" value="file">
							Upload File

							<logic:notPresent name="summaryPredications" scope="session">
								<tr>
									<td>
										<input type="radio" name="inputType" value="file"
											checked="checked">
										Upload File
										
										
									</td> <!--  [Alejandro] -->
								</tr>
							</logic:notPresent>

							<%--<html:file property="uploadFile" style="margin: 0.1em;height: 1.8em;width: 15em;font-size: 1.2em;"/> --%>
							<html:file property="uploadFile" />
							<html:submit property="method">
								<bean:message key="translate.button.upload" />
							</html:submit>
						</td>
					</tr>
					
				</table>  <%--  [Alejandro] --%>	
				<table cellspacing="3">
					<tr>
						<td>
							<b>Options:</b>
						</td>
					</tr>
					<tr>
						<bean:define id="langs" name="languages" scope="application"
							type="java.util.ArrayList" />
						<bean:define id="lang" name="defLanguage" scope="application"
							type="java.lang.String" />
						<logic:present name="lang" scope="session">
							<bean:define id="lang" name="lang" scope="session"
									type="java.lang.String" />
						</logic:present>
						<td>
							Target Language:
							<html:select property="language" value="<%=lang%>">
							<html:options collection="langs" property="label"
									labelProperty="value" />
							</html:select>
						</td>
					</tr>
					<tr>
						<td>
							<html:submit property="method">
								<bean:message key="translate.button.process" />
							</html:submit>
						</td>
					</tr>
				</table>

				<logic:present name="summaryPredications" scope="session">
					<logic:present name="lang" scope="session">
						<html:submit property="method">
							<bean:message key="translate.button.export" />
						</html:submit>
						<div id="displaytag">
							<c:set var="lan" value="${sessionScope.lang}" />
							<c:set var="predicates" value="${applicationScope.predicateMap}" />
							<display:table
									name="sessionScope.summaryPredications.sentencePredications"
									id="row" pagesize="20" sort="list">
								<display:column title="ID"
										decorator="gov.nih.nlm.semmed.util.PubmedLinkDecorator"
										group="1">
									<c:out
											value="${row.sentence.pmid}.${row.sentence.type}.${row.sentence.number}" />
								</display:column>
								<display:column title="SENTENCE" group="2">
									<c:out value="${row.sentence.sentence}" />
								</display:column>
								<display:column title="SUBJECT" sortable="true">
									<c:forEach var="sub" items="${row.predication.predicationArgumentSet}">
										<c:if test="${sub.type == 'S'}">
											<c:set var="sub_tr_found" value="false" />
											<c:forEach var="tr"
												items="${sub.conceptSemtype.concept.conceptTranslationSet}">
												<c:if test="${tr.language == lan}">
													<c:out value="${tr.translation}(${sub.conceptSemtype.semtype})" />
													<br>
													<c:set var="sub_tr_found" value="true" />
												</c:if>
											</c:forEach>
											<c:if test="${sub_tr_found == 'false'}">
												<c:out value="${sub.conceptSemtype.concept.preferredName}(${sub.conceptSemtype.semtype})" />
												<br>
											</c:if>
										</c:if>
									</c:forEach>
								</display:column>
								<c:set var="pred" value="${row.predication.predicate}" />
								<c:set var="predTranslate" value="${predicates[pred]}" />
								<display:column title="PREDICATE" sortable="true">
									<c:out value="${predTranslate.translationMap[lan]}" />
								</display:column>
								<display:column title="OBJECT" sortable="true">
									<c:forEach var="ob"
											items="${row.predication.predicationArgumentSet}">
										<c:if test="${ob.type == 'O'}">
											<c:set var="ob_tr_found" value="false" />
											<c:forEach var="tr"
													items="${ob.conceptSemtype.concept.conceptTranslationSet}">
												<c:if test="${tr.language == lan}">
													<c:out
														value="${tr.translation}(${ob.conceptSemtype.semtype})" />
													<br>
													<c:set var="ob_tr_found" value="true" />
												</c:if>
											</c:forEach>
											<c:if test="${ob_tr_found == 'false'}">
												<c:out
														value="${ob.conceptSemtype.concept.preferredName}(${ob.conceptSemtype.semtype})" />
												<br>
											</c:if>
										</c:if>
									</c:forEach>
								</display:column>
							</display:table>
							<input type="hidden" name="d-16544-p"
									value="<%=request.getParameter("d-16544-p")%>">
						</div>
					</logic:present>
				</logic:present>
			</html:form>
		</div>
		<jsp:include page="/jsp/footer.jsp" />
	</div>
</body>
</html:html>
