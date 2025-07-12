package avox.openutils.modules.stats;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Reordering {
    public static JsonObject reorderKeys(JsonObject original, List<String> customOrder, boolean putFirst) {
        JsonObject reordered = new JsonObject();
        Set<String> inserted = new HashSet<>();

        if (putFirst) {
            // Add customOrder keys first
            for (String key : customOrder) {
                if (original.has(key)) {
                    reordered.add(key, original.get(key));
                    inserted.add(key);
                }
            }
            // Then the rest
            for (Map.Entry<String, JsonElement> entry : original.entrySet()) {
                if (!inserted.contains(entry.getKey())) {
                    reordered.add(entry.getKey(), entry.getValue());
                }
            }
        } else {
            boolean insertedCustom = false;
            for (Map.Entry<String, JsonElement> entry : original.entrySet()) {
                String key = entry.getKey();

                if (customOrder.contains(key) && !insertedCustom) {
                    for (String customKey : customOrder) {
                        if (original.has(customKey)) {
                            reordered.add(customKey, original.get(customKey));
                            inserted.add(customKey);
                        }
                    }
                    insertedCustom = true;
                } else if (!inserted.contains(key)) {
                    reordered.add(key, entry.getValue());
                }
            }
        }

        return reordered;
    }
}