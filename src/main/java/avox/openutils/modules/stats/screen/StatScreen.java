package avox.openutils.modules.stats.screen;

import avox.openutils.modules.stats.screen.types.StatTabTypes;
import avox.openutils.modules.stats.screen.widgets.boxes.GuildTabBox;
import avox.openutils.modules.stats.screen.widgets.boxes.ParkourTabBox;
import avox.openutils.modules.stats.screen.widgets.boxes.PlayerTabBox;
import avox.openutils.modules.stats.screen.widgets.boxes.TotalTabBox;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.text.Text;
import avox.openutils.modules.stats.screen.widgets.SearchBox;
import avox.openutils.modules.stats.screen.widgets.Suggestor;

public class StatScreen extends Screen {
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);

    public StatScreen() {
        super(Text.of("OpenModpack"));
    }

    @Override
    public void init() {
        Tab[] tabs = new Tab[4];

        tabs[0] = new StatScreen.newTab(StatTabTypes.PLAYER);
        tabs[1] = new StatScreen.newTab(StatTabTypes.GUILD);
        tabs[2] = new StatScreen.newTab(StatTabTypes.PARKOUR);
        tabs[3] = new StatScreen.newTab(StatTabTypes.TOTAL);

        TabNavigationWidget tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width).tabs(tabs).build();
        this.addDrawableChild(tabNavigation);

        tabNavigation.selectTab(0, false);
        tabNavigation.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    private class newTab extends GridScreenTab {
        public SearchBox searchBox;

        public newTab(StatTabTypes category) {
            super(Text.of(category.name().toUpperCase()));
            GridWidget.Adder adder = grid.createAdder(1);
            StatViewer statViewer = new StatViewer(category, client, width, height);
            adder.add(statViewer);
            if (category.equals(StatTabTypes.PLAYER)) {
                statViewer.specialMessage = "Skriv in en spelares namn för att söka upp information om den.\n\nVälj också vilka delservrar du vill få statistik för.";
                searchBox = new PlayerTabBox(client, width, height, statViewer).searchBox;
            } else if (category.equals(StatTabTypes.GUILD)) {
                statViewer.specialMessage = "Skriv in en guilds namn för att söka upp information om den.\n\nMemberlist är ifall du vill ha en lista på guildens medlemmar.";
                searchBox = new GuildTabBox(client, width, height, statViewer).searchBox;
            } else if (category.equals(StatTabTypes.PARKOUR)) {
                statViewer.specialMessage = "Skriv in namnet på en parkour för att söka upp information om den.\n\nLength är längden på topplistan.";
                searchBox = new ParkourTabBox(client, width, height, statViewer).searchBox;
            } else {
                statViewer.specialMessage = "Skriv in vad för någonting som du vill den totala mängden av.";
                searchBox = new TotalTabBox(client, width, height, statViewer).searchBox;
            }
            adder.add(searchBox);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (tabManager.getCurrentTab() != null) {
            Suggestor suggestor = ((newTab) tabManager.getCurrentTab()).searchBox.suggestor;
            if (suggestor.textField.isFocused()) {
                if (keyCode == 258) { // tab
                    if (!suggestor.filteredSuggestions.isEmpty()) {
                        suggestor.textField.setText(suggestor.filteredSuggestions.get(suggestor.scroll + suggestor.highlight));
                        return false;
                    }
                } if (keyCode == 264) { // down
                    if (suggestor.displaySuggestions.size() - 1 > suggestor.highlight) {
                        suggestor.highlight ++;
                    } else {
                        suggestor.scroll++;
                    }
                    suggestor.updateSuggestions();
                    return false;
                } else if (keyCode == 265) { // up
                    if (suggestor.highlight > 0) {
                        suggestor.highlight --;
                    } else {
                        suggestor.scroll --;
                    }
                    suggestor.updateSuggestions();
                    return false;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}