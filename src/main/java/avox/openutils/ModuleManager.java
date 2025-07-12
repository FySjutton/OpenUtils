package avox.openutils;

import dev.isxander.yacl3.api.ConfigCategory;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    private static final Map<String, Module> modules = new HashMap<>();

    public void registerModule(Module module) {
        modules.put(module.getId(), module);
    }

    public void tick(MinecraftClient client) {
        for (Module module : modules.values()) {
           module.tick(client);
        }
    }

    public List<Module> getAllModules() {
        return modules.values().stream().toList();
    }

    public void loadConfig(ConfigCategory.Builder category) {
        for (Module module : modules.values()) {
            module.loadConfig(category);
        }
    }
}
