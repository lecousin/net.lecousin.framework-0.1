package net.lecousin.framework.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class MapUtil {

    public static <KEY,VALUE> LinkedHashMap<KEY,VALUE> move_above(LinkedHashMap<KEY,VALUE> map, KEY key) {
        ArrayList<Map.Entry<KEY,VALUE>> entries = new ArrayList<Map.Entry<KEY,VALUE>>(map.size());
        
        boolean found = false;
        for (Iterator<Map.Entry<KEY,VALUE>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<KEY,VALUE> entry = it.next();
            if (!found && entry.getKey().equals(key)) {
                found = true;
                if (entries.size() > 0)
                    entries.add(entries.size() - 1, entry);
                else
                    entries.add(entry);
            } else
                entries.add(entry);
        }
        LinkedHashMap<KEY,VALUE> result = new LinkedHashMap<KEY,VALUE>(entries.size());
        for (Iterator<Map.Entry<KEY,VALUE>> it = entries.iterator(); it.hasNext(); ) {
            Map.Entry<KEY,VALUE> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static <KEY,VALUE> LinkedHashMap<KEY,VALUE> move_below(LinkedHashMap<KEY,VALUE> map, KEY key) {
        ArrayList<Map.Entry<KEY,VALUE>> entries = new ArrayList<Map.Entry<KEY,VALUE>>(map.size());
        
        Map.Entry<KEY,VALUE> e = null;
        boolean nextPassed = false;
        for (Iterator<Map.Entry<KEY,VALUE>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<KEY,VALUE> entry = it.next();
            if (e != null) {
                if (nextPassed) {
                    entries.add(e);
                    e = null;
                } else
                    nextPassed = true;
            }
            if (e == null && entry.getKey().equals(key)) {
                e = entry;
                nextPassed = false;
            } else
                entries.add(entry);
        }
        LinkedHashMap<KEY,VALUE> result = new LinkedHashMap<KEY,VALUE>(entries.size());
        for (Iterator<Map.Entry<KEY,VALUE>> it = entries.iterator(); it.hasNext(); ) {
            Map.Entry<KEY,VALUE> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
