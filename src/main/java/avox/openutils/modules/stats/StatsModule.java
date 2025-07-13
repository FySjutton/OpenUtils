package avox.openutils.modules.stats;

import avox.openutils.Module;
import avox.openutils.modules.stats.screen.StatScreen;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class StatsModule extends Module<StatsModule.Config> {
    public static final StatsModule INSTANCE = new StatsModule(MinecraftClient.getInstance());

    public static class Config extends ModuleConfig {
    }

    private static KeyBinding statScreen;

    private StatsModule(MinecraftClient client) {
        super("stats", 1, Config.class);
        statScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Statistik Skärmen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "OpenUtils"
        ));
    }

    @Override
    public void tick(MinecraftClient client) {
        if (config.moduleEnabled && statScreen.wasPressed()) {
            client.setScreen(new StatScreen());
        }
    }

    @Override
    protected Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public void loadConfig(ConfigCategory.Builder category) {
        category.group(OptionGroup.createBuilder()
                .name(Text.of("Statistikskärmen"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Använd Modul"))
                        .description(OptionDescription.of(Text.of("Om du vill ha statistik påslagen, rekommenderas.")))
                        .binding(true, () -> config.moduleEnabled, val -> config.moduleEnabled = val)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())
                .build());
    }

    public StatsModule.Config getConfig() {
        return config;
    }
}
