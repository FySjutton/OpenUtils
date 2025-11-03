package avox.openutils.modules.stats;

import avox.openutils.Module;
import avox.openutils.modules.stats.screen.StatScreen;
import avox.openutils.togglescreen.core.QuickSetting;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class StatsModule extends Module<StatsModule.Config> implements QuickSetting {
    public static final StatsModule INSTANCE = new StatsModule();

    public static class Config extends ModuleConfig {}

    private StatsModule() {
        super("stats", 10, Config.class);
    }

    @Override
    public String getTitle() {
        return "Statistik Skärmen";
    }

    @Override
    public void onClick(MinecraftClient client) {
        client.setScreen(new StatScreen());
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
}
