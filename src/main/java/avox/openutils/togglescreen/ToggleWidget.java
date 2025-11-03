package avox.openutils.togglescreen;

import avox.openutils.OpenUtils;
import avox.openutils.modules.quests.QuestModule;
import avox.openutils.togglescreen.core.QuickSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;

import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.List;

public class ToggleWidget extends ElementListWidget<ToggleWidget.ToggleEntry> {
    public final TextRenderer textRenderer;

    public ToggleWidget(MinecraftClient client, int width, int height, int top, int itemHeight) {
        super(client, width, height, top, itemHeight);
        textRenderer = client.textRenderer;

        for (QuickSetting module : OpenUtils.moduleManager.getQuickToggleModules()) {
            addModule(module);
        }
    }

    public void addModule(QuickSetting module) {
        this.addEntry(new ToggleEntry(module));
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    public static class ToggleEntry extends ElementListWidget.Entry<ToggleEntry> {
        private final QuickSetting module;
        private final List<QuickSetting.Widget> widgets;

        public ToggleEntry(QuickSetting module) {
            this.module = module;
            this.widgets = module.getWidgets();
        }

        @Override
        public List<? extends ClickableWidget> children() {
            return widgets.stream().map(QuickSetting.Widget::widget).toList();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return widgets.stream().map(QuickSetting.Widget::widget).toList();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            MinecraftClient client = MinecraftClient.getInstance();

            context.fill(5, getY(), getContentWidth(), getY() + getContentHeight(), module.getColor());
            context.drawText(client.textRenderer, module.getTitle(), 10, getY() + getContentHeight() / 2 - client.textRenderer.fontHeight / 2 + 1, 0xFFFFFFFF, true);

            int x = getContentWidth() - 2;
            for (QuickSetting.Widget widget : widgets) {
                ClickableWidget element = widget.widget();
                element.active = widget.active().get();
                element.setX(x - element.getWidth());
                x -= element.getWidth() + 3;
                element.setY(getY() + 2);
                element.setHeight(getContentHeight() - 4);
                element.render(context, mouseX, mouseY, deltaTicks);
            }
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            int top = getY() + PADDING;
            int bottom = getY() + getContentHeight() - PADDING;
            if (click.y() < top || click.y() > bottom) return false;

            if (super.mouseClicked(click, doubled)) return true;
            module.onClick(MinecraftClient.getInstance());
            return true;
        }
    }
}