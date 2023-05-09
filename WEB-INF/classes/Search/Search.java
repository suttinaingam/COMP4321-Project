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
        HashMap<Integer, Double> results = new HashMap<Integer, Double>();
        ArrayList<String> result = new ArrayList<>();
        String catalinaHome = System.getenv("CATALINA_HOME");
        StopStem stopStem = new StopStem(catalinaHome + "/webapps/comp4321/WEB-INF/classes/stopwords.txt");
        query = query.trim();
        ArrayList<String> wordQuery = new ArrayList<>();
        ArrayList<String> phraseQuery = new ArrayList<>();
        int firstIndex = 0;
        int firstSign = 1000;
        int endSign = 1000;
        for (int i = 0; i < query.length(); i++){
            if (query.charAt(i)==' ' || i==query.length()-1){
                if (firstIndex < i+1 && endSign!=0){
                    System.out.println(firstIndex);
                    System.out.println(i);
                    if (i==query.length()-1){
                        if (query.substring(firstIndex, i+1)!=""){
                            wordQuery.add(query.substring(firstIndex, i+1));
                        }
                    }
                    else{
                        if (query.substring(firstIndex, i)!=""){
                            wordQuery.add(query.substring(firstIndex, i));
                        }
                    }
                    firstIndex = i+1;
                }
            }
            if (query.charAt(i)=='\"'){
                if (firstSign != 1000){
                    endSign = i;
                    phraseQuery.add(query.substring(firstSign+1, endSign));
                    firstSign = 1000;
                    endSign = 1000;
                    firstIndex = i+2;
                }
                else{
                    firstSign = i;
                    endSign = 0;
                }
            }
        }

        // System.out.println(wordQuery);
        // System.out.println(phraseQuery);
        // ArrayList<String> p = new ArrayList<>();
        // String[] words = query.split("[ \\n]");
		// for (int i = 0; i < words.length; i++){
		// 	words[i] = words[i].replace("\n", "").replace("\r", "");
		// 	words[i] = words[i].replaceAll("[())]", "");
		// 	words[i] = words[i].replaceAll("[^a-zA-Z]+", "");
		// 	words[i] = words[i].toLowerCase();
		// 	if (words[i]!="" && !stopStem.isStopWord(words[i])){
		// 		tokens.add(stopStem.stem(words[i]));
		// 	}
		// }
        // for (String token: tokens){
        //     System.out.println(token);
        // }
        String[] words = new String[wordQuery.size()];
        ArrayList<String> tokens = new ArrayList<String>();
        words = wordQuery.toArray(words);
		for (int i = 0; i < words.length; i++){
			words[i] = words[i].replace("\n", "").replace("\r", "");
			words[i] = words[i].replaceAll("[())]", "");
			words[i] = words[i].replaceAll("[^a-zA-Z]+", "");
			words[i] = words[i].toLowerCase();
			if (words[i]!="" && !stopStem.isStopWord(words[i])){
				tokens.add(stopStem.stem(words[i]));
			}
		}
        // System.out.println(tokens);
        // Create a HashMap to store the token frequencies
        HashMap<String, Integer> tokenFreqs = new HashMap<String, Integer>();

        // Iterate over the tokens and update the token frequencies
        for (int i = 0; i < tokens.size(); i++) {
            int currentFreq = tokenFreqs.getOrDefault(tokens.get(i), 0);
            tokenFreqs.put(tokens.get(i), currentFreq + 1);
        }
        ArrayList<String> phrase = new ArrayList<String>();
        String[] phrases = new String[phraseQuery.size()];
        phrases = phraseQuery.toArray(phrases);
		for (int i = 0; i < phrases.length; i++){
            String[] p = phrases[i].split(" ");
            String combine = "";
            System.out.println(p.length);
            System.out.println(p);
            for (int j = 0; j < p.length; j++){
                p[j] = p[j].replace("\n", "").replace("\r", "");
                p[j] = p[j].replaceAll("[())]", "");
                p[j] = p[j].replaceAll("[^a-zA-Z]+", "");
                p[j] = p[j].toLowerCase();
                if (p[j]!="" && !stopStem.isStopWord(p[j])){
                    System.out.println(combine);
                    combine = combine + stopStem.stem(phrases[j]);
                }
                if (j!=p.length-1){
                    System.out.println(combine);
                    combine += "_";
                    System.out.println(combine);
                }
            }
            phrase.add(combine);
		}
        System.out.println(phrase);
        // Create a HashMap to store the token frequencies
        HashMap<String, Integer> phraseFreqs = new HashMap<String, Integer>();

        // Iterate over the tokens and update the phrase frequencies
        for (int i = 0; i < phrase.size(); i++) {
            int currentFreq = phraseFreqs.getOrDefault(phrase.get(i), 0);
            phraseFreqs.put(phrase.get(i), currentFreq + 1);
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
        HTree phraseTable = null;
        HTree invertedPhrase = null;
        HTree forwardPhrase = null;
        HTree invWTP = null;
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
            phraseTable = HTree.load(recman, recman.getNamedObject("phrase"));
            invertedPhrase = HTree.load(recman, recman.getNamedObject("iphrase"));
            forwardPhrase = HTree.load(recman, recman.getNamedObject("fphrase"));
            invWTP = HTree.load(recman, recman.getNamedObject("invphrase"));
            String key;
			FastIterator wordIter = wordTable.keys();
            FastIterator invertedIter = invertedIndex.keys();
            FastIterator pageIter = pageTable.keys();
            FastIterator propIter = pageProp.keys();
            FastIterator forwardIter = forwardIndex.keys();
            FastIterator phraseIter = phraseTable.keys();
            FastIterator fphraseIter = forwardPhrase.keys();
            FastIterator iphraseIter = invertedPhrase.keys();
            // while ((key = (String)phraseIter.next())!=null){
            //     System.out.println(key + " " + phraseTable.get(key));
            // }
            int c = 0;
            // while ((key = (String)fphraseIter.next())!=null){
            //     if (c < 1){
            //         System.out.println(key + " " + forwardPhrase.get(key));
            //         c++;
            //     }
            // }
            c = 0;
            // while ((key = (String)iphraseIter.next())!=null){
            //     if (c < 1){
            //         System.out.println(key + " " + invertedPhrase.get(key));
            //         c++;
            //     }
            // }
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
            int numInvP = 0;
            while((key = (String)iphraseIter.next())!=null){
                // if (numInv < 20){
                //     System.out.println(key);
                //     System.out.println(invertedIndex.get(key));
                // }
                numInvP++;
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
                // if (i ==0 || i==1){System.out.println(arr.length/2);}
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
                // System.out.println(arr);
                for (int i = 0; i < arr.size(); i += 2) {
                    // System.out.println(i);
                    // System.out.println(tf[j].get(Integer.toString(i/2)));
                    double t =  tf[j].get(Integer.toString(i/2));
                    double numDoc = numDocuments;
                    // System.out.println(arr.get(i));
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
                    // System.out.println(innerProduct);
                    // System.out.println(q);
                    // System.out.println(doc);
                }
                if (Math.sqrt(q)*Math.sqrt(doc)!=0){
                    innerProduct = innerProduct/(Math.sqrt(q)*Math.sqrt(doc));
                }
                // if (i == 75){System.out.println(innerProduct);}
                int count = 0;
                for (String token: tokens){
                    String a = (String) invPT.get(Integer.toString(i));
                    // System.out.println(a);
                    String b = (String) titleIndex.get(a);
                    String[] temp = b.split(" ");
                    // if (i==70){
                    //     for (String t: temp){
                    //         System.out.print(t);
                    //     }
                    //     System.out.println("h" + temp.length/2);
                    // }
                    // System.out.println("length = " + temp.length/2);
                    // System.out.println(b);
                    // String titleWords = titleIndex.get((String) invPT.get(Integer.toString(i)));
                    if (b.contains(token)){
                        innerProduct += 1/temp.length;
                        count++;
                    }
                    // if (i==70){System.out.println(tokens.size());System.out.println(count);}
                    // System.out.println(temp.length/2);
                    if (count == tokens.size()){
                        innerProduct += 1;
                    }
                }
                if (innerProduct != 0.0){
                    results.put(Integer.valueOf(i), innerProduct);
                }
                // System.out.println("h3");
            }

            // phrase calculation
            HashMap<String, Integer>[] tfP = new HashMap[numDocuments];
            for (int i = 0; i < numDocuments; i++) {
                tfP[i] = new HashMap<String, Integer>();
            }
            HashMap<String, Double>[] tfidfP = new HashMap[numDocuments];
            for (int i = 0; i < numDocuments; i++) {
                tfidfP[i] = new HashMap<String, Double>();
            }
            HashMap<String, Integer> dfP = new HashMap<>();
            HashMap<String, Integer> maxtfP = new HashMap<>();
            // for (String word : tokens){
            //     tokensID.add((String)wordTable.get(stopStem.stem(word)));
            // }
            for (int j = 0; j < numDocuments; j++){
                max = 0;
                // System.out.println(j);
                // System.out.println(forwardPhrase.get(Integer.toString(j)));
                String content = (String) forwardPhrase.get(Integer.toString(j));
                String[] elements = content.substring(1, content.length() - 1).split(" ");
                // System.out.println(elements.length);
                ArrayList<String> arr = new ArrayList<String>(Arrays.asList(elements));
                for (int i = 0; i < arr.size(); i += 2) {
                    String word = arr.get(i);
                    // System.out.println(word);
                    int freq = Integer.parseInt(arr.get(i+1));
                    // System.out.println(freq);
                    // if (j==0 && i ==0){System.out.println(word);}
                    tfP[j].put(Integer.toString(i/2), freq);
                    // System.out.println("end");
                    if (max < freq){
                        max = freq;
                    }
                    // System.out.println("end");
                }
                // df.put(Integer.toString(j), arr.size()/2);
                maxtfP.put(Integer.toString(j),max);
            }
            for (int i = 0; i < numInvP; i++){
                // System.out.println(invWT.get(Integer.toString(i)));
                // System.out.println(invertedIndex.get(Integer.toString(i)));
                String content = (String) invertedPhrase.get(Integer.toString(i));
                String[] arr = content.trim().split(" ");
                // if (i ==0 || i==1){System.out.println(arr.length/2);}
                dfP.put(Integer.toString(i), arr.length/2);
                // if (i < 100){
                //     System.out.println("Page " + i + ": " + arr.length/2 + " occurrences");
                // }
            }
            for (int j = 0; j < numDocuments; j++){
                String content = (String) forwardPhrase.get(Integer.toString(j));
                String[] elements = content.substring(0, content.length() - 1).split(" ");
                ArrayList<String> arr = new ArrayList<String>(Arrays.asList(elements));
                double value = 0;
                for (int i = 0; i < arr.size(); i += 2) {
                    // System.out.println(i);
                    // System.out.println(tf[j].get(Integer.toString(i/2)));
                    double t =  tfP[j].get(Integer.toString(i/2));
                    double numDoc = numDocuments;
                    // System.out.println(arr);
                    // System.out.println(arr.get(i));
                    // System.out.println(phraseTable.get(arr.get(i)));
                    // System.out.println(dfP.get(phraseTable.get(arr.get(i))));
                    double d = Math.log(numDoc/dfP.get(phraseTable.get(arr.get(i))))/(Math.log(2));
                    // System.out.println("end");
                    // System.out.println(df.get(wordTable.get(arr.get(i))));
                    // System.out.println(maxtf.get(Integer.toString(j)));
                    if (j==75){
                        // System.out.println(t);
                        // System.out.println(d);
                    }
                    value = t * d;
                    value = value/maxtfP.get(Integer.toString(j));
                    tfidfP[j].put(arr.get(i), Double.valueOf(value));
                }
            }
            // for (Map.Entry<String, Double> entry : tfidf[0].entrySet()) {
            //     System.out.println("Word " + entry.getKey() + ": " + entry.getValue() + " occurrences");
            // }
            for (int i = 0; i < numDocuments; i++) {
                double innerProduct = 0.0;
                double doc = 0.0;
                double q = 0.0;
                if (results.get(Integer.toString(i))!=null){
                    innerProduct = results.get(Integer.toString(i));
                }
                String content = (String) forwardPhrase.get(Integer.toString(i));
                // System.out.println(i);
                String[] elements = content.substring(1, content.length() - 1).split(", ");
                ArrayList<String> arr = new ArrayList<String>(Arrays.asList(elements));
                for (Map.Entry<String, Double> entry : tfidfP[i].entrySet()) {
                    // System.out.println(entry.getKey());
                    // System.out.println(tfidf[i].get(entry.getKey()));
                    if (tfidfP[i].get(entry.getKey())!=null){
                        doc += Math.pow(tfidfP[i].get(entry.getKey()), 2);
                    }
                }
                // System.out.println("h");
                for (Map.Entry<String, Integer> entry : phraseFreqs.entrySet()) {
                    // System.out.println(entry.getKey());
                    if (tfidfP[i].get(entry.getKey())!=null){
                        double value = entry.getValue() * tfidfP[i].get(entry.getKey());
                        q += Math.pow(entry.getValue(), 2);
                        innerProduct += value;
                    }
                }
                // System.out.println("h2");
                if (i == 75){
                    // System.out.println(innerProduct);
                    // System.out.println(q);
                    // System.out.println(doc);
                }
                if (Math.sqrt(q)*Math.sqrt(doc)!=0){
                    innerProduct = innerProduct/(Math.sqrt(q)*Math.sqrt(doc));
                }
                // if (i == 75){System.out.println(innerProduct);}
                int count = 0;
                for (String token: phrase){
                    String a = (String) invPT.get(Integer.toString(i));
                    // System.out.println(a);
                    String b = (String) titleIndex.get(a);
                    String[] temp = b.split(" ");
                    // if (i==70){
                    //     for (String t: temp){
                    //         System.out.print(t);
                    //     }
                    //     System.out.println("h" + temp.length/2);
                    // }
                    // System.out.println("length = " + temp.length/2);
                    // System.out.println(b);
                    // String titleWords = titleIndex.get((String) invPT.get(Integer.toString(i)));
                    if (b.contains(token)){
                        innerProduct += 1/temp.length;
                        count++;
                    }
                    // if (i==70){System.out.println(tokens.size());System.out.println(count);}
                    // System.out.println(temp.length/2);
                    if (count == phrase.size()){
                        innerProduct += 1;
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
                    // System.out.println(entry.getKey() + " = " + entry.getValue());
                    // System.out.println(invPT.get(entry.getKey().toString()));
                    // results.put(entry.getKey(), entry.getValue());
                    result.add(entry.getKey() + " " + entry.getValue());
                }
                count++;
            }
        } catch(Exception e){}
        return result;
    }
}
