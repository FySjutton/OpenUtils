package avox.openutils.modules.stats;

import avox.openutils.Module;
import avox.openutils.modules.stats.screen.StatScreen;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class StatsModule extends Module<StatsModule.Config> {
    public static class Config extends ModuleConfig {
    }

    private static KeyBinding statScreen;

    public StatsModule() {
        super("stats", Config.class);
        statScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            Text.translatable("openmodpack.keybind.stat_screen").getString(),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            Text.translatable("openmodpack.category").getString()
        ));
    }

    @Override
    public void tick(MinecraftClient client) {
        if (config.enableModule) {
            if (statScreen.wasPressed()) {
                client.setScreen(new StatScreen());
            }
        }
    }

    @Override
    protected Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public void loadConfig(ConfigCategory.Builder category) {
        category.group(OptionGroup.createBuilder()
                .name(Text.of("Stats"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Enable Module"))
                        .description(OptionDescription.of(Text.of("Om du vill ha \"Stats\" modulen aktiverad.")))
                        .binding(config.enableModule, () -> config.enableModule, val -> config.enableModule = val)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())
                .build());
    }
}
