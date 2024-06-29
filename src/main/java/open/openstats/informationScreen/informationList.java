package open.openstats.informationScreen;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import org.apache.commons.lang3.text.WordUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class informationList extends ElementListWidget<informationList.Entry> {
    private JsonObject data;

    public informationList(int width, int height, JsonObject data, ArrayList<String> infoList) {
        super(MinecraftClient.getInstance(), width, height - 24, 24, 25);

        this.data = data;
        for (String x : infoList) {
            addEntry(new Entry(x));
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
        private final String displayText;
        private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        public Entry(String setting) {
            String value;
            try {
                value = data.get(setting).getAsString();
            } catch (Exception e) {
                value = "";
            }
            this.displayText = getText(setting, value);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawCenteredTextWithShadow(textRenderer, displayText, width / 2, y + entryHeight / 2 - 9 / 2, 0xFFFFFF);
        }
    }

    private String getText(String oriSetting, String oriValue) {
        String setting = oriSetting;
        String value = oriValue;

        if (setting.equals("id") || setting.equals("uuid")) {
            setting = setting.toUpperCase();
        } else if (!setting.equals("gQmynt")) {
            setting = WordUtils.capitalizeFully(setting.replaceAll("_", " "));
        }

        if (!(oriSetting.equals("username") || oriSetting.equals("last_server"))) {
            value = WordUtils.capitalizeFully(value);
        }

        setting = setting
                .replaceAll("Mb", "MB")
                .replaceAll("Uhc", "UHC");

        switch (oriSetting) {
            case "survival_money" -> value += " kr";
            case "survival_experience" -> value += " XP";
            case "onlinetime" -> value = parseMillis(value);
            case "creative_rank" -> value = parseCreativeRank(oriValue);
        }

        if (oriValue.isEmpty()) {
            value = "§6N/A";
        } else if (oriValue.equals("True")) {
            value = "§a" + value;
        } else if (oriValue.equals("False")) {
            value = "§c" + value;
        }

        return setting + ": §7" + value;
    }

    public static String parseMillis(String millis) {
        try {
            long milliSecs = Long.parseLong(millis);

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

            if (months > 0) {sb.append(months).append(Text.translatable("openstats.month").getString());}
            if (days > 0) {sb.append(days).append(" d, ");}
            if (hours > 0) {sb.append(hours).append(" h, ");}
            if (minutes > 0) {sb.append(minutes).append(" min, ");}
            if (seconds > 0) {sb.append(seconds).append(Text.translatable("openstats.second").getString());}

            if (!sb.isEmpty() && sb.charAt(sb.length() - 2) == ',') {
                sb.delete(sb.length() - 2, sb.length());
            }

            return sb.toString();
        } catch (Exception e) {
            return millis;
        }
    }

    private String parseCreativeRank(String rank) {
        switch (rank) {
            case "newbie" -> rank = "§aNybörjare";
            case "apprentice" -> rank = "§bLärling";
            case "experienced" -> rank = "§dErfaren!";
            case "expert" -> rank = "§5Expert";
            case "architect" -> rank = "§6Arkitekt";
        }
        return rank;
    }
}
