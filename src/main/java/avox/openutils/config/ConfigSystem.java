package avox.openutils.config;

import avox.openutils.Module;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static avox.openutils.OpenUtils.LOGGER;
import static avox.openutils.OpenUtils.moduleManager;

public class ConfigSystem {
    @SerialEntry
    public final Map<String, Object> moduleConfigs = new HashMap<>();

    public static final ConfigClassHandler<ConfigSystem> CONFIG = ConfigClassHandler.createBuilder(ConfigSystem.class)
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("openutils.json"))
                    .build())
            .build();

    public static Screen configScreen(Screen parent) {
        ConfigCategory.Builder moduleCategory = ConfigCategory.createBuilder()
            .name(Text.of("Modules"));

        moduleManager.loadConfig(moduleCategory);

        return YetAnotherConfigLib.create(CONFIG, ((defaults, config, builder) -> builder
            .title(Text.of("OpenUtils"))
            .category(moduleCategory.build())
            .save(() -> {
                ConfigSystem.saveModuleConfigs(); // Detta är överkurs om du redan sparar allt i getConfig
                ConfigSystem.CONFIG.save();
                ConfigSystem.applyModuleConfigs(); // <- viktigast!
            })
        )).generateScreen(parent);
    }

    public static void applyModuleConfigs() {
        for (Module<?> module : moduleManager.getAllModules()) {
            Object raw = CONFIG.instance().moduleConfigs.get(module.getId());
            module.applyConfig(raw);
        }
    }

    public static void saveModuleConfigs() {
        for (Module<?> module : moduleManager.getAllModules()) {
            CONFIG.instance().moduleConfigs.put(module.getId(), module.getConfigForSaving());
        }
    }

}