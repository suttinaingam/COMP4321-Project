package my_package;
import StopStem.*;
import IRUtilities.*;
import Crawler.*;
import Crawler.Crawler;
import java.io.File;

public class TestProgram {
    public static void main (String[] args) 
	{
		try  
		{         
			File f= new File("project.db");             
			f.delete(); 
			f = new File("project.lg");
			f.delete();
		}  catch(Exception e)  {}  
		String startLink = "http://www.cse.ust.hk";
		// String startLink = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
		int numPages = 300;
		try {
			Crawler.crawlIndex(startLink, numPages);
			// Crawler.retrieve();
		} catch (Exception e) {}
	}
	public static String getResult(){
		// StopStem stopStem = new StopStem("stopwords.txt");
		Crawler.retrieve();
		return "result";
	}
	public static String getWord(){
		return "word";
	}
}
    
