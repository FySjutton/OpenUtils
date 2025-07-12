package avox.openutils.modules.quests.questScreen;

import avox.openutils.modules.quests.Quest;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static avox.openutils.modules.quests.QuestManager.quests;

public class QuestScreen extends Screen {
    private QuestScreenWidget questScreenWidget;
    public int maxMissionWidth;
    public int maxProgressWidth;
    public HashMap<Quest, QuestRenderLine> questRenderLines = new HashMap<>();

    public QuestScreen() {
        super(Text.of("Quest Screen"));
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    protected void init() {
        questScreenWidget = new QuestScreenWidget(this, height, width);
        addSelectableChild(questScreenWidget);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        ArrayList<Integer> missionWidths = new ArrayList<>();
        ArrayList<Integer> progressWidths = new ArrayList<>();
        questRenderLines.clear();
        for (Quest quest : quests) {
            String questText = quest.missionAction + " " + quest.total + "st " + quest.prettyMission;
            missionWidths.add(client.textRenderer.getWidth(questText));

            String progressText = "§f(" + quest.completed + "/" + quest.total + ")" + (quest.completed > 0 ? " §7- §b" + String.format("%.2f%%", (double) quest.completed / quest.total * 100) : "");
            progressWidths.add(client.textRenderer.getWidth(progressText));
            questRenderLines.put(quest, new QuestRenderLine(questText, progressText));
        }
        maxMissionWidth = Collections.max(missionWidths);
        maxProgressWidth = Collections.max(progressWidths);

        context.drawCenteredTextWithShadow(client.textRenderer, "§lQuest:", 10 + (21 + maxMissionWidth) / 2, 15, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(client.textRenderer, "§lProgress:", 39 + maxMissionWidth + maxProgressWidth / 2, 15, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(client.textRenderer, "§lBar:", 72 + maxMissionWidth + maxProgressWidth, 15, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(client.textRenderer, "§lExpires:", 125 + maxMissionWidth + maxProgressWidth, 15, 0xFFFFFFFF);
        context.drawText(client.textRenderer, "§lReward:", 156 + maxMissionWidth + maxProgressWidth, 15, 0xFFFFFFFF, true);
        questScreenWidget.render(context, mouseX, mouseY, delta);
    }

    public static class QuestRenderLine {
        public String questText;
        public String progressText;

        public QuestRenderLine(String questText, String progressText) {
            this.questText = questText;
            this.progressText = progressText;
        }
    }
}