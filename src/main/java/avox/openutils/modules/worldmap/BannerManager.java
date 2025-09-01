package avox.openutils.modules.worldmap;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import static avox.openutils.OpenUtils.LOGGER;
import static avox.openutils.modules.worldmap.WorldMapModule.originBottomLeftMap;

public class BannerManager {
    public static volatile ArrayList<Banner> banners = new ArrayList<>();
    private static long lastFetchTime = 0;
    private static final long COOLDOWN = 10 * 60 * 1000;

    public record Banner(String map, int x, int y, Vec3d worldMapLocation, String name, String color) {}
    private static final HashMap<String, Identifier> mapPins = new HashMap<>();

    public static Identifier getBanner(String color) {
        if (mapPins.containsKey(color)) {
            return mapPins.get(color);
        }
        Identifier texture = Identifier.of("openutils", "textures/gui/map_pins/" + color + ".png");
        mapPins.put(color, texture);
        return texture;
    }

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
//                        .uri(URI.create("https://openavox.se/getbanners"))
                        .uri(URI.create("http://localhost:3000/getbanners"))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonArray arr = JsonParser.parseString(response.body()).getAsJsonArray();

                ArrayList<Banner> parsed = new ArrayList<>();
                for (JsonElement el : arr) {
                    JsonObject obj = el.getAsJsonObject();

                    String map = obj.get("map").getAsString();
                    int x = obj.get("x").getAsInt();
                    int y = obj.get("y").getAsInt();
                    String name = obj.get("name").getAsString();
                    String color = obj.get("color").getAsString();
                    Vec3d worldMapLocation = computeWorldMapLocation(map, x, y);

                    parsed.add(new Banner(map, x, y, worldMapLocation, name, color));
                }

                banners = parsed;
            } catch (Exception e) {
                LOGGER.info("[OpenUtils]: Failed to load /warp Worldmap banners! " + e.getMessage());
            }
        }, "BannerFetcherThread").start();
    }

    private static Vec3d computeWorldMapLocation(String map, int relative_x, int relative_y) {
        String[] parts = map.split(",");
        int x = Integer.parseInt(parts[0].trim());
        int z = Integer.parseInt(parts[1].trim());

        return new Vec3d(originBottomLeftMap.x + x + (relative_x + 128) / 255.0, 58, originBottomLeftMap.z - z + (relative_y + 128) / 255.0);
    }
}