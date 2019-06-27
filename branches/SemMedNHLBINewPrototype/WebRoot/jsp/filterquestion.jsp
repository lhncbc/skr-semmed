<%@ page language="java" pageEncoding="UTF-8"
	import="gov.nih.nlm.semmed.model.*,java.util.List"%>
<%@ taglib  uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
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
	<title>SemMed - Filter Question</title>
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
			<span class="left"><strong><a href="../Welcome.do"><span
					class="center">Questions</span> </a></strong> </span>
		</li>
		<li>
			<span class="left"><a href="../InitializeSearch.do"><span
					class="center">Search</span> </a> </span>
		</li>
		<li>
			<span class="left"><a
					href="../InitializeQuestion.do"><span class="center">Question
							Relevance</span> </a></span>
		</li>
	</ul>

	<br>

	<div id="content">
		<html:form action="FilterQuestion" method="post" enctype="multipart/form-data" >
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
			<logic:present role="SemMedTester">
				<table>
					<tr>
						<td>
							Upload Rule File
						</td>
						<td colspan ="4">
							<html:file property="uploadRuleFile" />
						</td>
						<td>
							<html:submit property="method">
								<bean:message key="question.button.uploadRule" />
							</html:submit>
						</td>
					</tr>
				</table>
			</logic:present>
			<br />
			<div>
			Select a question:
			</div>
			<div style="border:solid 1px">
			<div class="checklist">
			<c:if test="${questionNumber != null}">
				Question selected:
				<div style="border:1px solid black;background-color:yellow">
					<c:out value="${chosenQuestion}"/>
				</div>
			 <logic:present role="SemMedTester">
				<br/>
				Rule instantiated:
				<div style="border:1px solid black;background-color:yellow">
					<c:out value="${rules}"/>
				</div>
			    <br/>
			</logic:present>
			</c:if>
				<c:set var="itemCounter" value="${0}" />
				<c:set var="class" value="odd" />
				<c:catch var="exception">
				<logic:iterate id="o" name="questions">
					<div class="${class}">
						<c:set var="level" value="${o.label}" />
						<c:set var="value" value="${o.value}" />
						<% int iCounter = ((Long) pageContext.getAttribute("itemCounter")).intValue();
						   if(pageContext.getAttribute("level") != null && ((String) pageContext.getAttribute("level")).compareTo("") != 0 && ((String) pageContext.getAttribute("value")).compareTo("*") != 0) {
						   	if (session.getAttribute("questionNumber")!=null && (((Integer)session.getAttribute("questionNumber")).intValue()==
						         ((Long)pageContext.getAttribute("itemCounter")).intValue())) {%>
								<input name="questionNumber" value="${itemCounter}" type="radio" checked>
						<%}else{ %>
							<input name="questionNumber" value="${itemCounter}" type="radio">
						<%} %>

						<c:choose>
							<c:when test="${fn:contains(level,'_RiskFactor') and fn:contains(level,'_Disorder')}" >
								<% String[] questionArray = ((String)pageContext.getAttribute("level")).split("_RiskFactor");
								   pageContext.setAttribute("savedArray", questionArray); %>
									<c:choose>
										<c:when test="${fn:contains(savedArray[0],'_Disorder')}" >
											<% String[] nestedArray1 = questionArray[0].split("_Disorder"); %>
											<%= nestedArray1[0] %>
											<bean:define id="typeList" name="disorderTypes" scope="application"
												type="java.util.ArrayList" />
											<bean:define id="dtype" name="defDisorderType" scope="application"
												type="java.lang.String" />
											<c:choose>
												<c:when test="${questionNumber == itemCounter}">
													<html:select property="disorder[]" value="${disorder}">
														<html:options collection="typeList" property="label" labelProperty="value" />
													</html:select>
												</c:when>
												<c:otherwise>
													<html:select property="disorder[]" value="<%=dtype%>">
														<html:options collection="typeList" property="label" labelProperty="value" />
													</html:select>
												</c:otherwise>
											</c:choose>
											<%= nestedArray1[1] %>
										</c:when>
										<c:otherwise>
											<%= questionArray[0] %>

										</c:otherwise>
									</c:choose>
									<bean:define id="typeList" name="riskFactorTypes" scope="application"
										type="java.util.ArrayList" />
									<bean:define id="rtype" name="defRiskFactorType" scope="application"
										type="java.lang.String" />
									<c:choose>
										<c:when test="${questionNumber == itemCounter}">
											<html:select property="riskFactor[]" value="${riskFactor}">
												<html:options collection="typeList" property="label" labelProperty="value" />
											</html:select>
										</c:when>
										<c:otherwise>
											<html:select property="riskFactor[]" value="<%=rtype%>">
												<html:options collection="typeList" property="label" labelProperty="value" />
											</html:select>
										</c:otherwise>
									</c:choose>
									<c:choose>
										<c:when test="${fn:contains(savedArray[1],'_Disorder')}" >
											<% String[] nestedArray2 = questionArray[1].split("_Disorder"); %>
											<%= nestedArray2[0] %>
											<bean:define id="typeList" name="disorderTypes" scope="application"
												type="java.util.ArrayList" />
											<bean:define id="dtype" name="defDisorderType" scope="application"
												type="java.lang.String" />
											<c:choose>
												<c:when test="${questionNumber == itemCounter}">
													<html:select property="disorder[]" value="${disorder}">
														<html:options collection="typeList" property="label" labelProperty="value" />
													</html:select>
												</c:when>
												<c:otherwise>
													<html:select property="disorder[]" value="<%=dtype%>">
														<html:options collection="typeList" property="label" labelProperty="value" />
													</html:select>
												</c:otherwise>
											</c:choose>
											<%= nestedArray2[1] %>
										</c:when>
										<c:otherwise>
											<%= questionArray[1] %>
										</c:otherwise>
									</c:choose>
							</c:when>
							<c:when test="${fn:contains(level,'_RiskFactor')}" >
								<% String[] questionArray = ((String)pageContext.getAttribute("level")).split("_RiskFactor"); %>
								<%= questionArray[0] %>
								<bean:define id="typeList" name="riskFactorTypes" scope="application"
									type="java.util.ArrayList" />
								<bean:define id="rtype" name="defRiskFactorType" scope="application"
								type="java.lang.String" />
								<c:choose>
									<c:when test="${questionNumber == itemCounter}">
										<html:select property="riskFactor[]" value="${riskFactor}">
											<html:options collection="typeList" property="label" labelProperty="value" />
										</html:select>
									</c:when>
									<c:otherwise>
										<html:select property="riskFactor[]" value="<%=rtype%>">
											<html:options collection="typeList" property="label" labelProperty="value" />
										</html:select>
									</c:otherwise>
								</c:choose>
								<%= questionArray[1] %>
								<input type="hidden" name="disorder[]" value="" />
							</c:when>
							<c:when test="${fn:contains(level,'_Disorder')}" >
								<% String[] questionArray = ((String)pageContext.getAttribute("level")).split("_Disorder"); %>
								<%= questionArray[0] %>
								<bean:define id="typeList" name="disorderTypes" scope="application"
									type="java.util.ArrayList" />
								<bean:define id="dtype" name="defDisorderType" scope="application"
								type="java.lang.String" />
								<c:choose>
									<c:when test="${questionNumber == itemCounter}">
										<html:select property="disorder[]" value="${disorder}">
											<html:options collection="typeList" property="label" labelProperty="value" />
										</html:select>
									</c:when>
									<c:otherwise>
										<html:select property="disorder[]" value="<%=dtype%>">
											<html:options collection="typeList" property="label" labelProperty="value" />
										</html:select>
									</c:otherwise>
								</c:choose>
								<%= questionArray[1] %>
								<input type="hidden" name="riskFactor[]" value="" />
							</c:when>
							<c:otherwise>
								<c:out value="${level}"/>
								<input type="hidden" name="riskFactor[]" value="" />
								<input type="hidden" name="disorder[]" value="" />
							</c:otherwise>
						</c:choose>
					</div>
					<c:if test="${itemCounter%2==0}">
						<c:set var="class" value="ev" />
					</c:if>
					<c:if test="${itemCounter%2!=0}">
						<c:set var="class" value="odd" />
					</c:if>
					<c:set var="itemCounter" value="${itemCounter+1}" />
				<%}else{ %>
					<div>
						<b><c:out value="${level}"/></b>&nbsp; &nbsp; &nbsp;
					</div>
				<%} %>
				</logic:iterate>
				</c:catch>
			</div>
			</div>
<br/>

			 <table>

				<tr>
					<td>
						<html:submit property="method">
								<bean:message key="question.button.select" />
							</html:submit>
					</td>
					<logic:present role="SemMedTester">
					<td>	<html:submit property="method">
								<bean:message key="question.button.selectTest" />
							</html:submit>
					</td>
					</logic:present>
				</tr>
			</table>
		</html:form>
	</div>
	<jsp:include page="/jsp/footer.jsp" />
</body>
				<script type="text/javascript">
					tab('<%=session.getAttribute("currentQuestionDisplay")%>');
				</script>

</html:html>
