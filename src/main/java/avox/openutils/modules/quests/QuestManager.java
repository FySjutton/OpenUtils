package avox.openutils.modules.quests;

import avox.openutils.OpenUtils;
import avox.openutils.modules.quests.questScreen.QuestScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static avox.openutils.OpenUtils.playerInSurvival;
import static avox.openutils.modules.quests.QuestModule.renderQuestHud;

public class QuestManager {
    public static final ArrayList<Quest> quests = new ArrayList<>();
    private static final List<Integer> validSlots = List.of(11, 12, 13, 14, 20, 21, 22, 23, 29, 30, 31, 32, 38, 39, 40, 41);
    public static boolean autoClose = false;
    public static boolean openQuestScreen = false;

    public static void updateQuestList() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!renderQuestHud) {
            client.player.sendMessage(Text.of("§6OpenModpack: §eQuest Pad is disabled! §fToggle in: §7[" + OpenUtils.configScreenKeybind.getBoundKeyLocalizedText().getString() + "]"), false);
        }
        ScreenHandler screenHandler = client.player.currentScreenHandler;
        List<String> questIDs = quests.stream().map(quest -> quest.ID).toList();
        ArrayList<String> correctIDs = new ArrayList<>();
        for (int slot : validSlots) {
            Quest quest = new Quest(screenHandler.getSlot(slot).getStack());
            if (quest.claimed) {
                correctIDs.add(quest.ID);
                if (!questIDs.contains(quest.ID)) {
                    quests.add(quest);
                }
            }
        }
        quests.removeIf(quest -> !correctIDs.contains(quest.ID));
        if (autoClose) {
            autoClose = false;
            if (openQuestScreen) {
                openQuestScreen = false;
                if (!quests.isEmpty()) {
                    client.setScreen(new QuestScreen());
                } else {
                    client.setScreen(null);
                }
            } else {
                client.setScreen(null);
            }
        }
    }

    public static void newActionBar(Text message) {
        if (!playerInSurvival()) return;
        String text = message.getString();
        Pattern pattern = Pattern.compile("\\w+\\s+\\d+×\\s+([^(/)]+)\\s+\\((\\d+)/(\\d+)\\)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String material = extractTranslationKey(message);
            int done = Integer.parseInt(matcher.group(2));
            int total = Integer.parseInt(matcher.group(3));

            List<Quest> questList = quests.stream().filter(quest -> quest.missionTranslationKey.equals(material) && quest.total == total).toList();
            if (!questList.isEmpty()) {
                Quest quest = questList.getFirst();
                quest.completed = done;

                quests.remove(quest);
                if (quest.completed < quest.total) {
                    // Place it last
                    quests.add(quest);
                }
//            }
            } else {
                reloadQuests();
            }
        }
    }

    public static void reloadQuests() {
        MinecraftClient client = MinecraftClient.getInstance();
        autoClose = true;
        client.getNetworkHandler().sendChatCommand("quests");
    }

    public static String extractTranslationKey(Text text) {
        if (text.getContent() instanceof TranslatableTextContent content) {
            for (Object arg : content.getArgs()) {
                if (arg instanceof Text argText) {
                    if (argText.getContent() instanceof TranslatableTextContent innerContent) {
                        return innerContent.getKey();
                    }
                }
            }
        }

        for (Text sibling : text.getSiblings()) {
            String result = extractTranslationKey(sibling);
            if (result != null) return result;
        }

        return null;
    }

    public static void checkQuestExpireTimes() {
        if (QuestModule.INSTANCE.getConfig().moduleEnabled && playerInSurvival()) {
            long currentTime = System.currentTimeMillis();
            quests.removeIf(quest -> quest.expiresAt < currentTime);
        }
    }
}