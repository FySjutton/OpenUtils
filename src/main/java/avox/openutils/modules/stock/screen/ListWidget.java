package avox.openutils.modules.stock.screen;

import avox.openutils.modules.stock.StockItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static avox.openutils.modules.stock.screen.FilterManager.*;

public class ListWidget extends ElementListWidget<ListWidget.Entry> {
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    public StockScreen stockScreen;

    public ListWidget(StockScreen stockScreen, int width, int height) {
        super(MinecraftClient.getInstance(), width, height - 32 - 32, 32, 24);
        this.stockScreen = stockScreen;
        refreshEntries("");
    }

    public void refreshEntries(String search) {
        clearEntries();

        List<StockItem> entries = sortItems(filterItems(search));
        for (StockItem stockItem : entries) {
            addEntry(new Entry(this, stockItem));
        }
        stockScreen.suggestionsFound = !entries.isEmpty();
    }

    @Override
    protected int getScrollbarX() {
        return width - 15;
    }

    @Override
    public int getRowWidth() {
        return width - (overflows() ? 15 : 0);
    }

    @Override
    public int getRowLeft() {
        return 7;
    }

    public class Entry extends ElementListWidget.Entry<Entry> {
        public ListWidget listWidget;
        public StockItem stockItem;
        public Text info;
        public int infoWidth;

        public Entry(ListWidget listWidget, StockItem stockItem) {
            this.listWidget = listWidget;
            this.stockItem = stockItem;
            this.info = getInfoText(stockItem);
            this.infoWidth = textRenderer.getWidth(this.info);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return new ArrayList<>();
        }

        @Override
        public List<? extends Element> children() {
            return new ArrayList<>();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            context.fill(getX(), getY(), getX() + getRowWidth() - 12, getY() + 20, 0x80000000);
            context.drawItem(stockItem.itemStack.visualStack, 12, getY() + 2);
            int textY = getY() + 10 - textRenderer.fontHeight / 2;
            context.drawText(textRenderer, stockItem.name, 33, textY, 0xFFFFFFFF, true);
            context.drawText(textRenderer, info, getX() + getRowWidth() - infoWidth - 20, textY, 0xFFFFFFFF, true);

            if (!stockScreen.dropsDownsHovered() && hovered) {
                context.drawItemTooltip(textRenderer, stockItem.itemStack.visualStack, mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            return super.mouseClicked(click, doubled);
        }
    }
}