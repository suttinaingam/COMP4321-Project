import StopStem.*;
import IRUtilities.*;
import Crawler.*;
import java.io.File;
import Search.*;
import java.util.*;

public class TestProgram {
    public static void main (String[] args) 
	{
		// String startLink = "http://www.cse.ust.hk";
		String startLink = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
		int numPages = 300;
		try {
			// Crawler.crawlIndex(startLink, numPages);
			// Crawler.retrieve();
		} catch (Exception e) {}
		Search search = new Search();
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter your query: ");
        String query = scanner.nextLine();
        ArrayList<String> pages = search.retrieval_fun(query);
        System.out.println(pages);
	}
}
    
