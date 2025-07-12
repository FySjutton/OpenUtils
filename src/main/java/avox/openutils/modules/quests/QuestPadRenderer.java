package avox.openutils.modules.quests;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static avox.openutils.OpenUtils.*;
import static avox.openutils.modules.quests.ImageHandler.questEntities;
import static avox.openutils.modules.quests.QuestManager.quests;
import static avox.openutils.modules.quests.QuestModule.renderQuestHud;

public class QuestPadRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final HashMap<String, Item> specialQuestEntities = new HashMap<>(Map.of(
            "mjölka_cow", Items.MILK_BUCKET,
            "klipp_sheep", Items.WHITE_WOOL
    ));

    public static void renderHud(DrawContext context, RenderTickCounter renderTickCounter) {
        if (!renderQuestHud || quests.isEmpty() || !playerInSurvival()) {
            return;
        }

        Matrix3x2fStack matrixStack = context.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.scale(0.7f, 0.7f);
        float revereScale = 1 / 0.7f;

        Quest infoQuest = quests.getLast();
        String infoQuestString = infoQuest.missionAction + " " + infoQuest.prettyMission + " (" + infoQuest.completed + "/" + infoQuest.total + ")";
        int infoQuestWidth = client.textRenderer.getWidth(infoQuestString);
        int questBoxWidth = Math.max(20 + infoQuestWidth, Math.min(quests.size() - 1, 8) * 18);
        int questBoxHeight = 23 + Math.ceilDiv(quests.size() - 1, 8) * 18;

        int startX = (int) ((float) (client.getWindow().getScaledWidth() - questBoxWidth * 0.7) / 2 * revereScale);
        int startY = QuestModule.INSTANCE.getConfig().questPadY;

        int y = startY;
        int x = startX;
        context.fill(startX - 2, startY - 2, startX + questBoxWidth + 1, startY + questBoxHeight + 2, 0x4c000000);

        if (quests.size() > 1) {
            for (Quest quest : quests.subList(0, quests.size() - 1)) {
                if (x > startX + 16 * 8) {
                    x = startX;
                    y += 20;
                }
                drawQuestImage(quest, context, x, y);
                int completed = (int)(16 * ((double) quest.completed / quest.total));
                context.fill(x, y + 17, x + completed, y + 18, 0xFF3aeb34);
                context.fill(x + completed, y + 17, x + 16, y + 18, 0xFF2e2e2e);
                x += 18;
            }
            y += 21;
            x = startX;
        }

        context.drawText(client.textRenderer, infoQuestString, x + 18, y + 8 - client.textRenderer.fontHeight / 2 + 1, 0xFFFFFFFF, true);
        drawQuestImage(quests.getLast(), context, x, y);
        int completed = (int)((18 + infoQuestWidth) * ((double) infoQuest.completed / infoQuest.total));
        context.fill(x, y + 17, x + completed, y + 18, 0xFF3aeb34);
        context.fill(x + completed, y + 17, x + 18 + infoQuestWidth, y + 18, 0xFF2e2e2e);

        matrixStack.popMatrix();
    }

    public static void drawQuestImage(Quest quest, DrawContext context, int x, int y) {
        String[] parts = quest.missionTranslationKey.split("\\.");
        if (parts.length < 3) return;

        String category = parts[0]; // item, block, entity
        String name = String.join("_", Arrays.copyOfRange(parts, 2, parts.length));

        if (category.equals("block") || category.equals("item")) {
            Item item;
            if (category.equals("block")) {
                item = Registries.BLOCK.get(Identifier.of("minecraft", name)).asItem();
            } else {
                item = Registries.ITEM.get(Identifier.of("minecraft", name));
            }
            if (item != null) {
                context.drawItem(new ItemStack(item), x, y);
            }
        } else if (category.equals("entity")) {
            String value = quest.missionAction.toLowerCase() + "_" + name;
            if (specialQuestEntities.containsKey(value)) {

                context.drawItem(new ItemStack(specialQuestEntities.get(value)), x, y);
            } else {
                ImageHandler imageHandler = new ImageHandler();
                imageHandler.registerQuestEntity(name);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, questEntities.getOrDefault(name, Identifier.of("openutils", "textures/gui/unknown.png")), x, y, 0, 0, 16, 16, 16, 16);
            }
        }
    }
}