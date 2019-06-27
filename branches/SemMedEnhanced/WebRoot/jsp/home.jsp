<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html:html locale="true">
  <head>
    <html:base />
    <title>SemMed - Home</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
    <meta http-equiv="description" content="Home Page">
    <link href="<html:rewrite page="/css/semmed.css" />" rel="stylesheet" type="text/css">
   <script type="text/javascript" src="/scripts/boxover.js" /></script>
	<script type="text/javascript" src="/scripts/search.js"></script>
  </head>
  <body>
  <jsp:include page="/jsp/header.jsp"/>
  <br>
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
	<p>Semantic MEDLINE is a prototype Web application that summarizes MEDLINE citations returned by a PubMed search. Natural language processing is used to analyze salient content in titles and abstracts.
	This information is then presented in a graph that has links to the MEDLINE text processed.
	<p>Begin at the Search tab by selecting a search; then move to the Summarize tab. Choose a summary type to specify the point of view of the summary (Treatment of Disease, Substance Interactions, Diagnosis, or Pharmacogenomics).
	<p>After selecting a UMLS concept, click the Summarize and Visualize button. The graph appears in the next page.
  </div>
  <jsp:include page="/jsp/footer.jsp"/>
</body>
<script type="text/javascript">
</script>
</html:html>