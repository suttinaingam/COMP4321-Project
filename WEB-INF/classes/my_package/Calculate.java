package my_package;
import jdbm.RecordManager;
import IRUtilities.*;
import StopStem.*;
import java.util.*;

public class Calculate {
    public static String retrieval(String[] query){
        // RecordManager recman = null;
        // Vector<String> wordVector = new Vector<String>();
        // StopStem stopStem = new StopStem("stopwords.txt");
        // for (int i = 0; i < query.length; i++){
		// 	query[i] = query[i].replace("\n", "").replace("\r", "");
		// 	query[i] = query[i].replaceAll("[())]", "");
		// 	query[i] = query[i].replaceAll("[^a-zA-Z]+", "");
		// 	query[i] = query[i].toLowerCase();
		// 	if (!stopStem.isStopWord(query[i])){
		// 		wordVector.add(stopStem.stem(query[i]));
		// 	}
		// }
        // String[] array = wordVector.toArray(new String[wordVector.size()]);
        return "dd";
    }
}
