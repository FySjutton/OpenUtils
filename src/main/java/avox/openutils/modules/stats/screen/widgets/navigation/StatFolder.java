package avox.openutils.modules.stats.screen.widgets.navigation;

import java.util.ArrayList;

public class StatFolder {
    public ArrayList<StatEntry> entries = new ArrayList<>();
    public boolean open = false;
    public String name;
    public String id;

    public StatFolder(String name) {
        this.name = name;
    }
}