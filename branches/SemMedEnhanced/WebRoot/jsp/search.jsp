
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
		<a href="SemMedDocumentation2.pdf">SemMed Documentation</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	</div>
	<ul class="tabs">
		<li>
			<span class="left"><strong><a href="../jsp/home.jsp"><span
						class="center">Home</span> </a> </strong> </span>
		</li>
		<li>
			<span class="left"><strong><a href="../InitializeSearch.do"><span
						class="center">Search</span> </a> </strong> </span>
		</li>
		<li>
			<span class="left"><a href="../InitializeSummary.do"><span
					class="center">Summarization</span> </a> </span>
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
			<c:catch var="exception">
				<table>
					<logic:present role="SemMedTester">
					<tr>
						<td>
							Upload Citation File
						</td>
						<td colspan ="3">
							<html:file property="uploadCitationFile" />
						</td>
						<td>
							<html:submit property="method">
								<bean:message key="search.button.uploadcitation" />
							</html:submit>

						</td>
					</tr>


					<% if(session.getAttribute("uploadCitationComplete") != null){%>
					<tr>
					<td colspan="6">Upload citation file is completed! </td>
					</tr>
					<% } %>
					<tr>
					<td> &nbsp; &nbsp; </td>
					</tr>
					<tr>
						<td>
							Upload Semrep File
						</td>
						<td colspan ="3">
							<html:file property="uploadSemrepFile" />
						</td>
					</tr>
					<tr>
						<td>
							Email Address:
						</td>
						<td>
							<html:text size="30" property="emailAddress" value="" />
						</td>
						<td>
							<html:submit property="method">
								<bean:message key="search.button.uploadsemrep" />
							</html:submit>
						</td>
					</tr>
					<% if(session.getAttribute("uploadSemrepComplete") != null){%>
					<tr>
					<td colspan="6">Upload semrep file is completed! </td>
					</tr>
					<% } %>
					<tr>
					<td> &nbsp; &nbsp; </td>
					</tr>
				   </logic:present>
					<tr>
						<td colspan="1">
							Search Query:
						</td>
						<bean:define id="term" name="query" property="term"
							scope="session" type="java.lang.String" />


						<td colspan="5" nowrap="nowrap">
							<html:text size="60" property="term" value="<%=term%>" />
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
					<!--  <tr>
						<bean:define id="sources" name="sources" scope="application"
							type="java.util.ArrayList" />
						<bean:define id="source" name="query" property="source"
							scope="session" type="java.lang.String" />
						<td align="right" colspan="1">
							Source:
						</td>
						<td colspan="3" id="selectTD">
							<html:select name="pepe" property="selectedSource"
								value="<%=source%>" onchange="toggleOptions()">
								<html:options collection="sources" property="label"
									labelProperty="value" />
							</html:select>
						</td>
					</tr> -->
					<input type=hidden name="selectedSource" value="medline" >
				</table>
			</c:catch>

			<c:if test="${exception!=null}">
				<script type="text/javascript">alert("Your session has expired.");</script>
				<c:redirect url="Welcome.do">Your session has expired</c:redirect>
			</c:if>

			<!-- <table id="clinicalTrialOptions" cellpadding="3">
				<tr>
					<td>
						<b>Clinical Trial Options:</b>
					</td>
				</tr>
				<tr>
					<td>
						Maximum Rank:
						<bean:define id="maxRankList" name="citCounts" scope="application" />
						<html:select property="selectedMaxRank"
							value="<%=((Query) session.getAttribute("query")).getOption(
							"clinicalTrialMax").toString()%>">
							<html:options collection="maxRankList" property="label"
								labelProperty="value" />
						</html:select>
					</td>
				</tr>
				<tr>
					<td>
						<input type="checkbox" name="recruiting" checked="checked">
						Include trials that are no longer recruiting
					</td>
				</tr>
			</table> -->
			<div id="pubmedOptions">
				<table cellpadding="3">
					<tr>
						<td colspan="4">
							<b>Search Options:</b>
						</td>
					</tr>
					<tr>
						<td colspan="1">
							Most Recent:
						</td>
						<bean:define id="citCountList" name="citCounts"
							scope="application" />
						<td colspan="3">
							<html:select property="selectedCitCount"
								value="<%=((Query) session.getAttribute("query")).getOption(
							"pubmedMax").toString()%>">
								<html:options collection="citCountList" property="label"
									labelProperty="value" />
							</html:select>
						</td>
						<td align="right" colspan="3">
							Start Date:
						</td>
						<td colspan="3">
							<html:text size="10" property="startDate"
								value="<%=((Query) session.getAttribute("query")).getOption(
							"pubmedStartDate").toString()%>" />
						</td>
					</tr>
					<tr>
						<td />
						<td colspan="3" />
						<td align="right" colspan="3">
							End Date:
						</td>
						<td colspan="3">
							<html:text size="10" property="endDate"
								value="<%=((Query) session.getAttribute("query")).getOption(
							"pubmedEndDate").toString()%>" />
						</td>
					</tr>
					<tr>
						<td />
						<td colspan="3" />
						<td align="right" colspan="2" />
						<td>PubMed Filter:</td>
						<td>
							<logic:notPresent name="pubmedExtraOptions" scope="session">
								<form method="POST" action="../Search.do;method=toggleLimits">
								<%-- We are relying in the fact that the other buttons will override the method argument --%>
									<input type="hidden" name="method" value="toggleLimits">
									<input type="submit" name="pubmedExtra" value="Show">

							</logic:notPresent>
							<logic:present name="pubmedExtraOptions" scope="session">
								<input type="hidden" name="method" value="toggleLimits">
								<input type="submit" name="pubmedExtra" value="Hide">
							</logic:present>
						</td>
					</tr>
				</table>
				<logic:present name="pubmedExtraOptions" scope="session">
					<div class="mbar">
						<!--  <p class="limit_header">
							Limit your search by any of the following criteria.
						</p> -->
						<div class="box" id="autsearch"">
							<table class="toptable" border="0" cellspacing="0"
								cellpadding="0">
								<tr>
									<td nowrap="nowrap">
										Search by Author
									</td>
									<td>
										<a href="#"
											onclick="addrow('auth_dd', 'Author'); showhide('AuthBlock', true, '' , 'show'); SetFocus('author_'+newRowsCounter); return false">
											<img
												src="http://www.ncbi.nlm.nih.gov/corehtml/query/PubMed/gifs/addauthor.gif"
												width="69" height="18" class="add"
												title="Add Author to this Search" border="0" /> </a>
									</td>
									<td width="100%">
									</td>
									<td>
										<a href="#"
											onclick="rem_rows('auth_dd'); showhide('AuthBlock', true, '' , 'hide'); return false">
											<img
												src="http://www.ncbi.nlm.nih.gov/corehtml/query/PubMed/gifs/clear.gif"
												class="pin" title="Clear Author Filters" border="0" /> </a>
									</td>
									<td width="100%">
									</td>
								</tr>
							</table>

							<div class="inputlist" id="AuthBlock" style="display: none;">
								<table width="95%" border="0" cellspacing="2" cellpadding="1"
									align="center">
									<tr>
										<td width="70%">
											<div class="ev">
												<h1>
													Author Name
												</h1>
												<dd id="auth_dd">
													<table width="100%">
														<tfoot style="display: none;">
															<tr>
																<td>
																	<div class="boxodd">
																		<input type="text" id="auth" class="text"
																			name="author_" size="40" value="" />
																		<a href="#">remove</a>
																	</div>
																</td>
															</tr>
														</tfoot>
														<tbody>
														</tbody>
													</table>
												</dd>
												<a
													href="javascript:addrow('auth_dd', 'Author'); SetFocus('author_'+newRowsCounter); ">Add
													Another Author</a>
											</div>
										</td>
										<td class="td-separator"></td>
										<td class="radio_authors" valign="top">
											<input name="pmfilter_AuthOp" id="authAll" value="AND"
												checked="checked" type="radio" />
											<label for="authAll">
												All these authors
											</label>
											<br />
											<input name="pmfilter_AuthOp" id="authAny" value="OR"
												type="radio" />
											<label for="authAny">
												Any of these
											</label>
											<br />
										</td>
									</tr>
								</table>
							</div>
						</div>
						<div class="box" id="jousearch">
							<table class="toptable" border="0" cellspacing="0"
								cellpadding="0">
								<tr>
									<td nowrap="nowrap">
										Search by Journal
									</td>
									<td>
										<a href="#"
											onclick="addrow('jour_dd', 'Journal'); showhide('JourBlock', true, '' , 'show'); SetFocus('journal_'+newRowsCounter); return false">
											<img
												src="http://www.ncbi.nlm.nih.gov/corehtml/query/PubMed/gifs/addjournal.gif"
												width="69" height="18" class="add"
												title="Add Journal to this Search" border="0" /> </a>
									</td>
									<td width="100%">
									</td>
									<td>
										<a href="#"
											onclick="rem_rows('jour_dd'); showhide('JourBlock', true, '' , 'hide'); return false">
											<img
												src="http://www.ncbi.nlm.nih.gov/corehtml/query/PubMed/gifs/clear.gif"
												class="pin" title="Clear Journal Filters" border="0" /> </a>
									</td>
									<td width="100%">
									</td>
								</tr>
							</table>

							<div class="inputlist" id="JourBlock" style="display: none;">
								<table width="95%" border="0" cellspacing="2" cellpadding="1"
									align="center">
									<tr>
										<td width="70%">
											<div class="ev">
												<h1>
													Author Name
												</h1>
												<dd id="jour_dd">
													<table width="100%">
														<tfoot style="display: none;">
															<tr>
																<td>
																	<div class="boxodd">
																		<input type="text" id="auth" class="text"
																			name="journal_" size="40" value="" />
																		<a href="#">remove</a>
																	</div>
																</td>
															</tr>
														</tfoot>
														<tbody>
														</tbody>
													</table>
												</dd>
												<a
													href="javascript:addrow('jour_dd', 'Journal'); SetFocus('journal_'+newRowsCounter); ">Add
													Another JOurnal</a>
											</div>
										</td>
										<td class="td-separator"></td>
										<td class="radio_authors" valign="top">
											Results will be from any of these journals.
											<br />
										</td>
									</tr>
								</table>
							</div>
						</div>

						<div class="box" id="simsearch">
							<table class="toptable" border="0" cellspacing="0"
								cellpadding="0">
								<tr>
									<td nowrap="nowrap">
										Full Text, Free Full Text, and Abstracts
									</td>
									<td width="100%">
									</td>
									<td>
										<a href="#" onclick="return setAll('simsearch', false);">
											<img
												src="http://www.ncbi.nlm.nih.gov/corehtml/query/PubMed/gifs/clear.gif"
												class="pin" title="Clear current selections" border="0" />
										</a>
									</td>
									<td width="100%">
									</td>
								</tr>
							</table>
							<div class="inputlist">
								<div class="ev">
									<table border="0" align="center" width="80%">
										<tr>
											<td>
												<span class="citcheckbox"> <c:set var="checked"
														value="<%=((Query) session.getAttribute("query"))
							.getOption("fullText") == Boolean.TRUE%>" />
													<c:choose>
														<c:when test="${checked}">
															<input name="pmfilter_FullText" type="checkbox"
																value="full text" checked="checked" />
														</c:when>
														<c:otherwise>
															<input name="pmfilter_FullText" type="checkbox"
																value="full text" />
														</c:otherwise>
													</c:choose> <label>
														Links to full text
													</label> </span>
											</td>
											<td>
												<span class="citcheckbox"> <c:set var="checked"
														value="<%=((Query) session.getAttribute("query"))
							.getOption("freeFullText") == Boolean.TRUE%>" />
													<c:choose>
														<c:when test="${checked}">
															<input name="pmfilter_FreeFullText" type="checkbox"
																value="free full text" checked="checked" />
														</c:when>
														<c:otherwise>
															<input name="pmfilter_FreeFullText" type="checkbox"
																value="free full text" />
														</c:otherwise>
													</c:choose> <label>
														Links to free full text
													</label> </span>
											</td>
											<td>
												<span class="citcheckbox"> <c:set var="checked"
														value="<%=((Query) session.getAttribute("query"))
							.getOption("hasAbstract") == Boolean.TRUE%>" />
													<c:choose>
														<c:when test="${checked}">
															<input name="pmfilter_HasAbstract" type="checkbox"
																value="abstracts" checked="checked" />
														</c:when>
														<c:otherwise>
															<input name="pmfilter_HasAbstract" type="checkbox"
																value="abstracts" />
														</c:otherwise>
													</c:choose> <label>
														Abstracts
													</label> </span>
											</td>
										</tr>
									</table>
								</div>
							</div>
						</div>
						<div class="cleaner"></div>
						<table border="0" cellspacing="2" cellpadding="1" width="650"
							id="checkbox_filters">
							<tr>
								<td valign="top">
									<!-- === Humans or Animals === -->
									<div class="sbar">
										<div class="box" id="hum_ani">
											<table class="toptable" border="0" cellspacing="0"
												cellpadding="0">
												<tr>
													<td nowrap="nowrap">
														Humans or Animals
													</td>
													<td width="100%">
													</td>
													<td>
														<a href="#" onclick="return setAll('hum_ani', false);">
															<img
																src="http://www.ncbi.nlm.nih.gov/corehtml/query/PubMed/gifs/clear.gif"
																class="pin" title="Clear current selections" border="0" />
														</a>
													</td>
													<td width="100%">
													</td>
												</tr>
											</table>
											<div class="checklistSmall">
												<div class="odd">
													<span class="citcheckbox"> <c:set var="checked"
															value="<%=((Query) session.getAttribute("query"))
							.getOption("humans") == Boolean.TRUE%>" />
														<c:choose>
															<c:when test="${checked}">
																<input name="pmfilter_StudyH" type="checkbox"
																	value="Humans" checked="checked" />
															</c:when>
															<c:otherwise>
																<input name="pmfilter_StudyH" type="checkbox"
																	value="Humans" />
															</c:otherwise>
														</c:choose> <label>
															Humans
														</label> </span>
													<span class="citcheckbox"> <c:set var="checked"
															value="<%=((Query) session.getAttribute("query"))
							.getOption("animals") == Boolean.TRUE%>" />
														<c:choose>
															<c:when test="${checked}">
																<input name="pmfilter_StudyA" type="checkbox"
																	value="Animals" checked="checked" />
															</c:when>
															<c:otherwise>
																<input name="pmfilter_StudyA" type="checkbox"
																	value="Animals" />
															</c:otherwise>
														</c:choose> <label>
															Animals
														</label> </span>
												</div>
											</div>
										</div>
										<div class="cleaner"></div>
									</div>
									<!--end Humans or Animals-->
								</td>
								<td>
									<!-- === Gender === -->
									<div class="sbar">
										<div class="box" id="gender">
											<table class="toptable" border="0" cellspacing="0"
												cellpadding="0">
												<tr>
													<td nowrap="nowrap">
														Gender
													</td>
													<td width="100%">
													</td>
													<td>
														<a href="#" onclick="return setAll('gender', false);">
															<img
																src="http://www.ncbi.nlm.nih.gov/corehtml/query/PubMed/gifs/clear.gif"
																class="pin" title="Clear current selections" border="0" />
														</a>
													</td>
													<td width="100%">
													</td>
												</tr>
											</table>
											<div class="checklistSmall">
												<div class="odd">
													<span class="citcheckbox"> <c:set var="checked"
															value="<%=((Query) session.getAttribute("query"))
									.getOption("male") == Boolean.TRUE%>" />
														<c:choose>
															<c:when test="${checked}">
																<input name="pmfilter_SexM" type="checkbox" value="Male"
																	checked="checked" />
															</c:when>
															<c:otherwise>
																<input name="pmfilter_SexM" type="checkbox" value="Male" />
															</c:otherwise>
														</c:choose> <label>
															Male
														</label> </span>
													<span class="citcheckbox"> <c:set var="checked"
															value="<%=((Query) session.getAttribute("query"))
							.getOption("female") == Boolean.TRUE%>" />
														<c:choose>
															<c:when test="${checked}">
																<input name="pmfilter_SexF" type="checkbox"
																	value="Female" checked="checked" />
															</c:when>
															<c:otherwise>
																<input name="pmfilter_SexF" type="checkbox"
																	value="Female" />
															</c:otherwise>
														</c:choose> <label>
															Female
														</label> </span>
												</div>
											</div>
										</div>
										<div class="cleaner"></div>
									</div>
									<!--end Gender-->
								</td>
							</tr>
							<tr>
								<td>
									<!-- === Languages === -->
									<div class="sbar">
										<div class="box" id="lang">
											<table class="toptable" border="0" cellspacing="0"
												cellpadding="0">
												<tr>
													<td nowrap="nowrap">
														Languages
													</td>
													<td width="100%">
													</td>
													<td>
														<a href="#" onclick="return setAll('lang', false);"> <img
																src="http://www.ncbi.nlm.nih.gov/corehtml/query/PubMed/gifs/clear.gif"
																class="pin" title="Clear current selections" border="0" />
														</a>
													</td>
													<td width="100%">
													</td>
												</tr>
											</table>
											<div class="checklist">
												<c:set var="itemCounter" value="0" />
												<c:set var="class" value="ev" />
												<logic:iterate id="o" name="pubmedLanguages">
													<c:set var="itemCounter" value="${itemCounter+1}" />
													<c:set var="checked"
														value="<%=((Query) session.getAttribute("query"))
								.getOption("languages") != null
						&& ((List) ((Query) session.getAttribute("query"))
								.getOption("languages"))
								.contains(((LabelValueBean) o).getLabel())%>" />
													<div class="${class}">
														<c:choose>
															<c:when test="${checked}">
																<input
																	name="pmfilter_Language_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.label}"/>"
																	checked="${checked}" />
															</c:when>
															<c:otherwise>
																<input
																	name="pmfilter_Language_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.label}"/>" />

															</c:otherwise>
														</c:choose>
														<label>
															<c:out value="${o.label}" />
														</label>
													</div>
													<c:if test="${itemCounter%2==0}">
														<c:set var="class" value="ev" />
													</c:if>
													<c:if test="${itemCounter%2!=0}">
														<c:set var="class" value="odd" />
													</c:if>
												</logic:iterate>
												<h5>
													More Languages
												</h5>
												<c:set var="itemCounter" value="0" />
												<c:set var="class" value="ev" />
												<logic:iterate id="o" name="pubmedExtraLanguages">
													<c:set var="itemCounter" value="${itemCounter+1}" />
													<c:set var="checked"
														value="<%=((Query) session.getAttribute("query"))
								.getOption("languages") != null
						&& ((List) ((Query) session.getAttribute("query"))
								.getOption("languages"))
								.contains(((LabelValueBean) o).getLabel())%>" />
													<div class="${class}">
														<c:choose>
															<c:when test="${checked}">
																<input
																	name="pmfilter_Language_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.label}"/>"
																	checked="${checked}" />
															</c:when>
															<c:otherwise>
																<input
																	name="pmfilter_Language_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.label}"/>" />

															</c:otherwise>
														</c:choose>
														<label>
															<c:out value="${o.label}" />
														</label>
													</div>
													<c:if test="${itemCounter%2==0}">
														<c:set var="class" value="ev" />
													</c:if>
													<c:if test="${itemCounter%2!=0}">
														<c:set var="class" value="odd" />
													</c:if>
												</logic:iterate>
											</div>
											<div class="cleaner">
											</div>
										</div>
										<!--end Languages-->
									</div>
								</td>
								<td>
									<!-- === Subsets === -->
									<div class="sbar">
										<div class="box" id="subs">
											<h4>
												<a href="#" onclick="return setAll('subs', false);"><img
														src="http://www.ncbi.nlm.nih.gov/corehtml/query/PubMed/gifs/clear.gif"
														class="pin" title="Clear current selections" border="0" />
												</a>Subsets
											</h4>
											<div class="checklist">
												<h5>
													Journal Groups
												</h5>
												<c:set var="itemCounter" value="0" />
												<c:set var="class" value="ev" />
												<logic:iterate id="o" name="pubmedJournalSubsets">
													<c:set var="itemCounter" value="${itemCounter+1}" />
													<c:set var="checked"
														value="<%=((Query) session.getAttribute("query"))
								.getOption("journalSets") != null
						&& ((List) ((Query) session.getAttribute("query"))
								.getOption("journalSets"))
								.contains(((LabelValueBean) o).getValue())%>" />
													<div class="${class}">
														<c:choose>
															<c:when test="${checked}">
																<input
																	name="pmfilter_JournalSubsets_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>"
																	checked="${checked}" />
															</c:when>
															<c:otherwise>
																<input
																	name="pmfilter_JournalSubsets_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>" />
															</c:otherwise>
														</c:choose>
														<label>
															<c:out value="${o.label}" />
														</label>
													</div>
													<c:if test="${itemCounter%2==0}">
														<c:set var="class" value="ev" />
													</c:if>
													<c:if test="${itemCounter%2!=0}">
														<c:set var="class" value="odd" />
													</c:if>
												</logic:iterate>
												<h5>
													Topics
												</h5>
												<c:set var="itemCounter" value="0" />
												<c:set var="class" value="ev" />
												<logic:iterate id="o" name="pubmedTopicSubsets">
													<c:set var="itemCounter" value="${itemCounter+1}" />
													<c:set var="checked"
														value="<%=((Query) session.getAttribute("query"))
								.getOption("topicSets") != null
						&& ((List) ((Query) session.getAttribute("query"))
								.getOption("topicSets"))
								.contains(((LabelValueBean) o).getValue())%>" />
													<div class="${class}">
														<c:choose>
															<c:when test="${checked}">
																<input
																	name="pmfilter_TopicSubsets_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>"
																	checked="${checked}" />
															</c:when>
															<c:otherwise>
																<input
																	name="pmfilter_TopicSubsets_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>" />
															</c:otherwise>
														</c:choose>
														<label>
															<c:out value="${o.label}" />
														</label>
													</div>
													<c:if test="${itemCounter%2==0}">
														<c:set var="class" value="ev" />
													</c:if>
													<c:if test="${itemCounter%2!=0}">
														<c:set var="class" value="odd" />
													</c:if>
												</logic:iterate>
												<h5>
													More Subsets
												</h5>
												<logic:iterate id="o" name="pubmedExtraSubsets">
													<c:set var="itemCounter" value="${itemCounter+1}" />
													<c:set var="checked"
														value="<%=((Query) session.getAttribute("query"))
								.getOption("topicSets") != null
						&& ((List) ((Query) session.getAttribute("query"))
								.getOption("topicSets"))
								.contains(((LabelValueBean) o).getValue())%>" />
													<div class="${class}">
														<c:choose>
															<c:when test="${checked}">
																<input
																	name="pmfilter_ExtraSubsets_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>"
																	checked="${checked}" />
															</c:when>
															<c:otherwise>
																<input
																	name="pmfilter_ExtraSubsets_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>" />
															</c:otherwise>
														</c:choose>
														<label>
															<c:out value="${o.label}" />
														</label>
													</div>
													<c:if test="${itemCounter%2==0}">
														<c:set var="class" value="ev" />
													</c:if>
													<c:if test="${itemCounter%2!=0}">
														<c:set var="class" value="odd" />
													</c:if>
												</logic:iterate>
											</div>
										</div>
										<div class="cleaner"></div>
									</div>
									<!--end Subsets-->
								</td>
							</tr>
							<tr>
								<td>
									<!-- === Type of Article === -->
									<div class="sbar">
										<div class="box" id="pubt">
											<h4>
												<a href="#" onclick="return setAll('pubt', false);"><img
														src="http://www.ncbi.nlm.nih.gov/corehtml/query/PubMed/gifs/clear.gif"
														class="pin" title="Clear current selections" border="0" />
												</a>Type of Article
											</h4>
											<div class="checklist">
												<c:set var="itemCounter" value="0" />
												<c:set var="class" value="ev" />
												<logic:iterate id="o" name="pubmedArticleTypes">
													<c:set var="itemCounter" value="${itemCounter+1}" />
													<c:set var="checked"
														value="<%=((Query) session.getAttribute("query"))
								.getOption("publications") != null
						&& ((List) ((Query) session.getAttribute("query"))
								.getOption("publications"))
								.contains(((LabelValueBean) o).getValue())%>" />
													<div class="${class}">
														<c:choose>
															<c:when test="${checked}">
																<input
																	name="pmfilter_PubType_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>"
																	checked="${checked}" />
															</c:when>
															<c:otherwise>
																<input
																	name="pmfilter_PubType_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>" />
															</c:otherwise>
														</c:choose>
														<label>
															<c:out value="${o.label}" />
														</label>
													</div>
													<c:if test="${itemCounter%2==0}">
														<c:set var="class" value="ev" />
													</c:if>
													<c:if test="${itemCounter%2!=0}">
														<c:set var="class" value="odd" />
													</c:if>
												</logic:iterate>
												<h5>
													More Publication Types
												</h5>
												<logic:iterate id="o" name="pubmedExtraArticleTypes">
													<c:set var="itemCounter" value="${itemCounter+1}" />
													<c:set var="checked"
														value="<%=((Query) session.getAttribute("query"))
								.getOption("publications") != null
						&& ((List) ((Query) session.getAttribute("query"))
								.getOption("publications"))
								.contains(((LabelValueBean) o).getValue())%>" />
													<div class="${class}">
														<c:choose>
															<c:when test="${checked}">
																<input
																	name="pmfilter_PubType_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>"
																	checked="${checked}" />
															</c:when>
															<c:otherwise>
																<input
																	name="pmfilter_PubType_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>" />
															</c:otherwise>
														</c:choose>
														<label>
															<c:out value="${o.label}" />
														</label>
													</div>
													<c:if test="${itemCounter%2==0}">
														<c:set var="class" value="ev" />
													</c:if>
													<c:if test="${itemCounter%2!=0}">
														<c:set var="class" value="odd" />
													</c:if>
												</logic:iterate>
											</div>
										</div>
										<div class="cleaner"></div>
									</div>
									<!--end Type of Article-->
								</td>
								<td>
									<!-- === Ages === -->
									<div class="sbar">
										<div class="box" id="ages">
											<h4>
												<a href="#" onclick="return setAll('ages', false);"><img
														src="http://www.ncbi.nlm.nih.gov/corehtml/query/PubMed/gifs/clear.gif"
														class="pin" title="Clear current selections" border="0" />
												</a>Ages
											</h4>
											<div class="checklist">
												<c:set var="itemCounter" value="0" />
												<c:set var="class" value="ev" />
												<logic:iterate id="o" name="pubmedAges">
													<c:set var="itemCounter" value="${itemCounter+1}" />
													<c:set var="checked"
														value="<%=((Query) session.getAttribute("query"))
								.getOption("ages") != null
						&& ((List) ((Query) session.getAttribute("query"))
								.getOption("ages"))
								.contains(((LabelValueBean) o).getValue())%>" />
													<div class="${class}">
														<c:choose>
															<c:when test="${checked}">
																<input name="pmfilter_Age_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>"
																	checked="${checked}" />
															</c:when>
															<c:otherwise>
																<input name="pmfilter_Age_<c:out value="${o.value}"/>"
																	type="checkbox" value="<c:out value="${o.value}"/>" />
															</c:otherwise>
														</c:choose>
														<label>
															<c:out value="${o.label}" />
														</label>
													</div>
													<c:if test="${itemCounter%2==0}">
														<c:set var="class" value="ev" />
													</c:if>
													<c:if test="${itemCounter%2!=0}">
														<c:set var="class" value="odd" />
													</c:if>
												</logic:iterate>
											</div>
										</div>
										<div class="cleaner"></div>
									</div>
									<!--end Ages-->
								</td>
							</tr>
						</table>
					</div>
				</logic:present>


			</div>

			<logic:present name="displayedCitations" scope="session">
				<br>
				<%
				String[] sourceNames = new String[] { "Medline", "ClinicalTrials" };
				%>
				<logic:equal name="source" value="medline">
					<ul class="tabs" style="margin-left:-10px;">
						<li>
							<span class="left"><strong><a href="javascript:void(null);"><span
								class="center">Medline Citations</span> </a> </strong> </span>
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

							if (session.getAttribute("displayedCitations" + sourceName) == null) {
								continue;
							}
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
							<div>
								<ale:index count="<%=count%>" currentPage="<%=pageNumber%>"
									link="<%="../Search.do;method=search?method=search&s="
						+ sourceName + "&p={}"%>" />

								<c:set var="count" value="<%=count%>" />

							</div>
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
