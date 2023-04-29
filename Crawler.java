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
		for (int i = 0; i < words.length; i++){
			words[i] = words[i].replace("\n", "").replace("\r", "");
			words[i] = words[i].replaceAll("[())]", "");
			words[i] = words[i].replaceAll("[^a-zA-Z]+", "");
			words[i] = words[i].toLowerCase();
		}
		Vector<String> wordVector = new Vector<String>(Arrays.asList(words));
		// HashMap<String, Integer> frequencyMap = countFrequency(wordVector);
	
		// List<Map.Entry<String, Integer>> list = new ArrayList<>(frequencyMap.entrySet());
		// list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
	
		// StringBuilder output = new StringBuilder();
		// int count = 0;
		// for (Map.Entry<String, Integer> entry : list) {
		// 	if (count >= 10) {
		// 		break;
		// 	}
		// 	if (entry.getKey()!=""){
		// 		output.append(entry.getKey()).append(" ").append(entry.getValue()).append("; ");
		// 		count++;
		// 	}
		// }
		// System.out.println(output);
		return wordVector;
	}
	
	/*
	 * This method count word frquencies in a list of words. 
	 * @param a list of words in Vector<String> format
	 * @return mapping of each word with its frequency in HashMap<String, Integer> format
	 */
	public static HashMap<String, Integer> countFrequency(Vector<String> vector) {
		HashMap<String, Integer> frequencyMap = new HashMap<String, Integer>();
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
		Boolean check = false;
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
	public static void bfs(String startLink, int numPages) {
		PrintStream originOutputStream = System.out;
		File file = new File("spider_result.txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (Exception e) {
		}
		// Create a new PrintStream that writes to the file output stream
		PrintStream ps = new PrintStream(fos);
		// Redirect System.out to the new PrintStream
		System.setOut(ps);

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(startLink);
        visited.add(startLink);
		int pageCount = 0;
		Table pageTable = null;
		Table wordTable = null;
		Table pageProp = null;
		Table forwardIndex = null;
		Table childParent = null;
		InvertedIndex invertedIndex = null;
		try {
			pageTable = new Table("phase1", "page");
			wordTable = new Table("phase1", "word");
			pageProp = new Table("phase1", "pageprop");
			forwardIndex = new Table("phase1", "forwardindex");
			childParent = new Table("phase1", "childparent");
			invertedIndex = new InvertedIndex("phase1", "invertedindex");
		} catch (Exception e) {}
		int wordCount = 0;
		int pCount = 0;
		while (!queue.isEmpty() && pageCount < numPages) {
			String currentLink = queue.poll();
			try {
				URL url = new URL(currentLink);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				int responseCode = connection.getResponseCode();
				if (responseCode == 200){
					Crawler crawler = new Crawler(currentLink);
					System.out.print(crawler.getTitle(currentLink));
					System.out.println(currentLink);
					System.out.println(crawler.getDate(currentLink) + ", " + crawler.getSize(currentLink));
					if (pageTable.getValue(currentLink)==null){
						pageTable.addEntry(currentLink, String.valueOf(pCount));
						pCount++;
					}
					String value=crawler.getTitle(currentLink).trim()+" "+currentLink+" "+crawler.getDate(currentLink)+" "+crawler.getSize(currentLink);
					pageProp.addEntry(String.valueOf(pageCount), value);
					Vector<String> wordList = crawler.extractWords();
					HashMap<String, Integer> frequencyMap = countFrequency(wordList);
					List<Map.Entry<String, Integer>> list = new ArrayList<>(frequencyMap.entrySet());
					list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
					ArrayList<String> wlist = new ArrayList<>();
					for (Map.Entry<String, Integer> entry : list) {
						if (entry.getKey()!=""){
							wordTable.addEntry(String.valueOf(entry.getKey()), String.valueOf(wordCount));
							invertedIndex.addEntry(String.valueOf(wordTable.getValue(entry.getKey())), pageTable.getValue(currentLink), String.valueOf(entry.getValue()));
							wlist.add(String.valueOf(entry.getKey()));
							wordCount++;
						}
					}
					String wl = wlist.toString();
					forwardIndex.addEntry(pageTable.getValue(currentLink), wl);
					Vector<String> links = crawler.extractLinks();
					int maxLinks = (links.size()>10?10:links.size());
					for (int i = 0; i < maxLinks; i++) {
						if (pageTable.getValue(links.get(i))==null){
							pageTable.addEntry(links.get(i), String.valueOf(pCount));
							pCount++;
						}
						if (childParent.getValue(links.get(i))==null && !pageTable.getValue(links.get(i)).equals(String.valueOf(0))){
							childParent.addEntry(pageTable.getValue(links.get(i)), pageTable.getValue(currentLink));
						}
						System.out.println(links.get(i));
					}
					for (String link : links) {
						if (!visited.contains(link)) {
							visited.add(link);
							queue.add(link);
						} 
					}
					// System.out.println("---------------------------------------------------");
					pageCount++;
			
				}
			} catch (Exception e) {
			}
		}
		// Close the output stream
		try {
			fos.close();
			System.setOut(originOutputStream);
			pageTable.close();
			wordTable.close();
			pageProp.close();
			forwardIndex.close();
			invertedIndex.close();
			childParent.close();
		} catch (Exception e) {}
    }

	public static void testProgram(String startLink, int numPages) throws Exception {
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
			recman = RecordManagerFactory.createRecordManager("phase1");
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
		} catch (Exception e) {
		}
		// Create a new PrintStream that writes to the file output stream
		PrintStream ps = new PrintStream(fos);
		// Redirect System.out to the new PrintStream
		System.setOut(ps);

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(startLink);
        visited.add(startLink);
		int pageCount = 0;
		// try {
		// 	pageTable = new Table("phase1", "page");
		// 	wordTable = new Table("phase1", "word");
		// 	pageProp = new Table("phase1", "pageprop");
		// 	forwardIndex = new Table("phase1", "forwardindex");
		// 	childParent = new Table("phase1", "childparent");
		// 	invertedIndex = new InvertedIndex("phase1", "invertedindex");
		// } catch (Exception e) {}
		int wordCount = 0;
		int pCount = 0;
		while (!queue.isEmpty() && pageCount < numPages) {
			String currentLink = queue.poll();
			try {
				URL url = new URL(currentLink);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				int responseCode = connection.getResponseCode();
				if (responseCode == 200){
					Crawler crawler = new Crawler(currentLink);
					if (pageTable.get(currentLink)==null){
						pageTable.put(currentLink, String.valueOf(pCount));
						pCount++;
					}
					String[] value = new String[4];
					value[0] = crawler.getTitle(currentLink).trim();
					value[1] = currentLink;
					value[2] = crawler.getDate(currentLink);
					value[3] = String.valueOf(crawler.getSize(currentLink));
					if (pageProp.get(String.valueOf(pageCount))==null){
						pageProp.put(String.valueOf(pageCount), value);
					}
					Vector<String> wordList = crawler.extractWords();
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
					// int maxLinks = (links.size()>10?10:links.size());
					for (int i = 0; i < links.size(); i++) {
						if (pageTable.get(links.get(i))==null){
							if (pageTable.get(links.get(i))==null){
								pageTable.put(links.get(i), String.valueOf(pCount));
							}
							pCount++;
						}
						if (childParent.get(links.get(i))==null && !pageTable.get(links.get(i)).equals(String.valueOf(0))){
							if (childParent.get(pageTable.get(links.get(i)))==null){
								childParent.put(pageTable.get(links.get(i)), pageTable.get(currentLink));
							}
						}
					}
					for (String link : links) {
						if (!visited.contains(link)) {
							visited.add(link);
							queue.add(link);
						} 
					}
					// System.out.println("---------------------------------------------------");
					pageCount++;
			
				}
			} catch (Exception e) {
			}
		}
		// Close the output stream
		try {
			fos.close();
			System.setOut(originOutputStream);
			recman.commit();
			recman.close();
		} catch (Exception e) {}
	}

	public static void test(){
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
			recman = RecordManagerFactory.createRecordManager("phase1");
			pageTable = HTree.load(recman, recman.getNamedObject("page"));
			wordTable = HTree.load(recman, recman.getNamedObject("word"));
			pageProp = HTree.load(recman, recman.getNamedObject("pageprop"));
			forwardIndex = HTree.load(recman, recman.getNamedObject("forwardindex"));
			childParent = HTree.load(recman, recman.getNamedObject("childparent"));
			invertedIndex = HTree.load(recman, recman.getNamedObject("invertedindex"));
			FastIterator iter = pageProp.keys();
			String key;
			String key2;
			FastIterator iter2 = invertedIndex.keys();	
			FastIterator iter3 = childParent.keys();
			while ((key2 = (String)iter.next())!=null){
				String value2 = (String)invertedIndex.get(key2);
				System.out.print(value2);
				String[] array2 = value2.split(" ");
				for (String a : array2){
					System.out.println(a);
				}
			}
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
			// fos.close();
			// System.setOut(originOutputStream);
		} catch (Exception e) {}
	}
	
	public static void main (String[] args) 
	{
		try  
		{         
			File f= new File("phase1.db");             
			f.delete(); 
			f = new File("phase1.lg");
			f.delete();
		}  
		catch(Exception e)  {}  
		String startLink = "https://www.cse.ust.hk/";
		int numPages = 2;
        // bfs(startLink, numPages);
		try {
			testProgram(startLink, numPages);
			test();
		} catch (Exception e) {}
	}
}

/*
 * This class is used for creating inverted-file index.
 */
class InvertedIndex
{
	private RecordManager recman;
	private HTree hashtable;

	/*
	 * This is a class constructor.
	 * @param record manager name in String format
	 * @param inverted-file name in String format
	 */
	InvertedIndex(String recordmanager, String objectname) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		long recid = recman.getNamedObject(objectname);	
		if (recid != 0)
			hashtable = HTree.load(recman, recid);
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject(objectname, hashtable.getRecid() );
		}
	}

	/*
	 * This method add an entry with corresponding wordID to the inverted-file.
	 * @param wordID which maps to a word
	 * @param pageID which maps to a page
	 * @param frequencies of this word in the page
	 * @return none
	 */
	public void addEntry(String wordID, String pageID, String freq) throws IOException
	{
		if (hashtable.get(wordID)!=null){
			hashtable.put(wordID, hashtable.get(wordID) + "p" + pageID + " " + freq + " ");
		}
		else{
			hashtable.put(wordID, "p" + pageID + " " + freq + " ");
		}
	}

	/*
	 * This method delete an entry with corresponding wordID from the inverted-file.
	 * @param wordID which maps to a word
	 * @return none
	 */
	public void delEntry(String wordID) throws IOException
	{
		hashtable.remove(wordID);
	} 

	/*
	 * This method prints key and value of every entries in the hashtable in key: value format
	 * @param none
	 * @return none
	 */
	public void printAll() throws IOException
	{
		FastIterator iter = hashtable.keys();
		String key;	
		while( (key = (String)iter.next())!=null)
		{
			System.out.println(key + " : " + hashtable.get(key));
		}
	}

	/*
	 * This method returns value (pageID + freq) for corresponding key (wordID) in the hashtable.
	 * @param key (pageID) which maps to a page in String format
	 * @return value (pageID + freq) in String format
	 */
	public String getValue(String key)
	{
		try {
			return (String)hashtable.get(key);
		} catch (Exception e) {}
		return "";
	}

	/*
	 * This method commit all changes to record manager and close it.
	 * @param none
	 * @return none
	 */
	public void close() throws IOException
	{
		recman.commit();
		recman.close();				
	} 	
}

/*
 * This class is used for creating mapping tables, forward index, and link-based index (child-parent table).
 */
class Table
{
	private RecordManager recman;
	private HTree hashtable;

	/*
	 * This is a class constructor.
	 * @param record manager name in String format
	 * @param hashtable name in String format
	 */
	Table(String recordmanager, String objectname) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		long recid = recman.getNamedObject(objectname);
		if (recid != 0)
			hashtable = HTree.load(recman, recid);
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject(objectname, hashtable.getRecid() );
		}
	}

	/*
	 * This method add an entry with corresponding wordID to the hashtable
	 * @param key for the hashtable in String format
	 * @param value for the correponding key to be add in the hashtable in String format
	 * @return none
	 */
	public void addEntry(String key, String value) throws IOException
	{
		if (hashtable.get(key)==null){
			hashtable.put(key, value);
		}
	}

	/*
	 * This method delete an entry with corresponding wordID from the hashtable.
	 * @param key of the hashtable in String format
	 * @return none
	 */
	public void delEntry(String key) throws IOException
	{
		hashtable.remove(key);
	} 

	/*
	 * This method prints key and value of every entries in the hashtable in key: value format
	 * @param none
	 * @return none
	 */
	public void printAll() throws IOException
	{
		FastIterator iter = hashtable.keys();
		String key;	
		while( (key = (String)iter.next())!=null)
		{
			System.out.println(key + " : " + hashtable.get(key));
		}
	}	

	/*
	 * This method returns key for corresponding value in the hashtable.
	 * @param value in String format
	 * @return key in String format
	 */
	public String getKey(String value) throws IOException
	{
		FastIterator iter = hashtable.keys();
		String key;
		while ((key = (String)iter.next())!=null) {
			try {
				if (hashtable.get(key)==value){
					return key;
				}
			} catch (Exception e) {}
		}
		return "";
	}

	/*
	 * This method returns value for corresponding key in the hashtable.
	 * @param key in String format
	 * @return value in String format
	 */
	public String getValue(String key)
	{
		try {
			return (String)hashtable.get(key);
		} catch (Exception e) {}
		return "";
	}

	/*
	 * This method commit all changes to record manager and close it.
	 * @param none
	 * @return none
	 */
	public void close() throws IOException
	{
		recman.commit();
		recman.close();			
	} 	
}