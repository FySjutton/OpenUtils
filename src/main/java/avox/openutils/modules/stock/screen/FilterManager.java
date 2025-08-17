package avox.openutils.modules.stock.screen;

import avox.openutils.modules.stats.FormatTools;
import avox.openutils.modules.stock.StockItem;
import net.minecraft.item.Items;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static avox.openutils.modules.stock.StockModule.stockItems;
import static avox.openutils.modules.stock.screen.StockScreen.options;

public class FilterManager {
    public static List<StockItem> filterItems(String search) {
        HashMap<String, Boolean> filterOptions = options.get("Filtrering");
        List<StockItem> filteredItems;
        if (!search.isEmpty()) {
            filteredItems = stockItems.stream().filter(item -> item.name.getString().toLowerCase().contains(search)).toList();
        } else {
            filteredItems = new ArrayList<>(stockItems);
        }

        if (filterOptions.get("I Lager")) {
            filteredItems = filteredItems.stream().filter(item -> item.inStock).toList();
        } else if (filterOptions.get("Tomma")) {
            filteredItems = filteredItems.stream().filter(item -> !item.inStock).toList();
        }

        HashMap<String, Boolean> itemFilterOptions = options.get("Item Filtrering");

        List<StockItem> finalFilteredItems = new ArrayList<>();

        if (itemFilterOptions.getOrDefault("Böcker", false)) {
            finalFilteredItems.addAll(filteredItems.stream()
                    .filter(item -> item.itemStack.item == Items.ENCHANTED_BOOK)
                    .toList());
        }

        if (itemFilterOptions.getOrDefault("Trims", false)) {
            finalFilteredItems.addAll(filteredItems.stream()
                    .filter(item -> item.itemStack.item instanceof SmithingTemplateItem)
                    .toList());
        }

        if (itemFilterOptions.getOrDefault("Potions", false)) {
            finalFilteredItems.addAll(filteredItems.stream()
                    .filter(item -> List.of(Items.POTION, Items.LINGERING_POTION, Items.SPLASH_POTION)
                            .contains(item.itemStack.item))
                    .toList());
        }

        if (itemFilterOptions.getOrDefault("Annat", false)) {
            finalFilteredItems.addAll(filteredItems.stream()
                    .filter(item ->
                            item.itemStack.item != Items.ENCHANTED_BOOK &&
                                    !(item.itemStack.item instanceof SmithingTemplateItem) &&
                                    !List.of(Items.POTION, Items.LINGERING_POTION, Items.SPLASH_POTION).contains(item.itemStack.item)
                    )
                    .toList());
        }
        return finalFilteredItems;
    }

    public static List<StockItem> sortItems(List<StockItem> filteredItems) {
        HashMap<String, Boolean> sortingOptions = options.get("Sortering");
        if (sortingOptions.get("Lager")) {
            filteredItems.sort((a, b) -> Integer.compare(b.storage, a.storage));
        } else if (sortingOptions.get("Tjänat")) {
            filteredItems.sort((a, b) -> Integer.compare(b.earned, a.earned));
        } else if (sortingOptions.get("Flest Köpta")) {
            filteredItems.sort((a, b) -> Integer.compare(b.bought, a.bought));
        } else if (sortingOptions.get("Flest Sålda")) {
            filteredItems.sort((a, b) -> Integer.compare((b.transactions - b.bought), (a.transactions - a.bought)));
        } else if (sortingOptions.get("Antal Transaktioner")) {
            filteredItems.sort((a, b) -> Integer.compare(b.transactions, a.transactions));
        } else if (sortingOptions.get("Datum Skapat")) {
            filteredItems.sort((a, b) -> b.created.compareTo(a.created));
        }
        return filteredItems;
    }

    public static Text getInfoText(StockItem stockItem) {
        HashMap<String, Boolean> sortingOptions = options.get("Sortering");
        if (sortingOptions.get("Lager")) {
            return Text.of("§eLager:§f " + stockItem.storage + " st");
        } else if (sortingOptions.get("Tjänat")) {
            return Text.of("§eTjänat:§f " + FormatTools.formatNumber(stockItem.earned) + " kr");
        } else if (sortingOptions.get("Flest Köpta")) {
            return Text.of("§eTotalt köpta:§f " + stockItem.bought + " st");
        } else if (sortingOptions.get("Flest Sålda")) {
            return Text.of("§eTotalt sålda:§f " + (stockItem.transactions - stockItem.bought) + " st");
        } else if (sortingOptions.get("Antal Transaktioner")) {
            return Text.of("§eAntal transaktioner:§f " + stockItem.transactions + " st");
        } else if (sortingOptions.get("Datum Skapat")) {
            return Text.of("§eSkapades:§f " + stockItem.created);
        }
        return Text.of("Okänd");
    }
}
