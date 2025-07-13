package avox.openutils;

import dev.isxander.yacl3.api.ConfigCategory;
import net.minecraft.client.MinecraftClient;

import java.util.*;

public class ModuleManager {
    private final Map<String, Module> moduleMap = new HashMap<>();
    private final List<Module> sortedModules = new ArrayList<>();

    public void registerModule(Module module) {
        moduleMap.put(module.getId(), module);
        sortedModules.add(module);
        sortedModules.sort(Comparator.comparingInt(a -> a.priority));
    }

    public void tick(MinecraftClient client) {
        for (Module module : sortedModules) {
            module.tick(client);
        }
    }

    public void loadConfig(ConfigCategory.Builder category) {
        for (Module module : sortedModules) {
            module.loadConfig(category);
        }
    }

    public List<Module> getAllModules() {
        return List.copyOf(sortedModules);
    }
}
