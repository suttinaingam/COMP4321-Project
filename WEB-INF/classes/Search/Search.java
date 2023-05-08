package Search;
import jdbm.RecordManager;
import jdbm.htree.HTree;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import IRUtilities.*;
import StopStem.*;
import java.util.*;

import javax.print.attribute.standard.PagesPerMinute;

public class Search {
    public Search(){}

    public Double getDocumentLength(String documentID){

        return Double.valueOf(0);
    }

    public ArrayList<String> retrieval_fun(String query){
        HashMap<Integer, Double> results = new HashMap<Integer, Double>();
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> split = new ArrayList<String>(Arrays.asList(query.split(" ")));
        ArrayList<String> tokens = new ArrayList<String>();
        StopStem stopStem = new StopStem("stopwords.txt");
        for (String token: split){
            if (!stopStem.isStopWord(token)){
                tokens.add(stopStem.stem(token));
            }
        }
        for (String token: tokens){
            System.out.println(token);
        }

        // Create a HashMap to store the token frequencies
        HashMap<String, Integer> tokenFreqs = new HashMap<String, Integer>();

        // Iterate over the tokens and update the token frequencies
        for (int i = 0; i < tokens.size(); i++) {
            int currentFreq = tokenFreqs.getOrDefault(tokens.get(i), 0);
            tokenFreqs.put(tokens.get(i), currentFreq + 1);
        }

        // Print the token frequencies
        // for (Map.Entry<String, Integer> entry : tokenFreqs.entrySet()) {
        //     System.out.println(entry.getKey() + " = " + entry.getValue());
        // }
        // ArrayList<String> tokensID = new ArrayList<>();
        RecordManager recman = null;
		HTree pageTable = null;
		HTree wordTable = null;
		HTree pageProp = null;
		HTree forwardIndex = null;
		HTree childParent = null;
		HTree invertedIndex = null;
        HTree invPT = null;
		HTree invWT = null;
        HTree titleIndex = null;
        String catalinaHome = System.getenv("CATALINA_HOME");
		try {
			recman = RecordManagerFactory.createRecordManager(catalinaHome + "/bin/assets/project");
			pageTable = HTree.load(recman, recman.getNamedObject("page"));
			wordTable = HTree.load(recman, recman.getNamedObject("word"));
			pageProp = HTree.load(recman, recman.getNamedObject("pageprop"));
			forwardIndex = HTree.load(recman, recman.getNamedObject("forwardindex"));
			childParent = HTree.load(recman, recman.getNamedObject("childparent"));
			invertedIndex = HTree.load(recman, recman.getNamedObject("invertedindex"));	
            invPT = HTree.load(recman, recman.getNamedObject("invpage"));
            invWT = HTree.load(recman, recman.getNamedObject("invword"));	
            titleIndex = HTree.load(recman, recman.getNamedObject("title"));	
            String key;
			FastIterator wordIter = wordTable.keys();
            FastIterator invertedIter = invertedIndex.keys();
            FastIterator pageIter = pageTable.keys();
            FastIterator propIter = pageProp.keys();
            FastIterator forwardIter = forwardIndex.keys();
            int numDocuments = 0;
            int numWords = 0;
            while ((key = (String)pageIter.next())!=null){
                numDocuments++;
            }
            while((key = (String)wordIter.next())!=null){
                numWords++;
            }
            int numInv = 0;
            while((key = (String)invertedIter.next())!=null){
                // if (numInv < 20){
                //     System.out.println(key);
                //     System.out.println(invertedIndex.get(key));
                // }
                numInv++;
            }
            // System.out.println(numInv);
            // System.out.println(numWords);
            System.out.println("Collection size: " + numDocuments);
            HashMap<String, Integer>[] tf = new HashMap[numDocuments];
            for (int i = 0; i < numDocuments; i++) {
                tf[i] = new HashMap<String, Integer>();
            }
            HashMap<String, Double>[] tfidf = new HashMap[numDocuments];
            for (int i = 0; i < numDocuments; i++) {
                tfidf[i] = new HashMap<String, Double>();
            }
            HashMap<String, Integer> df = new HashMap<>();
            HashMap<String, Integer> maxtf = new HashMap<>();
            // for (String word : tokens){
            //     tokensID.add((String)wordTable.get(stopStem.stem(word)));
            // }
            int max = 0;
            for (int j = 0; j < numDocuments; j++){
                max = 0;
                String content = (String) forwardIndex.get(Integer.toString(j));
                String[] elements = content.substring(1, content.length() - 1).split(", ");
                ArrayList<String> arr = new ArrayList<String>(Arrays.asList(elements));
                String[] in = (String[])pageProp.get("75");
                if (j==75){
                    // System.out.println(in[0]);
                    // System.out.println(arr);
                    // String[] prop = (String[]) pageProp.get("0");
                    // System.out.println(prop[0]);
                }
                for (int i = 0; i < arr.size(); i += 2) {
                    String word = arr.get(i);
                    int freq = Integer.parseInt(arr.get(i+1));
                    // if (j==0 && i ==0){System.out.println(word);}
                    tf[j].put(Integer.toString(i/2), freq);
                    if (max < freq){
                        max = freq;
                    }
                }
                // df.put(Integer.toString(j), arr.size()/2);
                maxtf.put(Integer.toString(j),max);
            }
            // System.out.println(wordTable.get("page"));
            for (int i = 0; i < numInv; i++){
                // System.out.println(invWT.get(Integer.toString(i)));
                // System.out.println(invertedIndex.get(Integer.toString(i)));
                String content = (String) invertedIndex.get(Integer.toString(i));
                String[] arr = content.trim().split(" ");
                if (i ==0 || i==1){System.out.println(arr.length/2);}
                df.put(Integer.toString(i), arr.length/2);
                // if (i < 100){
                //     System.out.println("Page " + i + ": " + arr.length/2 + " occurrences");
                // }
            }
            for (int j = 0; j < numDocuments; j++){
                String content = (String) forwardIndex.get(Integer.toString(j));
                String[] elements = content.substring(1, content.length() - 1).split(", ");
                ArrayList<String> arr = new ArrayList<String>(Arrays.asList(elements));
                double value = 0;
                for (int i = 0; i < arr.size(); i += 2) {
                    // System.out.println(i);
                    // System.out.println(tf[j].get(Integer.toString(i/2)));
                    double t =  tf[j].get(Integer.toString(i/2));
                    double numDoc = numDocuments;
                    double d = Math.log(numDoc/df.get(wordTable.get(arr.get(i))))/(Math.log(2));
                    // System.out.println(df.get(wordTable.get(arr.get(i))));
                    // System.out.println(maxtf.get(Integer.toString(j)));
                    if (j==75){
                        // System.out.println(t);
                        // System.out.println(d);
                    }
                    value = t * d;
                    value = value/maxtf.get(Integer.toString(j));
                    tfidf[j].put(arr.get(i), Double.valueOf(value));
                }
            }
            // for (Map.Entry<String, Double> entry : tfidf[0].entrySet()) {
            //     System.out.println("Word " + entry.getKey() + ": " + entry.getValue() + " occurrences");
            // }
            for (int i = 0; i < numDocuments; i++) {
                double innerProduct = 0.0;
                double doc = 0.0;
                double q = 0.0;
                String content = (String) forwardIndex.get(Integer.toString(i));
                // System.out.println(i);
                String[] elements = content.substring(1, content.length() - 1).split(", ");
                ArrayList<String> arr = new ArrayList<String>(Arrays.asList(elements));
                for (Map.Entry<String, Double> entry : tfidf[i].entrySet()) {
                    // System.out.println(entry.getKey());
                    // System.out.println(tfidf[i].get(entry.getKey()));
                    if (tfidf[i].get(entry.getKey())!=null){
                        doc += Math.pow(tfidf[i].get(entry.getKey()), 2);
                    }
                }
                // System.out.println("h");
                for (Map.Entry<String, Integer> entry : tokenFreqs.entrySet()) {
                    // System.out.println(entry.getKey());
                    if (tfidf[i].get(entry.getKey())!=null){
                        double value = entry.getValue() * tfidf[i].get(entry.getKey());
                        q += Math.pow(entry.getValue(), 2);
                        innerProduct += value;
                    }
                }
                // System.out.println("h2");
                if (i == 75){
                    System.out.println(innerProduct);
                    System.out.println(q);
                    System.out.println(doc);
                }
                if (Math.sqrt(q)*Math.sqrt(doc)!=0){
                    innerProduct = innerProduct/(Math.sqrt(q)*Math.sqrt(doc));
                }
                if (i == 75){System.out.println(innerProduct);}
                for (String token: tokens){
                    String a = (String) invPT.get(Integer.toString(i));
                    // System.out.println(a);
                    String b = (String) titleIndex.get(a);
                    // System.out.println(b);
                    // String titleWords = titleIndex.get((String) invPT.get(Integer.toString(i)));
                    if (b.contains(token)){
                        innerProduct += 0.1;
                    }
                }
                if (innerProduct != 0.0){
                    results.put(Integer.valueOf(i), innerProduct);
                }
                // System.out.println("h3");
            }
        List<Map.Entry<Integer, Double>> sortedResults = new ArrayList<Map.Entry<Integer, Double>>(results.entrySet());
        Collections.sort(sortedResults, new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        int count = 0;
        
        for (Map.Entry<Integer, Double> entry : sortedResults) {
            if (entry.getValue()!=0.0){
                System.out.println(entry.getKey() + " = " + entry.getValue());
                System.out.println(invPT.get(entry.getKey().toString()));
                // results.put(entry.getKey(), entry.getValue());
                result.add(entry.getKey() + " " + entry.getValue());
            }
            // pageIter = pageTable.keys();
            // while ((key = (String)pageIter.next())!=null && count < 50){
            //     if (pageTable.get(key).equals(entry.getKey().toString())){
            //         result.add(key);
            //     }
            // }
            count++;
        }
        // List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(results.entrySet());

        // // Sort the list by value in descending order
        // Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
        //     public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
        //         return o2.getValue().compareTo(o1.getValue());
        //     }
        // });

        // // Put the sorted entries into a LinkedHashMap to maintain the order
        // LinkedHashMap<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
        // for (Map.Entry<Integer, Double> entry : list) {
        //     sortedMap.put(Integer.valueOf((String) invPT.get(entry.getKey())), entry.getValue());
        // }

        // Print the sorted map
        // System.out.println(sortedMap);
        // put in web.jsp
        String pageID;
        String[] prop;
        // for (int i = 0; i < 10; i++){
        //     System.out.println("i = "+i);
        //     if (sortedResults.get(i).getKey()!= null){
        //         System.out.println(sortedResults.get(i).getKey());
        //         if (pageProp.get(sortedResults.get(i).getKey().toString())!=null){
        //             prop = (String[]) pageProp.get(sortedResults.get(i).getKey().toString());
        //             System.out.println(sortedResults.get(i).getValue() + "  " + prop[0]);
        //         }
        //     }
        // }
        // for (String url: result){
        //     System.out.println(url);
        //     String[] in = (String[])pageProp.get(pageTable.get(url));
        //     String title = in[0];
        //     String URL = in[1];
        //     String date = in[2];
        //     String size = in[3];
        //     System.out.println(title);
        //     System.out.println(URL);
        //     System.out.println(date + ", " + size);
        //     System.out.println("--------------------");
        // }
        // System.out.println(result);
        } catch(Exception e){}
        return result;
    }
}
