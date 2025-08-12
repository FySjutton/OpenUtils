package avox.openutils.modules.stock.screen;

import avox.openutils.modules.stock.StockModule;
import avox.openutils.modules.stock.screen.selectWidget.SelectWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class StockScreen extends Screen {
    private final MinecraftClient client;
    private final boolean guildScreen;
    private ListWidget listWidget;

    private SelectWidget filterWidget;
    private SelectWidget itemFilterWidget;
    private SelectWidget sortingWidget;

    public static HashMap<String, LinkedHashMap<String, Boolean>> options = new HashMap<>();

    public StockScreen(boolean guildScreen) {
        super(Text.of("Stock Screen"));
        client = MinecraftClient.getInstance();
        options.clear();
        this.guildScreen = guildScreen;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.of("Öppna vanliga"), btn -> {
            if (client.getNetworkHandler() == null) return;
            StockModule.ignoreNextScreen = true;
            close();
            client.getNetworkHandler().sendChatCommand(guildScreen ? "stock guild" : "stock player");
        }).dimensions(10, client.getWindow().getScaledHeight() - 25, 100, 20).build());

        int width = client.getWindow().getScaledWidth();
        this.filterWidget = new SelectWidget(client, this, "Filtrering", false, FilterTabs.getFilter(), (width - 20) / 3, 65, 5, 5);
        this.itemFilterWidget = new SelectWidget(client, this, "Item Filtrering", true, FilterTabs.ITEM_FILTER, (width - 20) / 3, 85, 5 + (width - 20) / 3 + 5, 5);
        this.sortingWidget = new SelectWidget(client, this, "Sortering", false, FilterTabs.SORTING, (width - 20) / 3, 85, width - 5 - (width - 20) / 3, 5);

        listWidget = new ListWidget(this, width, height);
        addDrawableChild(listWidget);
    }

    public void filterChanged() {
        listWidget.refreshEntries();
    }

    public boolean dropsDownsHovered() {
        return getSelectWidgets().stream().anyMatch(SelectWidget::isFocused);
    }

    private List<SelectWidget> getSelectWidgets() {
        return List.of(filterWidget, itemFilterWidget, sortingWidget);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        for (SelectWidget widget : getSelectWidgets()) {
            widget.render(context, mouseX, mouseY, deltaTicks);
        }
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean found = false;
        for (SelectWidget widget : getSelectWidgets()) {
            if (widget.isFocused()) {
                found = true;
                widget.mouseClicked(mouseX, mouseY, button);
            } else {
                widget.open = false;
            }
        }
        if (found) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (SelectWidget widget : getSelectWidgets()) {
            if (widget.isFocused()) {
                return widget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (SelectWidget widget : getSelectWidgets()) {
            if (widget.isFocused()) {
                return widget.mouseReleased(mouseX, mouseY, button);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (SelectWidget widget : getSelectWidgets()) {
            if (widget.isFocused()) {
                return widget.keyReleased(keyCode, scanCode, modifiers);
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
