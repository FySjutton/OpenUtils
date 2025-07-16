package avox.openutils.modules.stock.screen.selectWidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;

import java.util.HashMap;
import java.util.Map;

import static avox.openutils.OpenUtils.LOGGER;

public class SelectWidget implements Drawable, Selectable, Element {
    private MinecraftClient client;
    private TextRenderer textRenderer;
    private SelectPartWidget selectPartWidget;
    public HashMap<String, Boolean> options;

    public SelectWidget(MinecraftClient client, int width, int height, Map<String, Boolean> options) {
        this.options = new HashMap<>(options);
        this.client = client;
        this.textRenderer = client.textRenderer;

        selectPartWidget = new SelectPartWidget(width, height, this.options);
        selectPartWidget.setY(20);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, 100, 20, 0x00000000);
        context.drawText(textRenderer, "Filter", 5, 7, 0xFFFFFFFF, true);
        selectPartWidget.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        LOGGER.info("clicked");
        return selectPartWidget.mouseClicked(mouseX, mouseY, button);
//        return Element.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return selectPartWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
//        return Element.super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return selectPartWidget.keyPressed(keyCode, scanCode, modifiers);
//        return Element.super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return selectPartWidget.mouseReleased(mouseX, mouseY, button);
//        return Element.super.mouseReleased(mouseX, mouseY, button);
    }
}
