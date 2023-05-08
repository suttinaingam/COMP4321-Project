<%@ page import="java.util.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="Search.Search" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.*" %>
<%@ page import="jdbm.RecordManager" %>
<%@ page import="jdbm.htree.HTree" %>
<%@ page import="jdbm.RecordManagerFactory" %>
<%@ page import="jdbm.helper.FastIterator" %>
<%@ page import="StopStem.StopStem" %>

<html>
<body>
<%
if(request.getParameter("query")!=null)
{
    out.println("Search Results:");
    out.println("<hr>");
    String[] tokens = request.getParameter("query").split(" ");
    String q = request.getParameter("query");
    StopStem stopStem = new StopStem("C:/apache-tomcat-8.5.88/webapps/comp4321/WEB-INF/classes/stopwords.txt");
    out.println("Your query: " + q.trim());
    out.println("<hr>");
    String query = request.getParameter("query");
    Search search = new Search();
    ArrayList<String> pages = search.retrieval_fun(q.trim());
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
        FastIterator cpIter = childParent.keys();
        String key;
        String index;
        double score;
        String[] info;
        String[] prop;
        for (int i = 0; i < pages.size(); i++){
            info = pages.get(i).split(" ");
            index = info[0];
            score = Double.valueOf(info[1]);
            prop = (String[]) pageProp.get(index);
            out.println("<b>Score: </b>" + score);
            out.println("<br>");
            out.println("<b>Title: </b> <a href='" + prop[1] + "'>" + prop[0] + "</a>");
            out.println("<br>");
            out.println("<b>URL: </b> <a href='" + prop[1] + "'>" + prop[1] + "</a>");
            out.println("<br>");
            out.println("<b>Last modification date: </b>" + prop[2]);
            out.println("<br>");
            out.println("<b>Size of page: </b>" + prop[3]);
            out.println("<br>");
            out.println("<b>Top 5 keywords with highest frequency: </b>");
            String keywords = (String)forwardIndex.get(index);
            String[] keywordsSplit = keywords.split(", ");
            out.print(keywordsSplit[0].substring(1, keywordsSplit[0].length()) + " " + keywordsSplit[1] + "; ");
            for (int j = 2; j < 10; j+=2){
                out.print(keywordsSplit[j] + " " + keywordsSplit[j+1]);
                if (j!=8){
                    out.println("; ");
                }
            }
            out.println("<br>");
            out.println("<b>Parent link: </b>");
            if (childParent.get(pageTable.get(prop[1]))!=null){
                out.println(invPT.get(childParent.get(pageTable.get(prop[1]))));
            }
            out.println("<br>");
            out.println("<b>Child link: </b>");
            out.println("<br>");
            cpIter = childParent.keys();
            while ((key = (String)cpIter.next())!=null){
                if (childParent.get(key).equals(pageTable.get(prop[1]))){
                    out.println(invPT.get(key));
                    out.println("<br>");
                }
            }
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
