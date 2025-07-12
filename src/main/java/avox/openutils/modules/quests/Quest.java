package avox.openutils.modules.quests;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static avox.openutils.modules.quests.QuestManager.extractTranslationKey;


public class Quest {
    public boolean claimed = false;
    public String ID;

    public String missionAction;
    public String missionTranslationKey;
    public String prettyMission;
    public int total;
    public int completed;

    public int priceMoney;
    public int priceXP;

    public long expiresAt;

    public Quest(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return;
        for (Text line : lore.lines()) {
            String text = line.getString().trim();
            if (text.isEmpty()) continue;

            if (text.startsWith("◇")) {
                Pattern pattern = Pattern.compile("◇\\s*([A-Za-zåäöÅÄÖ ]+)\\s+(\\d+)×\\s+([^(/)]+)(?:\\s*\\((\\d+)/(\\d+)\\))?");
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    missionTranslationKey = extractTranslationKey(line);
                    missionAction = matcher.group(1).trim();
                    ID = missionAction + "_" + missionTranslationKey;
                    prettyMission = matcher.group(3).trim();
                    if (matcher.group(4) != null && matcher.group(5) != null) {
                        completed = Integer.parseInt(matcher.group(4));
                        total = Integer.parseInt(matcher.group(5));
                    } else {
                        completed = 0;
                        total = Integer.parseInt(matcher.group(2));
                    }
                }
            }

            if (text.startsWith("Belöning:")) {
                Pattern rewardPattern = Pattern.compile("(\\d+)\\s*kr\\s*&\\s*(\\d+)\\s*xp");
                Matcher matcher = rewardPattern.matcher(text);
                if (matcher.find()) {
                    priceMoney = Integer.parseInt(matcher.group(1));
                    priceXP = Integer.parseInt(matcher.group(2));
                }
            }

            if (text.startsWith("⌚ ")) {
                claimed = true;
                Pattern timePattern = Pattern.compile("⌚\\s*(\\d+)[a-z ]+(\\d+).+");
                Matcher matcher = timePattern.matcher(text);
                if (matcher.find()) {
                    int hours = Integer.parseInt(matcher.group(1));
                    int minutes = Integer.parseInt(matcher.group(2));
                    long durationMillis = (hours * 60L + minutes) * 60 * 1000;
                    expiresAt = System.currentTimeMillis() + durationMillis;
                }
            }
        }
    }
}