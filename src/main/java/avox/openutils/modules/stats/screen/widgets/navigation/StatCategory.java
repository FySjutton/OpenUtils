package avox.openutils.modules.stats.screen.widgets.navigation;

import java.util.ArrayList;

public class StatCategory {
    public ArrayList<StatEntry> directStats = new ArrayList<>();
    public ArrayList<StatFolder> folders = new ArrayList<>();

    public String title;
    public String suffix = "";

    public StatCategory(String id) {
        this.title = id;
    }
}