package avox.openutils.modules.worldmap;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import static avox.openutils.OpenUtils.LOGGER;

public class BannerManager {
    public static volatile ArrayList<Banner> banners = new ArrayList<>();
    private static long lastFetchTime = 0;
    private static final long COOLDOWN = 10 * 60 * 1000;
    private static final Gson gson = new Gson();

    public record Banner(String map, int x, int y, String name) {}

    public static void fetchBanners() {
        long now = System.currentTimeMillis();
        if (now - lastFetchTime < COOLDOWN) return;
        lastFetchTime = now;

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://openavox.se/getbanners"))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                banners = gson.fromJson(response.body(), new TypeToken<ArrayList<Banner>>(){}.getType());
            } catch (Exception e) {
                LOGGER.info("[OpenUtils]: Failed to load /warp Worldmap banners! " + e.getMessage());
            }
        }, "BannerFetcherThread").start();
    }
}