<%@ page import="java.util.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="Search.Search" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.*" %>
<%@ page import="jdbm.RecordManager" %>
<%@ page import="jdbm.htree.HTree" %>
<%@ page import="jdbm.RecordManagerFactory" %>
<%@ page import="jdbm.helper.FastIterator" %>

<html>
<body>
<%
if(request.getParameter("query")!=null)
{
    out.println("Search Results:");
    out.println("<hr>");
    String[] tokens = request.getParameter("query").split(" ");
    for (int i = 0; i < tokens.length; i++){
        out.println(tokens[i]);
        out.println("<br>");
    }
    out.println("<hr>");
    String query = request.getParameter("query");
    Search search = new Search();
    ArrayList<String> pages = search.retrieval_fun(query);
    RecordManager recman = null;
    HTree pageTable = null;
    HTree wordTable = null;
    HTree pageProp = null;
    HTree forwardIndex = null;
    HTree childParent = null;
    HTree invertedIndex = null;
    HTree invPT = null;
    HTree invWT = null;
    try {
        recman = RecordManagerFactory.createRecordManager("C:/apache-tomcat-8.5.88/bin/assets/project");
        pageTable = HTree.load(recman, recman.getNamedObject("page"));
        wordTable = HTree.load(recman, recman.getNamedObject("word"));
        pageProp = HTree.load(recman, recman.getNamedObject("pageprop"));
        forwardIndex = HTree.load(recman, recman.getNamedObject("forwardindex"));
        childParent = HTree.load(recman, recman.getNamedObject("childparent"));
        invertedIndex = HTree.load(recman, recman.getNamedObject("invertedindex"));	
        invPT = HTree.load(recman, recman.getNamedObject("invpage"));
        invWT = HTree.load(recman, recman.getNamedObject("invword"));	
        String index;
        double score;
        String[] info;
        String[] prop;
        for (int i = 0; i < pages.size(); i++){
            info = pages.get(i).split(" ");
            index = info[0];
            score = Double.valueOf(info[1]);
            prop = (String[]) pageProp.get(index);
            out.println(score);
            out.println("<br>");
            out.println(prop[0]);
            out.println("<br>");
            out.println(prop[1]);
            out.println("<br>");
            out.println(prop[2]);
            out.println("<br>");
            out.println(prop[3]);
            out.println("<br>");
            out.println(forwardIndex.get(index));
            out.println("<br>");
            out.println("<hr>");
        }
    } catch(Exception e){}	
}
else
{
	out.println("You input nothing");
}

%>
</body>
</html>
