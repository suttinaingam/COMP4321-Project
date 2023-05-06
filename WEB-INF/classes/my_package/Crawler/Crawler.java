package Crawler;
import IRUtilities.*;
import StopStem.*;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.Vector;
import java.io.IOException;
import java.io.Serializable;
import jdbm.btree.BTree;
import jdbm.helper.StringComparator;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import java.net.URL;
import java.text.ParseException;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.ParserException;
import java.io.ByteArrayOutputStream;
import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.htmlparser.beans.StringBean;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.AndFilter;
import java.util.StringTokenizer;
import org.htmlparser.beans.LinkBean;
import java.util.Arrays;
import java.net.*;
import org.htmlparser.Tag;
import org.htmlparser.visitors.NodeVisitor;
import java.util.ArrayList;
import java.util.List;
import org.htmlparser.nodes.TagNode;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
// import javax.servlet.http.HttpSession;
/*
 * This class is used for crawling webpages.
 * 
 */
public class Crawler
{
	private String url;

	/*
	 * This is a class constructor.
	 * @param url in String format
	 */
	Crawler(String _url){url = _url;}

	/*
	 * This method extract words from a webpage.
	 * @param none
	 * @return a list of words extracted from a webpage in Vector<String> format
	 */
	public Vector<String> extractWords() throws ParserException {
		StringBean sb; 
		sb = new StringBean();
		sb.setLinks(false);
		sb.setURL(url);
		String allString = sb.getStrings();
		String[] words = allString.split("[ \\n]");
		StopStem stopStem = new StopStem("stopwords.txt");
		Vector<String> wordVector = new Vector<String>();
		for (int i = 0; i < words.length; i++){
			words[i] = words[i].replace("\n", "").replace("\r", "");
			words[i] = words[i].replaceAll("[())]", "");
			words[i] = words[i].replaceAll("[^a-zA-Z]+", "");
			words[i] = words[i].toLowerCase();
			// if (!stopStem.isStopWord(words[i])){
			// 	wordVector.add(stopStem.stem(words[i]));
			// }
			if (words[i]!=""){
				wordVector.add(stopStem.stem(words[i]));
			}
		}
		return wordVector;
	}
	
	/*
	 * This method count word frquencies in a list of words. 
	 * @param a list of words in Vector<String> format
	 * @return mapping of each word with its frequency in HashMap<String, Integer> format
	 */
	public static HashMap<String, Integer> countFrequency(Vector<String> vector) {
		HashMap<String, Integer> frequencyMap = new HashMap<String, Integer>();
		// System.out.println(vector.size());
		for (int i = 0; i < vector.size(); i++) {
			String element = vector.get(i);
			if (frequencyMap.containsKey(element)) {
				int count = frequencyMap.get(element);
				frequencyMap.put(element, count + 1);
			} else {
				frequencyMap.put(element, 1);
			}
		}
		return frequencyMap;
	}
	
	/*
	 * This method extract child links in a webpage.
	 * @param none
	 * @return a list of child links extracted from a webpage in Vector<String> format
	 */
	public Vector<String> extractLinks() throws ParserException
	{
		// extract links in url and return them
		Vector<String> v_link = new Vector<String>();
	    LinkBean lb = new LinkBean();
	    lb.setURL(url);
	    URL[] URL_array = lb.getLinks();
	    for(int i=0; i<URL_array.length; i++){
			if (!v_link.contains(URL_array[i].toString())){
				v_link.add(URL_array[i].toString());
			}
	    }
	    return v_link;
	}

	/*
	 * This method gets title of a webpage.
	 * @param URL link of a wepage in String format
	 * @return title of the webpage in String format
	 */
	public String getTitle(String link){
		PrintStream originalSystemOut = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut(new java.io.PrintStream(baos));
		try{
			Parser.main(new String[]  {link, "TITLE"});
		}catch(Exception e){
		
		}
		System.setOut(originalSystemOut);
		String output = baos.toString();
		String title;
		if (output.length()>7){
			title = output.substring(7, output.length());
		}
		else{
			title = " ";
		}
		return title;
    }

	/*
	 * This method gets last modification date of a webpage.
	 * @param URL link of a wepage in String format
	 * @return last modification date of the webpage in String format
	 * if last modification date is not available, return latest access date
	 */
	public String getDate(String link){
		try {
			URL url = new URL(link);
			URLConnection connection = url.openConnection();
			// Get the last modification date from the headers
			long lastModified = connection.getLastModified();
			if (lastModified == 0){
				lastModified = connection.getDate();
			}
			Date lastModifiedDate = new Date(lastModified);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String lastModifiedDateString = sdf.format(lastModifiedDate);
			return lastModifiedDateString;
		} catch (Exception e) {}
		return " ";
	}

	/*
	 * This method gets size of a webpage.
	 * @param URL link of a wepage in String format
	 * @return size of the webpage in long format
	 * if size is not available or 0, return number of HTML code in the webpage
	 */
	public long getSize(String link){
		try {
			URL url = new URL(link);
			Parser parser = new Parser(url.toString());
			NodeList nodes = parser.parse(null);
			String htmlCode = nodes.toHtml();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			long pageSize = connection.getContentLength();
			if (pageSize==-1){
				pageSize = htmlCode.length();
			}
			return pageSize;
		} catch (Exception e) {}
		return 0;
	}
	
	/*
	 * This method fetches webpages using breadth-first strategy.
	 * This method also write output to spider_results.txt.
	 * @param URL link of a wepage in String format
	 * @param number of pages to be fetched in int format
	 * @return none
	 */

	public static void crawlIndex(String startLink, int numPages) throws Exception {
		// Table t = new Table("phase1", "childparent");
		// t.printAll();
		RecordManager recman = null;
		HTree pageTable = null;
		HTree wordTable = null;
		HTree pageProp = null;
		HTree forwardIndex = null;
		HTree childParent = null;
		HTree invertedIndex = null;
		try {
			recman = RecordManagerFactory.createRecordManager("project");
			pageTable = HTree.createInstance(recman);
			recman.setNamedObject("page", pageTable.getRecid());
			wordTable = HTree.createInstance(recman);
			recman.setNamedObject("word", wordTable.getRecid());
			pageProp = HTree.createInstance(recman);
			recman.setNamedObject("pageprop", pageProp.getRecid());
			forwardIndex = HTree.createInstance(recman);
			recman.setNamedObject("forwardindex", forwardIndex.getRecid());
			childParent = HTree.createInstance(recman);
			recman.setNamedObject("childparent", childParent.getRecid());
			invertedIndex = HTree.createInstance(recman);
			recman.setNamedObject("invertedindex", invertedIndex.getRecid());
		} catch (Exception e) {}

		PrintStream originOutputStream = System.out;
		File file = new File("spider_result.txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (Exception e) {}
		// Create a new PrintStream that writes to the file output stream
		PrintStream ps = new PrintStream(fos);
		// Redirect System.out to the new PrintStream
		// System.setOut(ps);

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(startLink);
        visited.add(startLink);
		int pageCount = 0;
		int wordCount = 0;
		int pCount = 0;
		long startTime = System.currentTimeMillis();
		while (!queue.isEmpty() && pageCount < numPages) {
			String currentLink = queue.poll();
			try {
				URL url = new URL(currentLink);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				int responseCode = connection.getResponseCode();
				System.out.println(responseCode);
				// if (responseCode == 200 || currentLink == "http://www.cse.ust.hk"){
					Crawler crawler = new Crawler(currentLink);
					if (pageTable.get(currentLink)==null){
						pageTable.put(currentLink, String.valueOf(pCount));
						pCount++;
						System.out.println(pCount);
					}
					String[] value = new String[4];
					value[0] = crawler.getTitle(currentLink).trim();
					value[1] = currentLink;
					value[2] = crawler.getDate(currentLink);
					value[3] = String.valueOf(crawler.getSize(currentLink));
					if (pageProp.get(String.valueOf(pageCount))==null){
						pageProp.put(String.valueOf(pageCount), value);
					}
					Vector<String> wordList = crawler.extractWords(); // problem
					HashMap<String, Integer> frequencyMap = countFrequency(wordList);
					List<Map.Entry<String, Integer>> list = new ArrayList<>(frequencyMap.entrySet());
					list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
					ArrayList<String> wlist = new ArrayList<>();
					for (Map.Entry<String, Integer> entry : list) {
						if (entry.getKey()!=""){
							if (wordTable.get(String.valueOf(entry.getKey()))==null){
								wordTable.put(String.valueOf(entry.getKey()), String.valueOf(wordCount));
							}
							if (invertedIndex.get(String.valueOf(wordTable.get(entry.getKey())))!=null){
								invertedIndex.put(String.valueOf(wordTable.get(entry.getKey())), invertedIndex.get(String.valueOf(wordTable.get(entry.getKey()))) + "p" + pageTable.get(currentLink) + " " + String.valueOf(entry.getValue()) + " ");
							}
							else{
								invertedIndex.put(wordTable.get(entry.getKey()), "p" + pageTable.get(currentLink) + " " + String.valueOf(entry.getValue()) + " ");
							}
							wlist.add(String.valueOf(entry.getKey()));
							wordCount++;
						}
					}
					String wl = wlist.toString();
					if (forwardIndex.get(pageTable.get(currentLink))==null){
						forwardIndex.put(pageTable.get(currentLink), wl);
					}
					Vector<String> links = crawler.extractLinks();
					System.out.println(links.size());
					for (int i = 0; i < links.size(); i++) {
						if (pCount < numPages - 1){
							if (pageTable.get(links.get(i))==null){
								// if (pageTable.get(links.get(i))==null){
								pageTable.put(links.get(i), String.valueOf(pCount));
								pCount++;
							}
								// }
							System.out.println(pCount);
							if (!pageTable.get(links.get(i)).equals(String.valueOf(0))){
								if (childParent.get(pageTable.get(links.get(i)))==null){
									childParent.put(pageTable.get(links.get(i)), pageTable.get(currentLink));
								}
							}
						}
					}
					for (String link : links) {
						if (!visited.contains(link)) {
							visited.add(link);
							queue.add(link);
						} 
					}
					System.out.println(queue.size());
					pageCount++;
					System.out.println(pageCount);
					connection.disconnect();
				// }
			} catch (Exception e) {
			}
		}
		// code to be measured
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("Elapsed time: " + elapsedTime + " ms");
		// Close the output stream
		try {
			fos.close();
			System.setOut(originOutputStream);
			recman.commit();
			recman.close();
		} catch (Exception e) {}
	}

	public static void retrieve(){
		// PrintStream originOutputStream = System.out;
		// File file = new File("spider_result.txt");
		// FileOutputStream fos = null;
		// try {
		// 	fos = new FileOutputStream(file);
		// } catch (Exception e) {
		// }
		// PrintStream ps = new PrintStream(fos);
		// System.setOut(ps);
		RecordManager recman = null;
		HTree pageTable = null;
		HTree wordTable = null;
		HTree pageProp = null;
		HTree forwardIndex = null;
		HTree childParent = null;
		HTree invertedIndex = null;
		try {
			recman = RecordManagerFactory.createRecordManager("project");
			pageTable = HTree.load(recman, recman.getNamedObject("page"));
			wordTable = HTree.load(recman, recman.getNamedObject("word"));
			pageProp = HTree.load(recman, recman.getNamedObject("pageprop"));
			forwardIndex = HTree.load(recman, recman.getNamedObject("forwardindex"));
			childParent = HTree.load(recman, recman.getNamedObject("childparent"));
			invertedIndex = HTree.load(recman, recman.getNamedObject("invertedindex"));
			FastIterator iter = pageProp.keys();
			String key;
			String key2;
			FastIterator invertedIter = invertedIndex.keys();	
			FastIterator wordIter = wordTable.keys();
			// FastIterator iter3 = childParent.keys();
			int i = 0;
			while(invertedIter.next()!=null) {
				i++;
				// invertedIter.next();
			}
			// System.out.println(i);
			i = 0;
			while(wordIter.next()!=null) {
				i++;
				// invertedIter.next();
			}
			System.out.println(i);
			invertedIter = invertedIndex.keys();
			wordIter = wordTable.keys();
			while ((key = (String)invertedIter.next())!=null){
				// System.out.println(key);
				while ((key2 = (String)wordIter.next())!=null){
					// System.out.println("Inverted " + key);
					// System.out.println("WordTable " + wordTable.get(key2));
					// System.out.println(key.equals((String)wordTable.get(key2)));
					if (key.equals((String)wordTable.get(key2))){
						// System.out.print(key2 + ": ");
					}
				}
				// wordIter = wordTable.keys();
				// System.out.println(invertedIndex.get(key));
				// System.out.print(wordTable.get(key));
				// System.out.println(invertedIndex.get(key));
				// String[] array2 = value2.split(" ");
				// for (String a : array2){
				// 	// System.out.println(a);
				// }
			}
			iter = pageProp.keys();
			while( (key = (String)iter.next())!=null)
			{
				String[] in = (String[])pageProp.get(key);
				String title = in[0];
				String URL = in[1];
				String date = in[2];
				String size = in[3];
				System.out.println(title);
				System.out.println(URL);
				System.out.println(date + ", " + size);
				System.out.println("--------------------");
				
			}
			wordIter = wordTable.keys();
			while ((key = (String)wordIter.next())!=null){
				// System.out.println(key);
				// System.out.println(wordTable.get(key));
			}
			// fos.close();
			// System.setOut(originOutputStream);
		} catch (Exception e) {}
	}

	public static String hello_world(){
        return "hello world!";
    }
	
	// public static void main (String[] args) 
	// {
	// 	try  
	// 	{         
	// 		File f= new File("project.db");             
	// 		f.delete(); 
	// 		f = new File("project.lg");
	// 		f.delete();
	// 	}  catch(Exception e)  {}  
	// 	String startLink = "http://www.cse.ust.hk";
	// 	// String startLink = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
	// 	int numPages = 10;
    //     // bfs(startLink, numPages);
	// 	try {
	// 		crawlIndex(startLink, numPages);
	// 		retrieve();
	// 	} catch (Exception e) {}
	// }
}