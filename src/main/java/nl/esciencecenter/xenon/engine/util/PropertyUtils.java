package nl.esciencecenter.xenon.engine.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class PropertyUtils {
    
    public static Map<String, String> extract(Map<String, String> source, String prefix) {

    	if (source == null || source.size() == 0) { 
    		return new HashMap<String,String>(0);
    	}

    	HashMap<String, String> tmp = new HashMap<>(source.size());

        Iterator<Entry<String, String>> itt = source.entrySet().iterator();

        while (itt.hasNext()) {

            Entry<String, String> e = itt.next();

            if (e.getKey().startsWith(prefix)) {
                tmp.put(e.getKey(), e.getValue());
                itt.remove();
            }
        }

        return tmp;
    }
}


