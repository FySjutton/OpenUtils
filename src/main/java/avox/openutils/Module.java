package avox.openutils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.MinecraftClient;

public abstract class Module<T extends Module.ModuleConfig> {
    private final String id;
    protected T config;

    public Module(String id, Class<T> configClass) {
        this.id = id;
        this.config = createDefaultConfig(configClass);
    }

    public String getId() {
        return id;
    }

    public abstract void tick(MinecraftClient client);

    // Config
    public void applyConfig(Object loaded) {
        if (loaded != null) {
            try {
                Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

                String jsonString = gson.toJson(loaded);
                this.config = gson.fromJson(jsonString, getConfigClass());
            } catch (Exception ignored) {}
        }
    }

    public Object getConfigForSaving() {
        return config;
    }

    protected abstract Class<T> getConfigClass();

    protected T createDefaultConfig(Class<T> configClass) {
        try {
            return configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create default config for module: " + id, e);
        }
    }

    public abstract void loadConfig(ConfigCategory.Builder builder);

    public static abstract class ModuleConfig {
        @SerialEntry
        public boolean enableModule = true;
    }
}
