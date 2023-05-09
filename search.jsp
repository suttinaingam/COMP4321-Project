<%@ page import="java.util.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="Search.Search" %>
<%@ page import="Crawler.Crawler" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.*" %>
<%@ page import="jdbm.RecordManager" %>
<%@ page import="jdbm.htree.HTree" %>
<%@ page import="jdbm.RecordManagerFactory" %>
<%@ page import="jdbm.helper.FastIterator" %>
<%@ page import="StopStem.StopStem" %>

<link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
/>

    <!-- Latest compiled JavaScript -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>

<html>
<body>
<h1>Search Engine</h1>
<form name = "search" method="post" action="web.jsp"> 
<input type="text" name="query"> 
<button type="submit"> Enter </button>
</form>
<form name = "crawl" id = "crawl" method="post" action="search.jsp"> 
<input type="text" name="link" id="link"> 
<button type="submit" onclick="crawl()">Crawl</button>
</form>

</body>
</html>

<% String link = request.getParameter("link");
if (link != ""){
    Crawler crawler = new Crawler(link);
    crawler.crawlIndex(link, 300);
}
%>