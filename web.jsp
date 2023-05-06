<%@ page import="java.util.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="my_package.Test" %>
<%@ page import="my_package.TestProgram" %>
<%@ page import="my_package.Search" %>
<%@ page import="my_package.Crawler.Crawler" %>
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
    out.println(Test.test(tokens));
    out.println(Test.hello_world());
    out.println(Search.test());
    out.println(TestProgram.getWord());
    ArrayList<String> pages = Search.retrieval_fun("computer science");
    out.println(pages);
}
else
{
	out.println("You input nothing");
}

%>
</body>
</html>
