package avox.openutils.modules.stock.screen.selectWidget;

import avox.openutils.modules.stock.screen.StockScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static avox.openutils.modules.stock.screen.StockScreen.options;

public class SelectPartWidget extends ElementListWidget<SelectPartWidget.Entry> {
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private final StockScreen stockScreen;
    public boolean multiselect;
    public String category;

    public int elmWidth;
    public int posX;
    public int posY;

    public SelectPartWidget(StockScreen stockScreen, String category, int width, int height, int x, int y, boolean multiselect) {
        super(MinecraftClient.getInstance(), width, height, y + 20, 20);
        setX(x);

        this.multiselect = multiselect;
        this.category = category;
        this.stockScreen = stockScreen;

        this.elmWidth = width;
        this.posX = x;
        this.posY = y;

        for (String option : options.get(category).keySet()) {
            addEntry(new Entry(option, options.get(category).get(option)));
        }
    }

    @Override
    protected int getScrollbarX() {
        return posX + elmWidth - 5;
    }

    @Override
    public int getRowWidth() {
        return posX + elmWidth - 5;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    protected void drawHeaderAndFooterSeparators(DrawContext context) {
        context.drawHorizontalLine(posX, posX + elmWidth, posY + 20, 0xFF6e6e6e);
        context.drawHorizontalLine(posX, posX + elmWidth, posY + height + 20, 0xFF6e6e6e);
    }

    public void toggleCheckbox(CheckboxWidget widget, boolean newValue) {
        options.get(category).replace(widget.getMessage().getString(), newValue);

        if (!multiselect) {
            for (Entry child : this.children()) {
                if (child.checkbox != widget) {
                    child.checkbox.checked = false;
                    options.get(category).replace(child.checkbox.getMessage().getString(), false);
                } else {
                    widget.checked = true;
                    options.get(category).replace(widget.getMessage().getString(), true);
                }
            }
        }
        stockScreen.filterChanged();
    }

    public class Entry extends ElementListWidget.Entry<Entry> {
        public CheckboxWidget checkbox;

        public Entry(String name, boolean checked) {
            checkbox = CheckboxWidget.builder(Text.of(name), textRenderer)
                    .checked(checked)
                    .callback(SelectPartWidget.this::toggleCheckbox)
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
            checkbox.setX(posX + 3);
            checkbox.render(context, mouseX, mouseY, tickDelta);
        }
    }
}