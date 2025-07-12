package avox.openutils.modules.quests;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ImageHandler {
    public static final HashMap<String, Identifier> questEntities = new HashMap<>();

    public final HashMap<String, String> questEntitiesLinks = new HashMap<>(Map.of(
            "zombie", "https://minecraftfaces.com/wp-content/bigfaces/big-zombie-face.png",
            "husk", "https://minecraftfaces.com/wp-content/bigfaces/big-husk-face.png",
            "stray", "https://minecraftfaces.com/wp-content/bigfaces/big-stray-face.png",
            "wither_skeleton", "https://minecraftfaces.com/wp-content/bigfaces/big-wither-skeleton-face.png",
            "ghast", "https://minecraftfaces.com/wp-content/bigfaces/big-ghast-face.png",
            "skeleton", "https://minecraftfaces.com/wp-content/bigfaces/big-skeleton-face.png",
            "creeper", "https://minecraftfaces.com/wp-content/bigfaces/big-creeper-face.png",
            "spider", "https://i.ibb.co/kjr38Bv/big-spider-face-1.png",
            "enderman", "https://minecraftfaces.com/wp-content/bigfaces/big-enderman-face.png",
            "pillager", "https://art.pixilart.com/d763eed9ffca7c0.png"
    ));

    public void registerQuestEntity(String value) {
        if (!questEntities.containsKey(value)) {
            if (questEntitiesLinks.containsKey(value)) {
                try {
                    Identifier registeredImage = getImageFromURL(URI.create(questEntitiesLinks.get(value)).toURL(), value);
                    questEntities.put(value, Objects.requireNonNullElseGet(registeredImage, () -> Identifier.of("openutils", "textures/gui/unknown.png")));
                } catch (Exception ignored) {}
            }
        }
    }

    private Identifier getImageFromURL(URL url, String key) {
        try {
            InputStream in = url.openStream();
            NativeImage image = NativeImage.read(in);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(url::toString, image);
            Identifier textureId = Identifier.of("openutils", "dynamic/" + key);
            MinecraftClient.getInstance()
                    .getTextureManager()
                    .registerTexture(textureId, texture);
            return textureId;
        } catch (Exception ignored) {}
        return null;
    }
}