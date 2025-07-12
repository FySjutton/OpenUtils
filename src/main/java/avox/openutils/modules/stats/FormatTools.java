package avox.openutils.modules.stats;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FormatTools {
    private static final NumberFormat numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("sv-SE"));

    public static String toTitleCase(String input) {
        return Arrays.stream(input.trim().replaceAll("_", " ").split("\\s+"))
                .map(word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static String formatNumber(long number) {
        return numberFormat.format(number);
    }

    public static String parseMillis(String millis) {
        try {
            long milliSecs = Long.parseLong(millis.replaceAll("\\D", ""));

            long seconds = TimeUnit.MILLISECONDS.toSeconds(milliSecs);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(milliSecs);
            long hours = TimeUnit.MILLISECONDS.toHours(milliSecs);
            long days = TimeUnit.MILLISECONDS.toDays(milliSecs);
            long months = days / 30; // May not be true as not all months are 30 days, but yeah...

            days %= 30;
            hours %= 24;
            minutes %= 60;
            seconds %= 60;

            StringBuilder sb = new StringBuilder();

            if (months > 0) {sb.append(months).append(" mån, ");}
            if (days > 0) {sb.append(days).append(" d, ");}
            if (hours > 0) {sb.append(hours).append(" h, ");}
            if (minutes > 0) {sb.append(minutes).append(" min, ");}
            if (seconds > 0) {sb.append(seconds).append(" sek");}

            if (!sb.isEmpty() && sb.charAt(sb.length() - 2) == ',') {
                sb.delete(sb.length() - 2, sb.length());
            }

            return sb.toString();
        } catch (Exception e) {
            return millis;
        }
    }

    public static String parkourTime(String millis) {
        try {
            long milliSecs = Long.parseLong(millis.replaceAll("\\D", ""));
            long minutes = TimeUnit.MILLISECONDS.toMinutes(milliSecs);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(milliSecs) % 60;
            long milliseconds = milliSecs % 1000;

            return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
        } catch (Exception e) {
            e.printStackTrace();
            return millis;
        }
    }

    public static MutableText parseRank(String rank) {
        MutableText text = Text.literal(rank);
        switch (rank) {
            case "Norendor" -> text = text.withColor(43690);
            case "Avalon" -> text = text.withColor(5636095);
            case "Nirethia" -> text = text.withColor(16755200);
            case "Bashlang" ->
                    text = Text.literal("B").withColor(5603327).append(Text.literal("a").withColor(5625087).append(Text.literal("s").withColor(5636053).append(Text.literal("h").withColor(5635968).append(Text.literal("l").withColor(8388437).append(Text.literal("a").withColor(13958997).append(Text.literal("n").withColor(16766293).append(Text.literal("g").withColor(16744533))))))));
            case "Gq" -> text = Text.literal("90gq").withColor(43520);
            case "Media" -> text = text.withColor(11104511);
            case "Hjalpare" -> text = Text.literal("Hjälpare").withColor(5635925);
            case "Moderator" -> text = text.withColor(16733525);
            case "Byggare" -> text = text.withColor(5592575);
            case "Utvecklare" -> text = text.withColor(3652597);
            case "Admin" -> text = text.withColor(11141120);
            default -> {
                return Text.literal("§7" + text.getString());
            }
        }

        return Text.empty().append(text).setStyle(Style.EMPTY.withBold(true));
    }

    public static MutableText parseCreativeRank(String rank) {
        MutableText result = Text.literal(rank);
        switch (rank) {
            case "Newbie" -> result = Text.literal("Nybörjare").withColor(0x7cdd36);
            case "Apprentice" -> result = Text.literal("Lärling").withColor(0x61a8ea);
            case "Experienced" -> result = Text.literal("Erfaren").withColor(0xe869d2);
            case "Expert" -> result = Text.literal("Expert").withColor(0xa945e3);
            case "Architect" -> result = Text.literal("Arkitekt").withColor(0xefbc18);
        }
        return result;
    }

    public static MutableText parseParkourName(String name) {
        MutableText result = Text.literal(name);
        switch (name) {
            case "Nybörjare" -> result = result.withColor(0x55FF55);
            case "Lätt" -> result = result.withColor(0x00AA00);
            case "Medium" -> result = result.withColor(0xFFAA00);
            case "Svår" -> result = result.withColor(0xAA0000);
            case "Extrem" -> result = result.withColor(0x00AAAA);
            case "Insane" -> result = result.withColor(0xFF55FF);
            case "Mardröm" -> result = result.withColor(0xFFFFFF);
        }
        return result;
    }

    public static String reformatDate(String isoDate) {
        ZonedDateTime zdt = ZonedDateTime.parse(isoDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return zdt.format(formatter);
    }

    public static int experienceToLevel(int experience, boolean player) {
        if (player) {
            return (int) (-60 + Math.sqrt(40 * experience + 6400)) / 20;
        }
        return (int) ((Math.sqrt(200 * experience + 2560000) - 1500) / 100);
    }

    public static int levelToExperience(int level, boolean player) {
        if (player) {
            return (int) (10 * (Math.pow(level, 2) + 6 * level - 7));
        }
        return (int) ((Math.pow(100 * level + 1500, 2) - 2560000) / 200);
    }
}