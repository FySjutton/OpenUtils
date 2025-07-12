package avox.openutils.modules.stats;

import avox.openutils.modules.stats.screen.types.StatTabTypes;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

import static avox.openutils.modules.stats.FormatTools.*;

public class Formatter {
    public static MutableText formatCategory(StatTabTypes tab, String category, String suffix) {
        if (category.equals("parkour")) {
            category = "Klarade parkours";
        }
        category = toTitleCase(category);
        category = category
                .replaceAll("Uhc", "UHC")
                .replaceAll("Mb", "MB");
        return Text.literal(category + suffix);
    }

    public static MutableText formatFolder(StatTabTypes tab, String text, String category) {
        String newText = text;
        MutableText returnText;

        newText = toTitleCase(newText.replaceAll("_", " "));

        newText = newText
                .replaceAll("Oitc", "OITC")
                .replaceAll("Sg", "Survival Games")
                .replaceAll("Tnt", "TNT")
                .replaceAll("Season", "This Season");

        returnText = Text.literal(newText);
        return returnText;
    }

    public static MutableText formatKey(StatTabTypes tab, String key, String category, String folder) {
        String newKey = key;
        MutableText returnText;

        if (List.of("id", "uuid").contains(key)) {
            newKey = newKey.toUpperCase();
        } else if (!key.equals("gQmynt") && !category.equals("members") && !category.contains("leaderboard") && !folder.equals("builders")) {
            newKey = toTitleCase(newKey.replaceAll("_", " "));
        }

        if (tab.equals(StatTabTypes.PLAYER) && category.equals("parkour") && key.equals("id")) {
            newKey = "Parkour ID";
        }

        if (key.equals("null")) {
            newKey = "Unknown";
        }

        newKey = newKey
                .replaceAll("Oitc", "OITC")
                .replaceAll("Sg", "Survival Games")
                .replaceAll("Tnt", "TNT")
                .replaceAll("Mvps", "MVPs");

        returnText = Text.literal(newKey);
        return returnText;
    }

    public static MutableText formatValue(StatTabTypes tab, String value, String key, String category, String folder) {
        String newValue = value;
        MutableText returnText;

        if (key.equals("progress")) {
            boolean survival = category.equals("survival");
            int experience = Integer.parseInt(newValue);
            int experienceForNext = levelToExperience(experienceToLevel(experience, survival) + 1, survival);
            int progress = (int) (((double)experience / experienceForNext) * 100);
            newValue = String.format("%.2f", (double)experience / experienceForNext * 100) + "% §a" + "▏".repeat(progress) + "§c" + "▏".repeat(Math.max(0, 100 - progress));
        }

        if (newValue.matches("\\d+")) {
            newValue = formatNumber(Long.parseLong(newValue));
        }

        if (!key.equals("last_server") && !key.equals("username") && !newValue.equals("N/A")) {
            newValue = toTitleCase(newValue);
        }

        if (key.equals("onlinetime")) {
            newValue = parseMillis(value);
        }

        if (key.equals("lobby_parkour_time") || category.equals("leaderboard_speed")) {
            newValue = parkourTime(newValue);
        }

        if (key.equals("first_join") || (category.equals("guild") && key.equals("created"))) {
            newValue = reformatDate(newValue);
        }

        if (category.equals("parkour")) {
            if (key.equals("record")) {
                newValue = parkourTime(newValue);
            } else if (key.equals("rank")) {
                newValue = "#" + newValue;
            }
        }

        switch (key) {
            case "money" -> newValue += " kr";
            case "experience" -> newValue += " XP";
            case "quest_streak" -> newValue += " dagar";
        }

        if (category.equals("members")) {
            switch (newValue) {
                case "Owner" -> newValue = "§c" + newValue;
                case "Moderator" -> newValue = "§6" + newValue;
                case "Member" -> newValue = "§e" + newValue;
            }
        }

        returnText = Text.literal(newValue);

        if (key.equals("rank")) {
            switch (category) {
                case "Player" -> returnText = parseRank(newValue);
                case "creative" -> returnText = parseCreativeRank(newValue);
            }
        }

        if (category.equals("parkour info") && key.equals("difficulty")) {
            returnText = parseParkourName(newValue);
        }

        return returnText;
    }
}