package avox.openutils.modules.stock.screen;

import java.util.LinkedHashMap;

public class FilterTabs {
    public static final LinkedHashMap<String, Boolean> FILTER = createOrdered(
            "Alla", true,
            "Tomma", false,
            "I Lager", false
    );

    public static final LinkedHashMap<String, Boolean> ITEM_FILTER = createOrdered(
            "Böcker", true,
            "Trims", true,
            "Potions", true,
            "Annat", true
    );

    public static final LinkedHashMap<String, Boolean> SORTING = createOrdered(
            "Lager", true,
            "Tjänat", false,
            "Flest Köpta", false,
            "Flest Sålda", false,
            "Antal Transaktioner", false,
            "Datum Skapat", false
    );

    private static LinkedHashMap<String, Boolean> createOrdered(Object... kv) {
        LinkedHashMap<String, Boolean> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], (Boolean) kv[i + 1]);
        }
        return map;
    }
}
