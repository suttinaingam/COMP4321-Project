package my_package;
import jdbm.RecordManager;
import jdbm.htree.HTree;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import IRUtilities.*;
import StopStem.*;
import java.util.*;

public class Search {
    public static String hello_world(){
        // StopStem stopStem = new StopStem("stopwords.txt");
        return "tired";
    }
    public static String test(){return "t";}
    public static void main (String[] args){
        retrieval_fun("computer science");
    }

    public static ArrayList<String> retrieval_fun(String query){
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(query.split(" ")));

        // Create a HashMap to store the token frequencies
        HashMap<String, Integer> tokenFreqs = new HashMap<String, Integer>();

        // Iterate over the tokens and update the token frequencies
        for (int i = 0; i < tokens.size(); i++) {
            int currentFreq = tokenFreqs.getOrDefault(tokens.get(i), 0);
            tokenFreqs.put(Integer.toString(i), currentFreq + 1);
        }

        // Print the token frequencies
        for (Map.Entry<String, Integer> entry : tokenFreqs.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        ArrayList<String> tokensID = new ArrayList<>();
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
            StopStem stopStem = new StopStem("stopwords.txt");
            String key;
			FastIterator wordIter = wordTable.keys();
            FastIterator invertedIter = invertedIndex.keys();
            FastIterator pageIter = pageTable.keys();
            int numDocuments = 0;
            while ((key = (String)pageIter.next())!=null){
                numDocuments++;
            }
            System.out.println("Collection size: " + numDocuments);
            HashMap<String, Integer>[] tf = new HashMap[tokens.size()];
            for (int i = 0; i < tokens.size(); i++) {
                tf[i] = new HashMap<String, Integer>();
            }
            HashMap<String, Double>[] tfidf = new HashMap[numDocuments];
            for (int i = 0; i < numDocuments; i++) {
                tfidf[i] = new HashMap<String, Double>();
            }
            HashMap<String, Integer> df = new HashMap<>();
            for (String word : tokens){
                tokensID.add((String)wordTable.get(stopStem.stem(word)));
            }
            for (int j = 0; j < tokensID.size(); j++){
                String content = (String) invertedIndex.get(tokensID.get(j));
                String[] arr = content.trim().split(" ");
                df.put(Integer.toString(j), arr.length/2);
 
                HashMap<String, Integer> map = new HashMap<String, Integer>();

                for (int i = 0; i < arr.length; i += 2) {
                    String page = arr[i];
                    int freq = Integer.parseInt(arr[i+1]);
                    map.put(page, freq);
                    tf[j].put(page, freq);
                }

                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    System.out.println("Page " + entry.getKey() + ": " + entry.getValue() + " occurrences");
                }
            }
            for (Map.Entry<String, Integer> entry : tf[0].entrySet()) {
                System.out.println("Page " + entry.getKey() + ": " + entry.getValue() + " occurrences");
            }
            for (Map.Entry<String, Integer> entry : df.entrySet()) {
                System.out.println("Page " + entry.getKey() + ": " + entry.getValue() + " occurrences");
            }
            String value;
            // System.out.println(tf[0].get("p0"));
            // System.out.println(numDocuments);
            for (int i = 0; i < numDocuments; i++){
                // System.out.println("loop");
                for (int j = 0; j < tokens.size(); j++){
                    if (i == 0){
                        System.out.println(tf[j].get("p"+i));
                        System.out.println(Integer.valueOf(df.get(Integer.toString(j))));
                        double d = df.get(Integer.toString(j));
                        System.out.println(Math.log(d)/Math.log(2));
                    }
                    if (tf[j].get("p"+i) == null){
                        tfidf[i].put(Integer.toString(j), Double.valueOf(0) );
                    }
                    else{
                        double d = df.get(Integer.toString(j));
                        tfidf[i].put(Integer.toString(j), tf[j].get("p"+i) * Math.log(numDocuments/d)/Math.log(2));
                    }
                }
            }
            for (Map.Entry<String, Double> entry : tfidf[0].entrySet()) {
                System.out.println("Word " + entry.getKey() + ": " + entry.getValue() + " occurrences");
            }
            HashMap<Integer, Double> results = new HashMap<Integer, Double>();

            for (int i = 0; i < numDocuments - 1; i++) {
                double innerProduct = 0.0;
                for (int j = 0; j < tokens.size(); j++){
                    innerProduct += tokenFreqs.get(Integer.toString(j)) * tfidf[i].get(Integer.toString(j));
                }
                results.put(Integer.valueOf(i), innerProduct);
            }
            List<Map.Entry<Integer, Double>> sortedResults = new ArrayList<Map.Entry<Integer, Double>>(results.entrySet());
            Collections.sort(sortedResults, new Comparator<Map.Entry<Integer, Double>>() {
                public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            for (Map.Entry<Integer, Double> entry : sortedResults) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        } catch(Exception e){}
        return result;
    }
}
