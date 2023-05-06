package StopStem;

import IRUtilities.*;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

public class StopStem
{
	private Porter porter;
	private HashSet<String> stopWords;
	public boolean isStopWord(String str)
	{
		return stopWords.contains(str);	
	}
	public StopStem(String str) 
	{
		super();
		porter = new Porter();
		stopWords = new HashSet<String>();
				
		// use BufferedReader to extract the stopwords in stopwords.txt (path passed as parameter str)
		// add them to HashSet<String> stopWords
		// MODIFY THE BELOW CODE AND ADD YOUR CODES HERE
		stopWords.add("is");
		stopWords.add("am");
		stopWords.add("are");
		stopWords.add("was");
		stopWords.add("were");
		try {
			FileReader fs = new FileReader(str);
			BufferedReader bs = new BufferedReader(fs);
			String line = "";
			while((line = bs.readLine())!=null){
				stopWords.add(line);
			}
		} catch (Exception e) {
			
		}
	}
	public String stem(String str)
	{
		return porter.stripAffixes(str);
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
			System.out.println(i);
			i = 0;
			while(wordIter.next()!=null) {
				i++;
				// invertedIter.next();
			}
			System.out.println(i);
			invertedIter = invertedIndex.keys();
			wordIter = wordTable.keys();
			while ((key = (String)invertedIter.next())!=null){
				System.out.println(key);
				System.out.println(invertedIndex.get(key));
				while ((key2 = (String)wordIter.next())!=null){
					// System.out.println("Inverted " + key);
					// System.out.println("WordTable " + wordTable.get(key2));
					// System.out.println(key.equals((String)wordTable.get(key2)));
					if (key.equals((String)wordTable.get(key2))){
						System.out.println(key2);
					}
				}
				wordIter = wordTable.keys();
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
	// public static void main(String[] arg) 
	// {
	// 	test();
	// 	StopStem stopStem = new StopStem("stopwords.txt");
	// 	String input="";
	// 	try{
	// 		do
	// 		{
	// 			System.out.print("Please enter a single English word: ");
	// 			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	// 			input = in.readLine();
	// 			if(input.length()>0)
	// 			{	
	// 				if (stopStem.isStopWord(input))
	// 					System.out.println("It should be stopped");
	// 				else
	// 		   			System.out.println("The stem of it is \"" + stopStem.stem(input)+"\"");
	// 			}
	// 		}
	// 		while(input.length()>0);
	// 	}
	// 	catch(IOException ioe)
	// 	{
	// 		System.err.println(ioe.toString());
	// 	}
	// }
}

