package avox.openutils.modules.stock.screen;

import avox.openutils.modules.stock.StockItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static avox.openutils.modules.stock.StockModule.getItemName;
import static avox.openutils.modules.stock.StockModule.stockItems;

public class ListWidget extends ElementListWidget<ListWidget.Entry> {
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    public ListWidget(int width, int height) {
        super(MinecraftClient.getInstance(), width, height - 100, 50, 24);
        refreshEntries();
    }

    public void refreshEntries() {
        clearEntries();

        for (StockItem stockItem : stockItems) {
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
        public Text stock;

        public Entry(StockItem stockItem) {
            this.stockItem = stockItem;
            this.text = getItemName(stockItem.itemStack);
            this.stock = Text.of("§eLager:§f " + stockItem.storage + " st");
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            List<Selectable> children = new ArrayList<>();
            return children;
        }

        @Override
        public List<? extends Element> children() {
            List<Element> children = new ArrayList<>();
            return children;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.fill(x, y, x + entryWidth - 12, y + 20, 0x80000000);
            context.drawItem(stockItem.itemStack, 15, y + 2);
            int textY = y + 10 - textRenderer.fontHeight / 2;
            context.drawText(textRenderer, text, 35, textY, 0xFFFFFFFF, true);
            context.drawText(textRenderer, stock, x + entryWidth - 100, textY, 0xFFFFFFFF, true);

            if (hovered) {
                context.drawItemTooltip(textRenderer, stockItem.itemStack, mouseX, mouseY);
            }
        }
    }
}