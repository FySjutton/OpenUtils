package avox.openutils.modules.quests.questScreen;

import avox.openutils.modules.quests.QuestPadRenderer;
import avox.openutils.modules.quests.Quest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.ArrayList;
import java.util.List;

import static avox.openutils.modules.quests.QuestManager.quests;

public class QuestScreenWidget extends ElementListWidget<QuestScreenWidget.Entry> {
    private final QuestScreen parent;

    public QuestScreenWidget(QuestScreen parent, int height, int width) {
        super(MinecraftClient.getInstance(), width, height - 40, 30, 19);
        this.parent = parent;

        for (Quest quest : quests) {
            this.addEntry(new Entry(quest));
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
        public Quest quest;

        public Entry(Quest quest) {
            this.quest = quest;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            List<Selectable> children = new ArrayList<>();

            return children;
        }

        @Override
        public List<? extends net.minecraft.client.gui.Element> children() {
            List<Element> children = new ArrayList<>();

            return children;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int textY = getY() + client.textRenderer.fontHeight / 2 + 1;
            QuestScreen.QuestRenderLine renderLine = parent.questRenderLines.get(quest);

            int widthEnd = 10 + 16 + 7 + parent.maxMissionWidth;
            context.fill(9, getY(), widthEnd, getY() + 17, 0x40ffffff);
            context.fill(widthEnd + 2, getY(), widthEnd + parent.maxProgressWidth + 12, getY() + 17, 0x40ffffff);
            int barStart = widthEnd + 14 + parent.maxProgressWidth;
            context.fill(barStart, getY(), barStart + 50, getY() + 17, 0x40ffffff);

            int completed = (int)(48 * ((double) quest.completed / quest.total));
            context.fill(barStart + 1, getY() + 1, barStart + 1 + completed, getY() + 16, 0xFF3aeb34);
            context.fill(barStart + 1 + completed, getY() + 1, barStart + 49, getY() + 16, 0xFF2e2e2e);

            QuestPadRenderer.drawQuestImage(quest, context, 10, getY() + 1);
            context.drawText(client.textRenderer, renderLine.questText, 16 + 10 + 3, textY, 0xFFFFFFFF, true);
            context.drawCenteredTextWithShadow(client.textRenderer, renderLine.progressText, 16 + 10 + 2 + parent.maxMissionWidth + 5 + 5 + 2 + parent.maxProgressWidth / 2, textY, 0xFFFFFFFF);

            context.fill(barStart + 50 + 2, getY(), barStart + 50 + 2 + 50, getY() + 17, 0x40ffffff);

            String expireTime = formatUnixToTime(quest.expiresAt);
            context.drawCenteredTextWithShadow(client.textRenderer, expireTime, barStart + 50 + 2 + 25, textY, 0xFFFFFFFF);

            int priceWidth = width - (barStart + 50 + 2 + 52) - 10 - (overflows() ? 10 : 0);
            context.fill(barStart + 50 + 2 + 52, getY(), barStart + 50 + 2 + 52 + priceWidth, getY() + 17, 0x40ffffff);
            String priceText = quest.priceMoney + "kr & " + quest.priceXP + "xp";
            context.drawCenteredTextWithShadow(client.textRenderer, priceText, barStart + 50 + 2 + 52 + priceWidth / 2, textY, 0xFFFFFFFF);
        }
    }

    public static String formatUnixToTime(long unixMillis) {
        return java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                .format(java.time.Instant.ofEpochMilli(unixMillis)
                        .atZone(java.time.ZoneId.systemDefault()));
    }
}