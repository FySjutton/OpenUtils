package avox.openutils.modules.stats.screen.widgets.boxes;

import avox.openutils.modules.stats.screen.StatViewer;
import avox.openutils.modules.stats.screen.types.ValidTypes;
import avox.openutils.modules.stats.screen.widgets.navigation.StatCategory;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import avox.openutils.modules.stats.FormatTools;
import avox.openutils.modules.stats.screen.widgets.SearchBox;

import java.util.ArrayList;

public class TotalTabBox {
    public SearchBox searchBox;
    private String lastSearchValue;

    public TotalTabBox(MinecraftClient client, int width, int height, StatViewer viewer) {
        searchBox = new SearchBox(client, width, height, viewer, Text.of("Total Statistics"), this::parseJsonToCategories, this::getAPIUrl, this::verifyInput, new ArrayList<>(ValidTypes.totalMessages.keySet().stream().map(this::parseMessage).toList()));
    }

    private String getAPIUrl(String searchValue) {
        lastSearchValue = searchValue;
        StringBuilder APIFetchString = new StringBuilder("https://api.90gqopen.se/total/?type=");
        APIFetchString.append(searchValue.replaceAll(" ", "_").toLowerCase());

        return APIFetchString.toString();
    }

    private ArrayList<StatCategory> parseJsonToCategories(JsonObject data) {
        searchBox.statViewer.specialMessage = String.format(ValidTypes.totalMessages.get(lastSearchValue.replaceAll(" ", "_").toLowerCase()), FormatTools.formatNumber(data.get("total").getAsInt()));

        return new ArrayList<>();
    }

    private boolean verifyInput(String input) {
        return ValidTypes.totalMessages.containsKey(input.replaceAll(" ", "_").toLowerCase());
    }

    private String parseMessage(String message) {
        message = FormatTools.toTitleCase(message);
        message = message
                .replaceAll("Uhc", "UHC")
                .replaceAll("Mb", "MB")
                .replaceAll("Gqmynt", "gQmynt");
        return message;
    }
}