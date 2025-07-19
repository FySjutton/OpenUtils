package avox.openutils.modules;

import avox.openutils.Module;
import avox.openutils.OpenUtils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static avox.openutils.OpenUtils.taskQueue;

public class AdvancementRemoverModule extends Module<AdvancementRemoverModule.Config> {
    public static final AdvancementRemoverModule INSTANCE = new AdvancementRemoverModule(MinecraftClient.getInstance());
    public static class Config extends ModuleConfig {}
    public static boolean removeAdvancements = false;

    private AdvancementRemoverModule(MinecraftClient client) {
        super("resource_advancement_remover", 7, Config.class);

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((mc, world) -> {
            removeAdvancements = true;
            client.getToastManager().clear();
            taskQueue.add(new OpenUtils.DelayedTask(20 * 3, () -> removeAdvancements = false));
        });
    }

    @Override
    public void tick(MinecraftClient client) {}

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
