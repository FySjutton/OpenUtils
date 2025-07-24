package avox.openutils.modules.stock.screen;

import avox.openutils.modules.stock.StockItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static avox.openutils.modules.stock.StockModule.getItemName;
import static avox.openutils.modules.stock.screen.FilterManager.*;

public class ListWidget extends ElementListWidget<ListWidget.Entry> {
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    public StockScreen stockScreen;

    public ListWidget(StockScreen stockScreen, int width, int height) {
        super(MinecraftClient.getInstance(), width, height - 32 - 32, 32, 24);
        this.stockScreen = stockScreen;
        refreshEntries();
    }

    public void refreshEntries() {
        clearEntries();

        for (StockItem stockItem : sortItems(filterItems())) {
            addEntry(new Entry(stockItem));
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
        public StockItem stockItem;
        public Text text;
        public Text info;
        public int infoWidth;

        public Entry(StockItem stockItem) {
            this.stockItem = stockItem;
            this.text = getItemName(stockItem.itemStack);
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
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.fill(x, y, x + entryWidth - 12, y + 20, 0x80000000);
            context.drawItem(stockItem.itemStack, 15, y + 2);
            int textY = y + 10 - textRenderer.fontHeight / 2;
            context.drawText(textRenderer, text, 35, textY, 0xFFFFFFFF, true);
            context.drawText(textRenderer, info, x + entryWidth - infoWidth - 20, textY, 0xFFFFFFFF, true);

            if (!stockScreen.dropsDownsHovered() && hovered) {
                context.drawItemTooltip(textRenderer, stockItem.itemStack, mouseX, mouseY);
            }
        }
    }
}