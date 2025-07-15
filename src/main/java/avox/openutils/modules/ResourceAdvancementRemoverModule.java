package avox.openutils.modules;

import avox.openutils.Module;
import avox.openutils.OpenUtils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static avox.openutils.OpenUtils.LOGGER;
import static avox.openutils.OpenUtils.taskQueue;

public class ResourceAdvancementRemoverModule extends Module<ResourceAdvancementRemoverModule.Config> {
    public static final ResourceAdvancementRemoverModule INSTANCE = new ResourceAdvancementRemoverModule(MinecraftClient.getInstance());

    public static class Config extends ModuleConfig {
    }

    public static boolean removeAdvancements = false;
    private boolean awaitingWorldChange = false;

    private ResourceAdvancementRemoverModule(MinecraftClient client) {
        super("resource_advancement_remover", 7, Config.class);

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (message.getString().startsWith("Du har blivit teleportad till en slumpmässig plats i världen!")) {
                awaitingWorldChange = true;
            }
        });

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((mc, world) -> {
            if (awaitingWorldChange) {
                awaitingWorldChange = false;
                removeAdvancements = true;
                taskQueue.add(new OpenUtils.DelayedTask(20 * 3, () -> removeAdvancements = false));
            }
        });
    }

    @Override
    public void tick(MinecraftClient client) {
    }

    @Override
    protected Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public void loadConfig(ConfigCategory.Builder category) {
        category.group(OptionGroup.createBuilder()
                .name(Text.of("Ny Resurs Advancement Borttagaren"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Använd Modul"))
                        .description(OptionDescription.of(Text.of("Sätt på / av om OpenUtils ska blocka advancements, när du joinar en ny resurs för första gången.")))
                        .binding(true, () -> config.moduleEnabled, val -> config.moduleEnabled = val)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())

                .build());
    }

    public Config getConfig() {
        return config;
    }
}
