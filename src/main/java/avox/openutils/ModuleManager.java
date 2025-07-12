package avox.openutils;

import dev.isxander.yacl3.api.ConfigCategory;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static ArrayList<Module> modules = new ArrayList<>();

    public void registerModule(Module module) {
        modules.add(module);
    }

    public void tick(MinecraftClient client) {
        modules.forEach(module -> module.tick(client));
    }

    public List<Module> getAllModules() {
        return modules;
    }

    public void loadConfig(ConfigCategory.Builder category) {
        modules.forEach(module -> module.loadConfig(category));
    }
}
