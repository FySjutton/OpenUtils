package avox.openutils.modules.quests;

import avox.openutils.Module;
import avox.openutils.OpenUtils;
import avox.openutils.QuickToggleable;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static avox.openutils.OpenUtils.*;
import static avox.openutils.modules.quests.QuestManager.openQuestScreen;

public class QuestModule extends Module<QuestModule.Config> implements QuickToggleable {
    public static final QuestModule INSTANCE = new QuestModule(MinecraftClient.getInstance());
    public static class Config extends ModuleConfig {
        @SerialEntry
        public int questPadY = 30;
    }

    private static KeyBinding questScreen;
    public static boolean renderQuestHud = true;
    public static final String QUEST_TITLE = "Uppdrag";
    private boolean questsInitialized = false;

    private QuestModule(MinecraftClient client) {
        super("quests", 2, Config.class);
        questScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Uppdragsskärmen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                category
        ));

        taskQueue.add(new OpenUtils.DelayedTask(20 * 15, QuestManager::checkQuestExpireTimes, 1));
        HudElementRegistry.addFirst(
            Identifier.of("openutils", "quests"),
            QuestPadRenderer::renderHud
        );

        ScreenEvents.AFTER_INIT.register((mc, screen, width, height) -> {
            if (config.moduleEnabled && playerInSurvival() && screen.getTitle().getString() != null) {
                if (screen.getTitle().getString().equals(QUEST_TITLE)) {
                    taskQueue.add(new OpenUtils.DelayedTask(5, QuestManager::updateQuestList));
                }
            }
        });

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((mc, world) -> taskQueue.add(new DelayedTask(50, () -> {
            if (config.moduleEnabled && playerInSurvival() && !questsInitialized) {
                QuestManager.reloadQuests();
                questsInitialized = true;
            }
        })));
    }

    @Override
    public void tick(MinecraftClient client) {
        if (config.moduleEnabled && playerInSurvival()) {
            if (questScreen.wasPressed()) {
                openQuestScreen = true;
                QuestManager.reloadQuests();
            }
        }
    }

    @Override
    public String getTitle() {
        return "Quest Pad";
    }

    @Override
    public Boolean isEnabled() {
        return renderQuestHud;
    }

    @Override
    public void setEnabled(boolean enabled) {
        renderQuestHud = !renderQuestHud;
        addToast(MinecraftClient.getInstance(), "Quest Pad växlades!", getToggledString("Quest pad är nu %s!", renderQuestHud));
    }

    public Config getConfig() {
        return config;
    }

    @Override
    protected Class<QuestModule.Config> getConfigClass() {
        return QuestModule.Config.class;
    }

    @Override
    public void loadConfig(ConfigCategory.Builder category) {
        category.group(OptionGroup.createBuilder()
            .name(Text.of("Quest Pad"))
            .option(Option.<Boolean>createBuilder()
                    .name(Text.of("Använd Modul"))
                    .description(OptionDescription.of(Text.of("Om du vill använda quest pad funktionen.")))
                    .binding(true, () -> config.moduleEnabled, val -> config.moduleEnabled = val)
                    .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                    .build())
            .option(Option.<Integer>createBuilder()
                    .name(Text.of("Y-Position"))
                    .description(OptionDescription.of(Text.of("Hur långt ner på skärmen du vill ha din quest pad.")))
                    .binding(30, () -> config.questPadY, newVal -> config.questPadY = newVal)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                            .range(0, 200)
                            .step(1))
                    .build())
            .build());
    }
}
