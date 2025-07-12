package avox.openutils.modules.stats.screen.widgets.boxes;

import avox.openutils.modules.stats.screen.StatViewer;
import avox.openutils.modules.stats.screen.types.EntryType;
import avox.openutils.modules.stats.screen.types.ValidTypes;
import avox.openutils.modules.stats.screen.widgets.navigation.StatCategory;
import avox.openutils.modules.stats.screen.widgets.navigation.StatEntry;
import avox.openutils.modules.stats.screen.widgets.navigation.StatFolder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import avox.openutils.modules.stats.Reordering;
import avox.openutils.modules.stats.screen.widgets.SearchBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static avox.openutils.OpenUtils.LOGGER;

public class PlayerTabBox {
    public SearchBox searchBox;

    public PlayerTabBox(MinecraftClient client, int width, int height, StatViewer viewer) {
        ArrayList<String> players = new ArrayList<>(client.getNetworkHandler()
                .getPlayerList()
                .stream()
                .map(entry -> entry.getProfile().getName())
                .collect(Collectors.toSet()).stream().toList());
        searchBox = new SearchBox(client, width, height, viewer, Text.of("Player Statistics"), this::parseJsonToCategories, this::getAPIUrl, this::verifyInput, players);

        searchBox.addEntry(EntryType.Checkbox, "Survival Stats");
        searchBox.addEntry(EntryType.Checkbox, "Creative Stats");
        searchBox.addEntry(EntryType.Checkbox, "MB Stats");
        searchBox.addEntry(EntryType.Checkbox, "UHC Stats");
        searchBox.addEntry(EntryType.Checkbox, "Event Stats");
        searchBox.addEntry(EntryType.Checkbox, "Parkour Stats");
    }

    private String getAPIUrl(String searchValue) {
        StringBuilder APIFetchString = new StringBuilder("https://api.90gqopen.se/player/?username=");
        APIFetchString.append(searchValue);

        for (int i = 2; i < searchBox.getEntryCount(); i++) {
            if (searchBox.getEntry(i).checkboxWidget.isChecked()) {
                APIFetchString.append("&").append(searchBox.getEntry(i).checkboxWidget.getMessage().getString().toLowerCase().split(" ")[0]).append("=true");
            }
        }

        return APIFetchString.toString();
    }

    private ArrayList<StatCategory> parseJsonToCategories(JsonObject data) {
        // Resort keys for better order
        if (data.get("survival") != null) {
            if (data.get("survival").isJsonObject()) {
                JsonObject originalSurvival = data.getAsJsonObject("survival");
                if (originalSurvival != null) {
                    data.add("survival", Reordering.reorderKeys(originalSurvival, List.of("money", "level", "experience"), false));
                }
            }
        }

        StatCategory playerCategory = new StatCategory("Player");
        ArrayList<StatCategory> categories = new ArrayList<>();
        for (String key : data.keySet()) {
            if (data.get(key).isJsonObject()) {
                StatCategory newCategory = new StatCategory(key);
                JsonObject inner = data.get(key).getAsJsonObject();

                if (List.of("survival", "creative", "mb").contains(key)) {
                    for (String innerKey : inner.keySet()) {
                        if (innerKey.equals("experience") && key.equals("survival")) {
                            newCategory.directStats.add(new StatEntry("progress", getJsonString(inner.get(innerKey))));
                        }
                        newCategory.directStats.add(new StatEntry(innerKey, getJsonString(inner.get(innerKey))));
                        if (innerKey.equals("wins")) {
                            newCategory.directStats.add(new StatEntry("win_percent", getWinPercent(inner, "")));
                        }
                    }
                } else if (List.of("event", "uhc").contains(key)) {
                    for (String innerKey : inner.keySet()) {
                        List<String> prefixes = List.of(
                                "anvil_", "border_runners_", "dragons_", "infection_", "maze_",
                                "oitc_", "paintball_", "parkour_", "red_rover_", "snow_fight_",
                                "spleef_", "sumo_", "sg_", "tnt_run_",
                                "season_", "alltime_"
                        );

                        Optional<String> match = prefixes.stream()
                                .filter(innerKey::startsWith)
                                .findFirst();

                        if (match.isPresent()) {
                            ArrayList<StatFolder> folder = new ArrayList<>(newCategory.folders.stream().filter(f -> f.id.equals(match.get())).toList());
                            if (folder.isEmpty()) {
                                StatFolder newFolder = new StatFolder(match.get().substring(0, match.get().length() - 1));
                                newFolder.id = match.get();
                                newCategory.folders.add(newFolder);
                                folder.add(newFolder);
                            }
                            folder.getFirst().entries.add(new StatEntry(innerKey, getJsonString(inner.get(innerKey))));
                            if (innerKey.equals(match.get() + "wins")) {
                                folder.getFirst().entries.add(new StatEntry(match.get() + "win_percent", getWinPercent(inner, match.get())));
                            }
                        } else {
                            newCategory.directStats.add(new StatEntry(innerKey, getJsonString(inner.get(innerKey))));
                            if (innerKey.equals("wins")) {
                                newCategory.directStats.add(new StatEntry("win_percent", getWinPercent(inner, "")));
                            }
                        }
                    }
                } else if (key.equals("parkour")) {
                    JsonArray parkourStats = inner.get("parkourStats").getAsJsonArray();
                    newCategory.suffix = " (" + parkourStats.size() + "/" + ValidTypes.parkours.size() + " - " + String.format("%.2f%%", parkourStats.size() * 100.0 / ValidTypes.parkours.size()) + ")";
                    ArrayList<String> finishedParkours = new ArrayList<>();
                    for (JsonElement map : inner.get("parkourStats").getAsJsonArray()) {
                        JsonObject obj = map.getAsJsonObject();
                        StatFolder statFolder = new StatFolder(getJsonString(obj.get("parkour_name")) + " (§a✔§r)");
                        finishedParkours.add(obj.get("parkour_name").getAsString());
                        for (String innerKey : obj.keySet()) {
                            statFolder.entries.add(new StatEntry(innerKey, getJsonString(obj.get(innerKey))));
                        }
                        newCategory.folders.add(statFolder);
                    }
                    for (String other : ValidTypes.parkours) {
                        if (!finishedParkours.contains(other.replaceAll(" ", "_").toLowerCase())) {
                            StatFolder statFolder = new StatFolder(other + " (§c✘§r)");
                            statFolder.entries.add(new StatEntry("parkour_name", other));
                            newCategory.folders.add(statFolder);
                        }
                    }
                }
                categories.add(newCategory);
            } else {
                playerCategory.directStats.add(new StatEntry(key, getJsonString(data.get(key))));
            }
        }

        categories.addFirst(playerCategory);

        return categories;
    }

    private String getWinPercent(JsonObject object, String prefix) {
        int gamesPlayed = -1;
        int wins = -1;
        if (object.has(prefix + "games_played")) {
            gamesPlayed = object.get(prefix + "games_played").getAsInt();
        }
        if (object.has(prefix + "wins")) {
            wins = object.get(prefix + "wins").getAsInt();
        }
        if (wins != -1 && gamesPlayed != -1 && gamesPlayed != 0) {
            return String.format("%.3g", (double) wins / gamesPlayed * 100) + "%";
        }
        return "?";
    }

    private String getJsonString(JsonElement elm) {
        if (elm.isJsonNull()) {
            return "N/A";
        } else {
            return elm.getAsString();
        }
    }

    private boolean verifyInput(String input) {
        return input.matches("\\w{3,16}");
    }
}