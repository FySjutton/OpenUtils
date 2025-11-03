package avox.openutils;

import avox.openutils.togglescreen.core.QuickSetting;
import dev.isxander.yacl3.api.ConfigCategory;
import net.minecraft.client.MinecraftClient;

import java.util.*;

public class ModuleManager {
    private final List<Module<?>> modules = new ArrayList<>();
    private final List<QuickSetting> quickToggleModules = new ArrayList<>();

    public void registerModule(Module<?> module) {
        modules.add(module);
        modules.sort(Comparator.comparingInt(a -> a.priority));

        if (module instanceof QuickSetting toggleableModule) {
            quickToggleModules.add(toggleableModule);
            quickToggleModules.sort(Comparator.comparingInt(a -> ((Module<?>) a).priority));
        }
    }

    public void tick(MinecraftClient client) {
        for (Module<?> module : modules) {
            module.tick(client);
        }
    }

    public void loadConfig(ConfigCategory.Builder category) {
        for (Module<?> module : modules) {
            module.loadConfig(category);
        }
    }

    public List<Module<?>> getAllModules() {
        return List.copyOf(modules);
    }

    public List<QuickSetting> getQuickToggleModules() {
        return quickToggleModules;
    }
}
