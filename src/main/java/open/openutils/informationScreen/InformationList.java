package open.openutils.informationScreen;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.*;
import org.apache.commons.lang3.text.WordUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import static open.openutils.OpenUtils.LOGGER;

public class InformationList extends ElementListWidget<InformationList.Entry> {
    private final JsonObject data;
    private final LinkedHashMap<String, Boolean> view = new LinkedHashMap<>();
    private LinkedHashMap<String, ArrayList<String>> informationList;

    public InformationList(int width, int height, JsonObject data, LinkedHashMap<String, ArrayList<String>> infoList) {
        super(MinecraftClient.getInstance(), width, height - 24 - 15 - 10, 24 + 15 + 10, 25);

        this.data = data;
        updateViewList(infoList, false);
    }

    public void updateViewList(LinkedHashMap<String, ArrayList<String>> infoList, boolean search) {
        view.clear();

        for (String x : infoList.keySet()) {
            if (!x.equals("uncategorized")) {
                view.put(x, search);
            }
        }

        informationList = infoList;
        updateEntries();
    }

    public void updateEntries() {
        clearEntries();
        for (String category : informationList.keySet()) {
            if (!category.equals("uncategorized")) {
                addEntry(new Entry(true, category));
                if (view.get(category)) {
                    for (String stat : informationList.get(category)) {
                        addEntry(new Entry(false, stat));
                    }
                }
            } else {
                for (String stat : informationList.get(category)) {
                    addEntry(new Entry(false, stat));
                }
            }
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

    @Override
    protected void drawMenuListBackground(DrawContext context) {} // Removes the dark overlay

    public class Entry extends ElementListWidget.Entry<Entry> {
        private MutableText displayText;
        private ButtonWidget button;

        private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        public Entry(Boolean button, String setting) {
            if (button) {
                this.button = ButtonWidget.builder(Text.of(setting.replace("_", " ")), btn -> toggleButton(setting))
                        .dimensions((int) (width * 0.1), 0, (int) (width * 0.8), 20)
                        .build();
            } else {
                if (!setting.equals("noResults")) {
                    String value;
                    if (setting.contains("total_win_percent")) {
                        int totalGames = 0;
                        int totalWins = data.get("event_wins").getAsInt();;
                        for (String key : data.keySet()) {
                            try {
                                if (!key.contains("_games_played")) continue;
                                String gamemode = key.replaceAll("_games_played", "");
                                if (gamemode.equals("mb") || gamemode.equals("uhc")) continue;
                                int total = data.get(key).getAsInt();
                                totalGames += total;
                            } catch (Exception ignored) {}
                        }
                        value = String.format("%.3g", (double) totalWins / totalGames *  100) + "%";
                    } else if (setting.contains("_win_percent")) {
                        try {
                            String gamemode = setting.replaceAll("_win_percent", "");
                            int total = data.get(gamemode + "_games_played").getAsInt();
                            int wins = data.get(gamemode + "_wins").getAsInt();
                            value = String.format("%.3g", (total > 0 ? (double) wins / total * 100 : 0)) + "%";
                        } catch (Exception e) {
                            value = "-%";
                        }
                    } else {
                        try {
                            value = data.get(setting).getAsString();
                        } catch (Exception e) {
                            value = "";
                        }
                    }
                    this.displayText = getText(setting, value);
                } else {
                    this.displayText = Text.translatable("openutils.no_result");
                }
            }
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            List<Selectable> children = new ArrayList<>();
            if (button != null) {
                children.add(button);
            }
            return children;
        }

        @Override
        public List<? extends Element> children() {
            List<Element> children = new ArrayList<>();
            if (button != null) {
                children.add(button);
            }
            return children;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (button != null) {
                button.setY(y);
                button.render(context, mouseX, mouseY, tickDelta);
            } else {
                context.drawCenteredTextWithShadow(textRenderer, displayText, width / 2, y + entryHeight / 2 - 9 / 2, 0xFFFFFF);
            }
        }
    }

    private void toggleButton(String btn) {
        view.replace(btn, !view.get(btn));
        updateEntries();
    }

    private MutableText getText(String oriSetting, String oriValue) {
        String setting = oriSetting;
        String value = oriValue;
        MutableText coloredValue = null;

        if (Arrays.asList("id", "uuid").contains(setting)) {
            setting = setting.toUpperCase();
        } else if (!setting.equals("gQmynt")) {
            setting = WordUtils.capitalizeFully(setting.replaceAll("_", " "));
        }

        if (!(oriSetting.equals("username") || oriSetting.equals("last_server"))) {
            value = WordUtils.capitalizeFully(value);
        }

        setting = setting
                .replaceAll("Tnt", "TNT")
                .replaceAll("Sg", "SG")
                .replaceAll("Oitc", "OITC")
                .replaceAll("Mvp", "MVP")
                .replaceAll("Mb", "MB")
                .replaceAll("Uhc", "UHC");

        switch (oriSetting) {
            case "survival_money" -> value += " kr";
            case "survival_experience" -> value += " XP";
            case "onlinetime" -> value = parseMillis(oriValue);
            case "creative_rank" -> value = parseCreativeRank(oriValue);
            case "rank" -> coloredValue = parseRank(oriValue);
            case "oitc_longest_bow_kill" -> value += " blocks";
            case "lobby_parkour_time" -> value = parkourTime(oriValue);
            case "lobby_parkour_reward" -> value = "#" + value;
        }

        if (oriSetting.equals("participation") || oriSetting.equals("party_invites") || oriSetting.equals("random_skin") || oriSetting.equals("spectator_visibility")) {
            value = Text.translatable("openutils." + (oriValue.equals("1") ? "on" : "off")).getString();
        }

        if (oriSetting.equals("playtime_privacy")) {
            switch (value) {
                case "0" -> value = "§cIngen";
                case "1" -> value = "§aAlla";
                case "2" -> value = "§eEndast Vänner";
            }
        }

        if (oriSetting.equals("lobby_visibility")) {
            switch (value) {
                case "0" -> value = "§aAlla";
                case "1" -> value = "§dParty";
                case "2" -> value = "§8Ingen";
            }
        }

        if (oriValue.isEmpty()) {
            value = "§6N/A";
        } else if (oriValue.equals("True")) {
            value = "§a" + value;
        } else if (oriValue.equals("False")) {
            value = "§c" + value;
        }

        if (coloredValue == null) {
            return Text.literal(setting + ": §7" + value);
        } else {
            return Text.literal(setting + ": ").append(coloredValue);
        }
    }

    private static String parseMillis(String millis) {
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

    private String parseCreativeRank(String rank) {
        switch (rank) {
            case "newbie" -> rank = "§aNybörjare";
            case "apprentice" -> rank = "§bLärling";
            case "experienced" -> rank = "§dErfaren";
            case "expert" -> rank = "§5Expert";
            case "architect" -> rank = "§6Arkitekt";
        }
        return rank;
    }

    private static String parkourTime(String millis) {
        try {
            long milliSecs = Long.parseLong(millis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(milliSecs);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(milliSecs) % 60;
            long milliseconds = milliSecs % 1000;

            return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
        } catch (Exception e) {
            return millis;
        }
    }

    private MutableText parseRank(String rank) {
        MutableText text = Text.literal(rank.substring(0, 1).toUpperCase() + rank.substring(1));
        if (rank.equals("norendor")) {
            text = text.withColor(43690);
        } else if (rank.equals("avalon")) {
            text = text.withColor(5636095);
        } else if (rank.equals("nirethia")) {
            text = text.withColor(16755200);
        } else if (rank.equals("bashlang")) {
            text = Text.literal("B").withColor(5603327).append(Text.literal("a").withColor(5625087).append(Text.literal("s").withColor(5636053).append(Text.literal("h").withColor(5635968).append(Text.literal("l").withColor(8388437).append(Text.literal("a").withColor(13958997).append(Text.literal("n").withColor(16766293).append(Text.literal("g").withColor(16744533))))))));
        } else if (rank.equals("gq")) {
            text = Text.literal("90gq").withColor(43520);
        } else if (rank.equals("media")) {
            text = text.withColor(11104511);
        } else if (rank.equals("hjalpare")) {
            text = Text.literal("Hjälpare").withColor(5635925);
        } else if (rank.equals("moderator")) {
            text = text.withColor(16733525);
        } else if (rank.equals("byggare")) {
            text = text.withColor(5592575);
        } else if (rank.equals("utvecklare")) {
            text = text.withColor(3652597);
        } else if (rank.equals("admin")) {
            text = text.withColor(11141120);
        } else {
            return Text.literal("§7" + text.getString());
        }

        return Text.empty().append(text).setStyle(Style.EMPTY.withBold(true));
    }
}
