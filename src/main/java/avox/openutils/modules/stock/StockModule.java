package avox.openutils.modules.stock;

import avox.openutils.Module;
import avox.openutils.modules.stock.screen.StockScreen;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
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

    public static class Config extends ModuleConfig {
        @SerialEntry
        public boolean standardEmpty = true;
    }
    public static boolean screenOpen = false;
    public static final List<Integer> itemSlots = List.of(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    );
    public static boolean ignoreNextScreen = false;

    private boolean waitingForLoad;
    private Runnable foundWaiting;
    private SlotWaitCondition waitCondition;

    private final Pattern pagePattern = Pattern.compile("Sida (\\d+)/(\\d+)");
    private int page;
    private boolean inStockPage;

    public static final ArrayList<StockItem> stockItems = new ArrayList<>();
    public static boolean muteSounds = false;

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
            if (waitCondition != null && waitCondition.check()) {
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
                        processStockScreen(client, screen, client.currentScreen.getTitle().getString().equals("Guild-affärer"));
                    }
                }
            }
        }
    }

    private void processStockScreen(MinecraftClient client, ScreenHandler screen, boolean guildScreen) {
        muteSounds = true;
        switchMode(client, screen, true, () -> {
            page = 1;
            loadPages(client, screen, () -> switchMode(client, screen, false, () -> {
                page = 1;
                loadPages(client, screen, () -> {
                    muteSounds = false;
                    client.setScreen(null);
                    client.setScreen(new StockScreen(guildScreen));
                });
            }));
        });
    }

    private void loadPages(MinecraftClient client, ScreenHandler screen, Runnable onComplete) {
        int pages = 1;
        ItemStack stack = screen.getSlot(44).getStack();
        if (stack.getItem() == Items.ARROW) {
            LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
            if (lore != null) {
                Matcher matcher = pagePattern.matcher(lore.lines().getFirst().getString());
                if (matcher.matches()) {
                    pages = Integer.parseInt(matcher.group(2));
                }
            }
        }

        page = 1;
        loadNextPage(client, screen, pages, onComplete);
    }

    private void loadNextPage(MinecraftClient client, ScreenHandler screen, int totalPages, Runnable onComplete) {
        waitingForLoad = true;
        waitCondition = () -> {
            int thisPage = getCurrentPage(screen.getSlot(44).getStack());
            return (thisPage == page + 1 || thisPage == -1);
        };
        foundWaiting = () -> {
            waitingForLoad = false;

            for (int i : itemSlots) {
                if (screen.getSlot(i).hasStack()) {
                    StockItem item = new StockItem(screen.getSlot(i).getStack(), inStockPage);
                    if (!stockItems.contains(item)) {
                        stockItems.add(item);
                    }
                }
            }

            if (page < totalPages) {
                client.interactionManager.clickSlot(
                        client.player.currentScreenHandler.syncId,
                        44, 0,
                        SlotActionType.PICKUP,
                        client.player
                );
                page++;

                loadNextPage(client, screen, totalPages, onComplete);
            } else {
                onComplete.run();
            }
        };
    }

    private void switchMode(MinecraftClient client, ScreenHandler screen, boolean inStock, Runnable onComplete) {
        inStockPage = inStock;

        client.interactionManager.clickSlot(
                client.player.currentScreenHandler.syncId,
                42, 0,
                SlotActionType.PICKUP,
                client.player
        );

        waitingForLoad = true;
        ItemStack oldStack = screen.getSlot(42).getStack().copy();

        waitCondition = () -> {
            ItemStack newStack = screen.getSlot(42).getStack();
            LoreComponent oldLore = oldStack.getComponents().get(DataComponentTypes.LORE);
            LoreComponent newLore = newStack.getComponents().get(DataComponentTypes.LORE);
            return newLore != null && (!newLore.equals(oldLore));
        };

        foundWaiting = () -> {
            waitingForLoad = false;

            goToFirstPage(client, screen, () -> waitForForwardArrow(client, screen, () -> loadPages(client, screen, onComplete)));
        };
    }

    private void goToFirstPage(MinecraftClient client, ScreenHandler screen, Runnable onComplete) {
        ItemStack back = screen.getSlot(36).getStack();
        if (back.getItem() == Items.ARROW) {
            LoreComponent oldLore = back.getComponents().get(DataComponentTypes.LORE);

            client.interactionManager.clickSlot(
                    client.player.currentScreenHandler.syncId,
                    36, 0,
                    SlotActionType.PICKUP,
                    client.player
            );

            waitingForLoad = true;
            waitCondition = () -> {
                ItemStack newBack = screen.getSlot(36).getStack();
                LoreComponent newLore = newBack.getComponents().get(DataComponentTypes.LORE);
                return (newLore != null && !newLore.equals(oldLore)) || newBack.isEmpty();
            };

            foundWaiting = () -> {
                waitingForLoad = false;
                if (screen.getSlot(36).getStack().getItem() == Items.ARROW) {
                    goToFirstPage(client, screen, onComplete);
                } else {
                    onComplete.run();
                }
            };
        } else {
            onComplete.run();
        }
    }

    private void waitForForwardArrow(MinecraftClient client, ScreenHandler screen, Runnable onReady) {
        waitingForLoad = true;

        waitCondition = () -> {
            ItemStack forward = screen.getSlot(44).getStack();
            return forward.isEmpty() || forward.getItem() == Items.ARROW;
        };

        foundWaiting = () -> {
            waitingForLoad = false;
            onReady.run();
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

    @Override
    protected Class<Config> getConfigClass() {
        return Config.class;
    }

    public Config getConfig() {
        return config;
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
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Endast Tomma Standard"))
                        .description(OptionDescription.of(Text.of("Om du vill ha endast tomma som standard.")))
                        .binding(true, () -> config.standardEmpty, val -> config.standardEmpty = val)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())

                .build());
    }

    public interface SlotWaitCondition {
        boolean check();
    }
}
