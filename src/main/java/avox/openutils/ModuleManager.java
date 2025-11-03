package avox.openutils;

import dev.isxander.yacl3.api.ConfigCategory;
import net.minecraft.client.MinecraftClient;

import java.util.*;

public class ModuleManager {
    private final List<Module<?>> sortedModules = new ArrayList<>();
    private final List<QuickToggleable> quickToggleModules = new ArrayList<>();

    public void registerModule(Module<?> module) {
        sortedModules.add(module);
        sortedModules.sort(Comparator.comparingInt(a -> a.priority));

        if (module instanceof QuickToggleable toggleableModule) {
            quickToggleModules.add(toggleableModule);
        }
    }

    public void tick(MinecraftClient client) {
        for (Module<?> module : sortedModules) {
            module.tick(client);
        }
    }

    public void loadConfig(ConfigCategory.Builder category) {
        for (Module<?> module : sortedModules) {
            module.loadConfig(category);
        }
    }

    public List<Module<?>> getAllModules() {
        return List.copyOf(sortedModules);
    }

    public List<QuickToggleable> getQuickToggleModules() {
        return quickToggleModules;
    }
}
