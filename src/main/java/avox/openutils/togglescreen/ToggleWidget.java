package avox.openutils.togglescreen;

import avox.openutils.Module;
import avox.openutils.ModuleManager;
import avox.openutils.OpenUtils;
import avox.openutils.QuickToggleable;
import avox.openutils.modules.quests.QuestModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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

        addModule(QuestModule.INSTANCE);
        for (QuickToggleable module : OpenUtils.moduleManager.getQuickToggleModules()) {
            addModule(module);
        }
    }

    public void addModule(QuickToggleable module) {
        this.addEntry(new ToggleEntry(module));
    }

    public static class ToggleEntry extends ElementListWidget.Entry<ToggleEntry> {
        private final QuickToggleable module;

        public ToggleEntry(QuickToggleable module) {
            this.module = module;
        }

        @Override
        public List<? extends ClickableWidget> children() {
            return List.of();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            MinecraftClient client = MinecraftClient.getInstance();

            context.fill(5, getY(), getContentWidth() * 2 - 10, getY() + getContentHeight(), 0xff303030);
            context.drawStrokedRectangle(5, getY(), getContentWidth() * 2 - 15, getContentHeight(), module.isEnabled() ? 0xff65ff57 : 0xffff5757);
            context.drawText(client.textRenderer, module.getTitle(), 15, getY() + getContentHeight() / 2 - client.textRenderer.fontHeight / 2, 0xFFFFFFFF, true);
        }
    }
}