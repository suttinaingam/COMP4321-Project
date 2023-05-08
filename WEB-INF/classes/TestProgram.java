import StopStem.*;
import IRUtilities.*;
import Crawler.*;
import java.io.File;
import Search.*;
import java.util.*;
import jdbm.RecordManager;
import jdbm.htree.HTree;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;

public class TestProgram {
    public static void main (String[] args) 
	{
		// String startLink = "http://www.cse.ust.hk";
		String startLink = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
		int numPages = 300;
		try {
			Crawler.crawlIndex(startLink, numPages);
			// Crawler.retrieve();
		} catch (Exception e) {}
		Search search = new Search();
		// Scanner scanner = new Scanner(System.in);
		// System.out.print("Enter your query: ");
        // String query = scanner.nextLine();
		RecordManager recman = null;
		HTree pageTable = null;
		HTree wordTable = null;
		HTree pageProp = null;
		HTree forwardIndex = null;
		HTree childParent = null;
		HTree invertedIndex = null;
        HTree invPT = null;
		HTree invWT = null;
        String catalinaHome = System.getenv("CATALINA_HOME");
		try {
			// recman = RecordManagerFactory.createRecordManager(catalinaHome + "/bin/assets/project");
			// pageTable = HTree.load(recman, recman.getNamedObject("page"));
			// wordTable = HTree.load(recman, recman.getNamedObject("word"));
			// pageProp = HTree.load(recman, recman.getNamedObject("pageprop"));
			// forwardIndex = HTree.load(recman, recman.getNamedObject("forwardindex"));
			// childParent = HTree.load(recman, recman.getNamedObject("childparent"));
			// invertedIndex = HTree.load(recman, recman.getNamedObject("invertedindex"));	
            // invPT = HTree.load(recman, recman.getNamedObject("invpage"));
            // invWT = HTree.load(recman, recman.getNamedObject("invword"));	
			// ArrayList<String> pages = search.retrieval_fun("test page");
			// System.out.println(pages);
			// String index;
			// double score;
			// String[] info;
			// String[] prop;
			// for (int i = 0; i < pages.size(); i++){
			// 	info = pages.get(i).split(" ");
			// 	index = info[0];
			// 	score = Double.valueOf(info[1]);
			// 	prop = (String[]) pageProp.get(index);
			// 	System.out.println(score);
			// 	System.out.println(prop[0]);
			// 	System.out.println(invPT.get(index));
			// 	System.out.println(prop[2]);
			// 	System.out.println(prop[3]);
			// 	System.out.println(forwardIndex.get(index));
			// 	System.out.println(" ");
			// }
		} catch(Exception e){}	
            String key;
	}
}
    
