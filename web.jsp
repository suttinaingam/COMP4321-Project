<%@ page import="java.util.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="Search.Search" %>
<%@ page import="Crawler.Crawler" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<html>
<body>
<%
if(request.getParameter("query")!=null)
{
    out.println("The words you entered are:");
    out.println("<hr>");
    String[] tokens = request.getParameter("query").split(" ");
    for (int i = 0; i < tokens.length; i++){
        out.println(tokens[i]);
        out.println("<br>");
    }
    String query = request.getParameter("query");
    Search search = new Search();
    ArrayList<String> pages = search.retrieval_fun(query);
    out.println(pages);
}
else
{
	out.println("You input nothing");
}

%>
</body>
</html>
