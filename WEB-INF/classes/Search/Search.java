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

    public ArrayList<String> retrieval_fun(String query){
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> split = new ArrayList<String>(Arrays.asList(query.split(" ")));
        ArrayList<String> tokens = new ArrayList<String>();
        StopStem stopStem = new StopStem("stopwords.txt");
        for (String token: split){
            if (!stopStem.isStopWord(token)){
                tokens.add(stopStem.stem(token));
            }
        }

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
        String catalinaHome = System.getenv("CATALINA_HOME");
		try {
			recman = RecordManagerFactory.createRecordManager(catalinaHome + "/bin/assets/project");
			pageTable = HTree.load(recman, recman.getNamedObject("page"));
			wordTable = HTree.load(recman, recman.getNamedObject("word"));
			pageProp = HTree.load(recman, recman.getNamedObject("pageprop"));
			forwardIndex = HTree.load(recman, recman.getNamedObject("forwardindex"));
			childParent = HTree.load(recman, recman.getNamedObject("childparent"));
			invertedIndex = HTree.load(recman, recman.getNamedObject("invertedindex"));	
            String key;
			FastIterator wordIter = wordTable.keys();
            FastIterator invertedIter = invertedIndex.keys();
            FastIterator pageIter = pageTable.keys();
            FastIterator propIter = pageProp.keys();
            FastIterator forwardIter = forwardIndex.keys();
            int numDocuments = 0;
            while ((key = (String)pageIter.next())!=null){
                // System.out.println(key);
                // System.out.println(pageTable.get(key));
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
            HashMap<String, Integer> maxtf = new HashMap<>();
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
            int max;
            for (int i = 0; i < numDocuments; i++){
                max = 0;
                for (int j = 0; j < tokens.size(); j++){
                    if (tf[j].get("p"+i)!=null){
                        if (max < tf[j].get("p"+i)){
                            max = tf[j].get("p"+i);
                        }
                    }
                }
                maxtf.put("p"+i,max);
            }
            String value;
            for (int i = 0; i < numDocuments; i++){
                for (int j = 0; j < tokens.size(); j++){
                    // if (i == 0){
                    //     System.out.println(tf[j].get("p"+i));
                    //     System.out.println(Integer.valueOf(df.get(Integer.toString(j))));
                    //     double d = df.get(Integer.toString(j));
                    //     System.out.println(Math.log(d)/Math.log(2));
                    // }
                    if (tf[j].get("p"+i) == null){
                        tfidf[i].put(Integer.toString(j), Double.valueOf(0) );
                    }
                    else{
                        // System.out.println("Max" + maxtf.get("p"+i));
                        double d = df.get(Integer.toString(j));
                        double d2 = maxtf.get("p"+i);
                        if (i == 0){
                            double r = tf[j].get("p"+i) * Math.log(numDocuments/d)/Math.log(2);
                            double r2 = r/d2;
                            // System.out.println("d2 = " + d2);
                            // System.out.println("d = " + d);
                            // System.out.println("tfidf = " + r2);
                        }
                        tfidf[i].put(Integer.toString(j), (tf[j].get("p"+i) * Math.log(numDocuments/d))/(Math.log(2)*d2));
                    }
                }
            }
            // for (Map.Entry<String, Double> entry : tfidf[0].entrySet()) {
            //     System.out.println("Word " + entry.getKey() + ": " + entry.getValue() + " occurrences");
            // }
            
            HashMap<Integer, Double> results = new HashMap<Integer, Double>();
            for (int i = 0; i < numDocuments - 1; i++) {
                double innerProduct = 0.0;
                double doc = 0.0;
                double q = 0.0;
                for (int j = 0; j < tokens.size(); j++){
                    // if (i == 0){
                    //     System.out.println("freq = " + tokenFreqs.get(Integer.toString(j)));
                    //     System.out.println("tfidf = "+ tfidf[i].get(Integer.toString(j)));
                    // }
                    innerProduct += tokenFreqs.get(Integer.toString(j)) * tfidf[i].get(Integer.toString(j));
                    q += Math.pow(tokenFreqs.get(Integer.toString(j)), 2);
                    doc += Math.pow( tfidf[i].get(Integer.toString(j)), 2);
                }
                // if (i == 0){
                //     System.out.println("q = " + q );
                //     System.out.println("doc = " + doc );
                // }
                // System.out.println("Inner = " + innerProduct);
                if (q!= 0 && doc!=0){
                    innerProduct = innerProduct/(Math.sqrt(q)*Math.sqrt(doc));
                    results.put(Integer.valueOf(i), innerProduct);
                }
            }
            List<Map.Entry<Integer, Double>> sortedResults = new ArrayList<Map.Entry<Integer, Double>>(results.entrySet());
            Collections.sort(sortedResults, new Comparator<Map.Entry<Integer, Double>>() {
                public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            int count = 0;
            for (Map.Entry<Integer, Double> entry : sortedResults) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
                pageIter = pageTable.keys();
                while ((key = (String)pageIter.next())!=null && count < 50){
                    if (pageTable.get(key).equals(entry.getKey().toString())){
                        result.add(key);
                    }
                    // if (pageTable.get(key).equals("12")){
                    //     System.out.println(key);
                    //     System.out.println(invertedIndex.get("12"));
                    //     System.out.println(forwardIndex.get("12"));
                    // }
                }
                count++;
            }
            //put in web.jsp
            // String pageID;
            // String[] prop;
            // for (int i = 0; i < 10; i++){
            //     System.out.println("i = "+i);
            //     if (sortedResults.get(i).getKey()!= null){
            //         System.out.println(sortedResults.get(i).getKey());
            //         if (pageProp.get(sortedResults.get(i).getKey().toString())!=null){
            //             System.out.println(pageProp.get(sortedResults.get(i).getKey().toString()));
            //             prop = (String[]) pageProp.get(sortedResults.get(i).getKey().toString());
            //             System.out.println(sortedResults.get(i).getValue() + "  " + prop[0]);
            //         }
            //     }
            // }
            // while ((key = (String)propIter.next())!=null){
            //     String[] in = (String[])pageProp.get(key);
			// 	String title = in[0];
			// 	String URL = in[1];
			// 	String date = in[2];
			// 	String size = in[3];
			// 	System.out.println(title);
			// 	System.out.println(URL);
			// 	System.out.println(date + ", " + size);
			// 	System.out.println("--------------------");
            // }
            // for (String url: result){
            //     System.out.println(url);
            //     System.out.println(pageProp.get(url));
            //     String[] in = (String[])pageProp.get(url);
			// 	String title = in[0];
			// 	String URL = in[1];
			// 	String date = in[2];
			// 	String size = in[3];
			// 	System.out.println(title);
			// 	System.out.println(URL);
			// 	System.out.println(date + ", " + size);
			// 	System.out.println("--------------------");
            // }
            // System.out.println(result);
        } catch(Exception e){}
        ArrayList<String> r = new ArrayList<String>();
        r.add("Suttinai");
        return result;
    }
}
