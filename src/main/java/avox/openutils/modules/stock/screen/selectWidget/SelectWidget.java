package avox.openutils.modules.stock.screen.selectWidget;

import avox.openutils.modules.stock.screen.StockScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.input.KeyInput;

import java.util.LinkedHashMap;

import static avox.openutils.modules.stock.screen.StockScreen.options;

public class SelectWidget implements Drawable, Selectable, Element {
    private final TextRenderer textRenderer;
    private final SelectPartWidget selectPartWidget;

    public String title;

    public boolean open = false;
    private boolean hovered;

    private final int width;
    private final int partHeight;
    private final int x;
    private final int y;

    public SelectWidget(MinecraftClient client, StockScreen stockScreen, String title, boolean multiselect, LinkedHashMap<String, Boolean> defaultOptions, int width, int partHeight, int x, int y) {
        this.textRenderer = client.textRenderer;
        this.title = title;

        this.width = width;
        this.partHeight = partHeight;
        this.x = x;
        this.y = y;

        options.put(title, defaultOptions);
        selectPartWidget = new SelectPartWidget(stockScreen, title, width, partHeight, x, y, multiselect);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        setFocused((mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + getHeight()));

        context.fill(x, y, x + width, y + getHeight() + 1, 0xFF000000);
        context.drawText(textRenderer, title, x + 5, y + 10 - textRenderer.fontHeight / 2, 0xFFFFFFFF, true);
        context.drawText(textRenderer, open ? "▼" : "▶", x + width - 15, y + 10 - textRenderer.fontHeight / 2, 0xFFFFFFFF, true);
        if (open) {
            selectPartWidget.render(context, mouseX, mouseY, deltaTicks);
        }
    }

    private int getHeight() {
        return 20 + (open ? partHeight : 0);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {}

    @Override
    public void setFocused(boolean focused) {
        hovered = focused;
    }

    @Override
    public boolean isFocused() {
        return hovered;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.x() > x && click.x() < x + width && click.y() > y && click.y() < y + 20) {
            open = !open;
        }
        if (!open) return false;
        return selectPartWidget.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!open) return false;
        return selectPartWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!open) return false;
        return selectPartWidget.keyPressed(input);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (!open) return false;
        return selectPartWidget.mouseReleased(click);
    }
}
