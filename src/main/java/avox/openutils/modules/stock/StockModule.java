package avox.openutils.modules.stock;

import avox.openutils.Module;
import avox.openutils.modules.stock.screen.StockScreen;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static avox.openutils.OpenUtils.*;

public class StockModule extends Module<StockModule.Config> {
    public static final StockModule INSTANCE = new StockModule(MinecraftClient.getInstance());

    public static class Config extends ModuleConfig { }
    public static boolean screenOpen = false;
    public static final List<Integer> itemSlots = List.of(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    );
    public static boolean ignoreNextScreen = false;

    private boolean waitingForLoad;
    private Runnable foundWaiting;
    private final Pattern pagePattern = Pattern.compile("Sida (\\d+)/\\d+");
    private int page;

    public static final ArrayList<StockItem> stockItems = new ArrayList<>();

    private StockModule(MinecraftClient client) {
        super("stock", 4, Config.class);

        HudElementRegistry.addLast(
                Identifier.of("openutils", "stock_loading"),
                (context, renderTickCounter) -> {
                    if (screenOpen) {
                        context.drawCenteredTextWithShadow(client.textRenderer, "§b§lLoading...", client.getWindow().getScaledWidth() / 2, client.getWindow().getScaledHeight() / 2 - 4, 0xFFFFFFFF);
                    }
                }
        );
    }

    @Override
    public void tick(MinecraftClient client) {
        if (waitingForLoad && client.currentScreen instanceof HandledScreen) {
            ScreenHandler screen = ((HandledScreen<?>) client.currentScreen).getScreenHandler();
            // Makes sure the server has updated
            int thisPage = getCurrentPage(screen.getSlot(44).getStack());
            if (thisPage == page + 1 || thisPage == -1) {
                foundWaiting.run();
            }
        }

        if (config.moduleEnabled) {
            if (!ignoreNextScreen && !screenOpen && playerInSurvival()) {
                if (client.currentScreen instanceof HandledScreen) {
                    if (List.of("Dina affärer", "Guild-affärer").contains(client.currentScreen.getTitle().getString())) {
                        screenOpen = true;
                        ScreenHandler screen = ((HandledScreen<?>) client.currentScreen).getScreenHandler();
                        stockItems.clear();
                        processGuildScreen(client, screen, client.currentScreen.getTitle().getString().equals("Guild-affärer"));
                    }
                }
            }
        }
    }

    private void processGuildScreen(MinecraftClient client, ScreenHandler screen, boolean guildScreen) {
        page = 1;
        loadPage(client, screen, () -> {
            client.setScreen(null);
            client.setScreen(new StockScreen(guildScreen));
        });
    }

    private void loadPage(MinecraftClient client, ScreenHandler screen, Runnable onComplete) {
        waitingForLoad = true;
        foundWaiting = () -> {
            waitingForLoad = false;
            for (int i : itemSlots) {
                if (screen.getSlot(i).hasStack()) {
                    stockItems.add(new StockItem(screen.getSlot(i).getStack()));
                }
            }

            if (screen.getSlot(44).hasStack() && screen.getSlot(44).getStack().getItem() == Items.ARROW) {
                page = getCurrentPage(screen.getSlot(44).getStack());
                client.interactionManager.clickSlot(
                    client.player.currentScreenHandler.syncId,
                    44, 0,
                    SlotActionType.PICKUP,
                    client.player
                );

                loadPage(client, screen, onComplete);
            } else {
                onComplete.run();
            }
        };
    }

    private int getCurrentPage(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }
        LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
        if (lore != null) {
            Matcher matcher = pagePattern.matcher(lore.lines().getFirst().getString());
            if (matcher.matches()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return -1;
    }

    public static Text getItemName(ItemStack stack) {
        Item item = stack.getItem();
        Text text;

        if (item.equals(Items.ENCHANTED_BOOK)) {
            ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
            if (enchants != null && !enchants.isEmpty()) {
                int level = enchants.getEnchantmentEntries().stream().toList().getFirst().getIntValue();
                Enchantment value = enchants.getEnchantments().stream().toList().getFirst().value();

                text = Text.literal(value.description().getString()).append(" " + level);
            } else {
                text = stack.getName();
            }
        } else {
            text = stack.getName();
        }
        return text;
    }

    @Override
    protected Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public void loadConfig(ConfigCategory.Builder category) {
        category.group(OptionGroup.createBuilder()
                .name(Text.of("Stock Skärm"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Använd Modul"))
                        .description(OptionDescription.of(Text.of("Slå på eller av tydligare stock skärmar.")))
                        .binding(true, () -> config.moduleEnabled, val -> config.moduleEnabled = val)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())

                .build());
    }
}
