package avox.openutils.modules.stats.screen;

import avox.openutils.modules.stats.Formatter;
import avox.openutils.modules.stats.screen.types.StatTabTypes;
import avox.openutils.modules.stats.screen.widgets.navigation.StatCategory;
import avox.openutils.modules.stats.screen.widgets.navigation.StatEntry;
import avox.openutils.modules.stats.screen.widgets.navigation.StatFolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class StatViewer extends ElementListWidget<StatViewer.Entry> {
    private final MinecraftClient client;
    private final int screenWidth;
    public ArrayList<StatCategory> lastCategories;
    public boolean drawMessage = true;
    public String specialMessage = "";
    private StatTabTypes tabType;

    public StatViewer(StatTabTypes tabType, MinecraftClient client, int width, int height) {
        super(client, width - width / 3 - 10 - 5 - 10, height - 24 - 15 - 10, 24 + 10, 20);
        this.client = client;
        this.screenWidth = width;
        this.tabType = tabType;
        setX(width / 3 + 10 + 5);
    }

    public void updateEntries(ArrayList<StatCategory> categories) {
        this.clearEntries();
        if (!categories.isEmpty()) {
            drawMessage = false;
        }
        for (StatCategory category : categories) {
            addEntry(new Entry(null, null, category));
            for (StatEntry statEntry : category.directStats) {
                addEntry(new Entry(statEntry, null, category));
            }
            for (StatFolder folder : category.folders) {
                addEntry(new Entry(null, folder, category));
                if (folder.open) {
                    for (StatEntry entry : folder.entries) {
                        addEntry(new Entry(entry, folder, category));
                    }
                }
            }
        }
        refreshScroll();
        lastCategories = categories;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        if (drawMessage && !specialMessage.isEmpty()) {
            context.drawWrappedText(client.textRenderer, Text.of(specialMessage), screenWidth / 3 + 10 + 5 + 5, getY() + 5, screenWidth - screenWidth / 3 - 10 - 5 - 10 - 10, 0xFFFFFFFF, true);
        }
    }

    @Override
    protected int getScrollbarX() {
        return screenWidth / 3 + 10 + 5 + (screenWidth - screenWidth / 3 - 10 - 5 - 10) - 10;
    }

    @Override
    public int getRowWidth() {
        return width - 20;
    }

    public class Entry extends ElementListWidget.Entry<StatViewer.Entry> {
        public MutableText category;

        public MutableText key;
        public MutableText value;

        public StatFolder folder;
        public MutableText folderName;
        public ButtonWidget folderBtn;

        public Entry(StatEntry statEntry, StatFolder folder, StatCategory category) {
            String folderName = (folder == null ? "" : folder.name);
            String categoryName = category.title;
            if (statEntry != null) {
                key = Formatter.formatKey(tabType, statEntry.key, categoryName, folderName);
                value = Formatter.formatValue(tabType, statEntry.value, statEntry.key, category.title, (folder == null ? "" : folder.name));
            } else if (folder != null) {
                this.folder = folder;
                this.folderName = Formatter.formatFolder(tabType, folder.name, categoryName);
                folderBtn = ButtonWidget.builder(Text.empty(), btn -> {
                    folder.open = !folder.open;
                    updateEntries(lastCategories);
                }).dimensions(0, 0, 100, 20).build();
            } else {
                this.category = Formatter.formatCategory(tabType, category.title, category.suffix);
            }
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            List<Selectable> children = new ArrayList<>();

            if (folderBtn != null) {
                children.add(folderBtn);
            }
            return children;
        }

        @Override
        public List<? extends Element> children() {
            List<Element> children = new ArrayList<>();
            if (folderBtn != null) {
                children.add(folderBtn);
            }
            return children;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int centerHeight = y + entryHeight / 2 - client.textRenderer.fontHeight / 2;
            if (this.category != null) {
                context.drawText(client.textRenderer, this.category, x - 5, centerHeight, 0xFFfff700, true);
            } else if (folderBtn != null) {
                folderBtn.setPosition(x - 5, y);
                folderBtn.setDimensions(18, 18);
                if (folderBtn.isMouseOver(mouseX, mouseY)) {
                    folderBtn.render(context, mouseX, mouseY, tickDelta);
                }

                context.drawText(client.textRenderer, Text.of(folder.open ? "▼" : "▶"), x + 1, centerHeight + 1, 0xFFFFFFFF, true);
                context.drawText(client.textRenderer, folderName, x + 18, centerHeight + 1, 0xFFFFFFFF, true);
            } else {
                context.fill(x - 5, y, x + entryWidth - (overflows() ? 8 : 0), y + entryHeight, 0x80000000);
                context.drawText(client.textRenderer, this.key, x, centerHeight, 0xFFFFFFFF, true);
                context.drawText(client.textRenderer, this.value, x + entryWidth - (overflows() ? 8 : 0) - client.textRenderer.getWidth(this.value) - 3, centerHeight, 0xFFFFFFFF, true);
            }
        }
    }
}