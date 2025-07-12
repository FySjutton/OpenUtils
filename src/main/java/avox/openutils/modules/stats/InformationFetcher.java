package avox.openutils.modules.stats;

import avox.openutils.modules.stats.screen.StatViewer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static avox.openutils.OpenUtils.LOGGER;

public class InformationFetcher {
    public static JsonObject fetchFromAPI(String url, StatViewer statViewer) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.body().matches("\\d+")) {
                JsonObject resp = new JsonObject();
                resp.addProperty("total", Integer.parseInt(response.body()));
                return resp;
            }
            try {
                JsonObject parsedData = JsonParser.parseString(response.body()).getAsJsonObject();
                if (parsedData.getAsJsonObject().has("error")) {
                    statViewer.drawMessage = true;
                    statViewer.specialMessage = "§c" + parsedData.get("error").getAsString();
                    return null;
                }
                return parsedData;
            } catch (Exception e) {
                e.printStackTrace();
                statViewer.drawMessage = true;
                statViewer.specialMessage = "§c" + response.body();
                LOGGER.error("Fetching information failed with: " + response.body() + "\"");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statViewer.drawMessage = true;
            statViewer.specialMessage = "§cUnknown error occurred!";
            LOGGER.error("Fetching information failed");
        }
        return null;
    }
}