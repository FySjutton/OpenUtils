package avox.openutils.modules.stats.screen.widgets.boxes;

import avox.openutils.modules.stats.screen.StatViewer;
import avox.openutils.modules.stats.screen.types.EntryType;
import avox.openutils.modules.stats.screen.widgets.navigation.StatCategory;
import avox.openutils.modules.stats.screen.widgets.navigation.StatEntry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import avox.openutils.modules.stats.Reordering;
import avox.openutils.modules.stats.screen.widgets.SearchBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuildTabBox {
    public SearchBox searchBox;

    private final ArrayList<String> guildNames = new ArrayList<>(List.of("Avox"));

    public GuildTabBox(MinecraftClient client, int width, int height, StatViewer viewer) {
        searchBox = new SearchBox(client, width, height, viewer, Text.of("Guild Statistics"), this::parseJsonToCategories, this::getAPIUrl, this::verifyInput, guildNames);

        searchBox.addEntry(EntryType.Checkbox, "View Memberlist");
    }

    private String getAPIUrl(String searchValue) {
        StringBuilder APIFetchString = new StringBuilder("https://api.90gqopen.se/guild/?name=");
        APIFetchString.append(searchValue.toLowerCase());

        if (searchBox.getEntry(2).checkboxWidget.isChecked()) {
            APIFetchString.append("&memberlist=true");
        }

        return APIFetchString.toString();
    }

    private ArrayList<StatCategory> parseJsonToCategories(JsonObject data) {
        data = Reordering.reorderKeys(data, List.of("guild_level", "experience"), false);

        StatCategory infoCategory = new StatCategory("guild");
        ArrayList<StatCategory> categories = new ArrayList<>();
        for (String key : data.keySet()) {
            if (key.equals("memberlist")) {
                StatCategory memberList = new StatCategory("members");

                List<JsonObject> entries = new ArrayList<>();
                for (JsonElement element : data.get(key).getAsJsonArray()) {
                    entries.add(element.getAsJsonObject());
                }

                Map<String, Integer> rolePriority = Map.of(
                        "owner", 0,
                        "moderator", 1,
                        "member", 2
                );

                entries.sort((a, b) -> {
                    String roleA = a.get("role").getAsString();
                    String roleB = b.get("role").getAsString();
                    int roleCompare = Integer.compare(rolePriority.getOrDefault(roleA, 99), rolePriority.getOrDefault(roleB, 99));
                    if (roleCompare != 0) return roleCompare;

                    String nameA = a.get("name").getAsString().toLowerCase();
                    String nameB = b.get("name").getAsString().toLowerCase();
                    return nameA.compareTo(nameB);
                });

                for (JsonObject player : entries) {
                    memberList.directStats.add(new StatEntry(player.get("name").getAsString(), player.get("role").getAsString()));
                }
                categories.add(memberList);
            } else {
                if (key.equals("experience")) {
                    infoCategory.directStats.add(new StatEntry("progress", data.get(key).getAsString()));
                }
                infoCategory.directStats.add(new StatEntry(key, data.get(key).getAsString()));
            }
        }

        categories.addFirst(infoCategory);

        return categories;
    }

    private boolean verifyInput(String input) {
//        return guildNames.stream().map(String::toLowerCase).toList().contains(input.toLowerCase()); // When /list
        return !input.isEmpty();
    }
}