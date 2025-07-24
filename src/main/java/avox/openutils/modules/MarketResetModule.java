package avox.openutils.modules;

import avox.openutils.Module;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Calendar;

import static avox.openutils.OpenUtils.*;
import static avox.openutils.SubserverManager.playerOn90gQopen;

public class MarketResetModule extends Module<MarketResetModule.Config> {
    public static final MarketResetModule INSTANCE = new MarketResetModule(MinecraftClient.getInstance());

    public static class Config extends ModuleConfig {
        @SerialEntry
        public SurvivalMarket sendRequirement = SurvivalMarket.IN_SURVIVAL;
        @SerialEntry
        public boolean partyMode = false;
        @SerialEntry
        public int notificationOffset = 0;
        @SerialEntry
        public boolean useSound = true;
    }

    private boolean remindedForToday = false;

    private static boolean renderPartyBackground = false;
    private static int currentPartyTick = 0;
    private static final int[] party_colors = {
            0x20FF0000,
            0x20FFA500,
            0x20FFFF00,
            0x2000FF00,
            0x2000FFFF,
            0x20FF00FF
    };

    private MarketResetModule(MinecraftClient client) {
        super("market_reset", 3, Config.class);

        HudElementRegistry.addFirst(
            Identifier.of("openutils", "market_party"),
            this::renderPartyBackground
        );
    }

    @Override
    public void tick(MinecraftClient client) {
        if (renderPartyBackground) {
            currentPartyTick++;
        }
        if (config.moduleEnabled) {
            boolean checkTimer = config.sendRequirement.equals(SurvivalMarket.NONE);
            if (!checkTimer) {
                if (config.sendRequirement.equals(SurvivalMarket.IN_SURVIVAL)) {
                    checkTimer = playerInSurvival();
                } else {
                    checkTimer = playerOn90gQopen;
                }
            }

            if (checkTimer) {
                int minute = Calendar.getInstance().get(Calendar.MINUTE);

                if (minute == config.notificationOffset || minute == (20 + config.notificationOffset) || minute == ((40 + config.notificationOffset) % 60)) {
                    if (!remindedForToday) {
                        remindedForToday = true;
                        if (config.partyMode) {
                            sendPartyModeNotification(client);
                        } else {
                            sendNormalNotification(client);
                        }
                    }
                } else {
                    remindedForToday = false;
                }
            }
        }
    }

    private void sendPartyModeNotification(MinecraftClient client) {
        new Thread(() -> {
            renderPartyBackground = true;
            currentPartyTick = 0;
            String[] colors = {"§c", "§6", "§e", "§a", "§b", "§d"};
            client.inGameHud.setTitleTicks(0, 1000, 0);
            for (int i = 0; i < 25; i++) {
                String color = colors[i % colors.length];
                client.inGameHud.clearTitle();
                client.inGameHud.setTitle(Text.of(color + "§lMARKNAD RESET!!!"));
                client.inGameHud.setSubtitle(Text.of("§b§lSÄLJ SÄLJ SÄLJ!!!"));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {}
            }
            client.inGameHud.clearTitle();
            client.inGameHud.setDefaultTitleFade();
        }).start();
        for (int i = 0; i < 3; i++) {
            addToast(client, "Marknadsgränsen har återställts!", "Du kan nu sälja igen!");
        }
        for (int i = 0; i < 5; i++) {
            if (config.useSound) {
                if (client.player == null) return;
                client.player.playSound(SoundEvents.BLOCK_BELL_USE);
            }
        }
    }

    private void sendNormalNotification(MinecraftClient client) {
        addToast(client, "Marknadsgränsen har återställts!", "Du kan nu sälja igen!");

        if (config.useSound) {
            if (client.player == null) return;
            client.player.playSound(SoundEvents.BLOCK_BELL_USE);
        }
    }

    private void renderPartyBackground(DrawContext context, RenderTickCounter renderTickCounter) {
        if (config.moduleEnabled) {
            if (!renderPartyBackground) return;

            long elapsed = currentPartyTick;
            if (currentPartyTick >= 100) {
                renderPartyBackground = false;
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();

            int index = (int)((elapsed / 2) % party_colors.length);
            int color = party_colors[index];

            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            context.fill(0, 0, width, height, color);
        }
    }

    @Override
    protected Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public void loadConfig(ConfigCategory.Builder category) {
        category.group(OptionGroup.createBuilder()
                .name(Text.of("Marknadsnotifikationer"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Använd Modul"))
                        .description(OptionDescription.of(Text.of("Slå på eller av marknadsnotifikationerna.")))
                        .binding(true, () -> config.moduleEnabled, val -> config.moduleEnabled = val)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())
                .option(Option.<SurvivalMarket>createBuilder()
                        .name(Text.of("Notifikationskrav"))
                        .description(OptionDescription.of(Text.of("Vilket krav som krävs för att notifikationen ska skickas.")))
                        .binding(SurvivalMarket.IN_SURVIVAL, () -> config.sendRequirement, newVal -> config.sendRequirement = newVal)
                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(SurvivalMarket.class))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Party Mode"))
                        .description(OptionDescription.of(Text.of("Om du vill aktivera party mode eller inte.")))
                        .binding(false, () -> config.partyMode, newVal -> config.partyMode = newVal)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())
                .option(Option.<Integer>createBuilder()
                        .name(Text.of("Notifikationsfördröjning"))
                        .description(OptionDescription.of(Text.of("Hur många minuter efter resetten som notifikationen ska skickas.")))
                        .binding(0, () -> config.notificationOffset, newVal -> config.notificationOffset = newVal)
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(0, 19)
                                .step(1)
                                .formatValue(val -> Text.of(val + " min")))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Använd Ljud"))
                        .description(OptionDescription.of(Text.of("Om ett litet klock-ljud ska spelas när notifikationen skickas.")))
                        .binding(true, () -> config.useSound, newVal -> config.useSound = newVal)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())

                .build());
    }

    public enum SurvivalMarket implements NameableEnum {
        NONE,
        ON_OPEN,
        IN_SURVIVAL;

        @Override
        public Text getDisplayName() {
            Text returnText = null;
            switch (name()) {
                case "NONE" -> returnText = Text.of("Inget");
                case "ON_OPEN" -> returnText = Text.of("Inne på 90gQopen");
                case "IN_SURVIVAL" -> returnText =Text.of("Inne i survival");
            }
            return returnText;
        }
    }
}
