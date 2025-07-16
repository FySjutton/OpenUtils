package avox.openutils.modules.stock.screen.selectWidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static avox.openutils.OpenUtils.LOGGER;

public class SelectPartWidget extends ElementListWidget<SelectPartWidget.Entry> {
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    public SelectPartWidget(int width, int height, HashMap<String, Boolean> options) {
        super(MinecraftClient.getInstance(), 100, options.size() * 20, 0, 20);

        for (String option : options.keySet()) {
            addEntry(new Entry(option, options.get(option)));
        }
    }

    @Override
    protected int getScrollbarX() {
        return width - 15;
    }

    @Override
    public int getRowWidth() {
        return width - 15;
    }

    public class Entry extends ElementListWidget.Entry<Entry> {
        public CheckboxWidget checkbox;

        public Entry(String name, boolean checked) {
            checkbox = CheckboxWidget.builder(Text.of(name), textRenderer)
                    .checked(checked)
                    .callback((widget, selected) -> {
                        LOGGER.info(widget.getMessage().getString() + " is now " + selected);
                    })
                    .build();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            List<Selectable> children = new ArrayList<>();
            children.add(checkbox);
            return children;
        }

        @Override
        public List<? extends Element> children() {
            List<Element> children = new ArrayList<>();
            children.add(checkbox);
            return children;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            checkbox.setY(y);
            checkbox.setX(10);
            checkbox.render(context, mouseX, mouseY, tickDelta);
        }
    }
}