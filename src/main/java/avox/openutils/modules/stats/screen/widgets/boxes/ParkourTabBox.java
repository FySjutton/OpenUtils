package avox.openutils.modules.stats.screen.widgets.boxes;

import avox.openutils.modules.stats.screen.StatViewer;
import avox.openutils.modules.stats.screen.types.EntryType;
import avox.openutils.modules.stats.screen.types.ValidTypes;
import avox.openutils.modules.stats.screen.widgets.navigation.StatCategory;
import avox.openutils.modules.stats.screen.widgets.navigation.StatEntry;
import avox.openutils.modules.stats.screen.widgets.navigation.StatFolder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import avox.openutils.modules.stats.screen.widgets.SearchBox;

import java.util.ArrayList;

public class ParkourTabBox {
    public SearchBox searchBox;

    public ParkourTabBox(MinecraftClient client, int width, int height, StatViewer viewer) {
        searchBox = new SearchBox(client, width, height, viewer, Text.of("Parkour Statistics"), this::parseJsonToCategories, this::getAPIUrl, this::verifyInput, ValidTypes.parkours);
        searchBox.addEntry(EntryType.Slider, "");
    }

    private String getAPIUrl(String searchValue) {
        StringBuilder APIFetchString = new StringBuilder("https://api.90gqopen.se/parkour/?name=");
        APIFetchString.append(searchValue.replaceAll(" ", "_").toLowerCase());

        int sliderValue = searchBox.children().get(2).sliderWidget.getDisplayScaled();
        if (sliderValue != 10) {
            APIFetchString.append("&length=").append(sliderValue);
        }

        return APIFetchString.toString();
    }

    private ArrayList<StatCategory> parseJsonToCategories(JsonObject data) {
        StatCategory infoCategory = new StatCategory("parkour info");
        ArrayList<StatCategory> categories = new ArrayList<>();
        for (String key : data.keySet()) {
            if (data.get(key).isJsonArray()) {
                if (key.equals("builders")) {
                    StatFolder builders = new StatFolder("builders");
                    for (JsonElement element : data.get(key).getAsJsonArray()) {
                        builders.entries.add(new StatEntry(element.getAsJsonObject().get("username").getAsString(), ""));
                    }
                    infoCategory.folders.add(builders);
                } else {
                    StatCategory leaderboard = new StatCategory(key);
                    int rank = 0;
                    for (JsonElement element : data.get(key).getAsJsonArray()) {
                        rank ++;
                        JsonObject entry = element.getAsJsonObject();
                        leaderboard.directStats.add(new StatEntry("#" + rank + " " + entry.get("username").getAsString(), entry.get(key.equals("leaderboard_speed") ? "time" : "completions").getAsString()));
                    }
                    categories.add(leaderboard);
                }
            } else {
                infoCategory.directStats.add(new StatEntry(key, data.get(key).getAsString()));
            }
        }

        categories.addFirst(infoCategory);

        return categories;
    }

    private boolean verifyInput(String input) {
        return ValidTypes.parkours.stream().map(String::toLowerCase).toList().contains(input.toLowerCase());
    }
}