package open.openutils.informationScreen;

import com.google.gson.JsonElement;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TabButtonWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class InfoScreen extends Screen {
    private static JsonElement data;
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    private final Identifier SEARCH_ICON = Identifier.ofVanilla("icon/search");
    private final LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> informationLines = infoLines.informationLines;

    public InfoScreen(JsonElement data) {
        super(Text.of("OpenUtils"));
        InfoScreen.data = data;
    }

    @Override
    public void init() {
        Tab[] tabs = new Tab[informationLines.size()];
        int index = 0;
        for (String x : informationLines.keySet()) {
            tabs[index] = new newTab(x, informationLines.get(x));
            index++;
        }

        TabNavigationWidget tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width).tabs(tabs).build();
        this.addDrawableChild(tabNavigation);

        SearchField searchbar = new SearchField(textRenderer, width, this);
        this.addDrawableChild(searchbar);
        tabNavigation.selectTab(0, false);
        tabNavigation.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawGuiTexture(RenderLayer::getGuiTextured, SEARCH_ICON, width / 2 - width / 6 - 15, 31, 12, 12);
    }

    private class newTab extends GridScreenTab {
        public InformationList infoList;
        public newTab(String tabName, LinkedHashMap<String, ArrayList<String>> info_list) {
            super(Text.of(tabName));
            GridWidget.Adder adder = grid.createAdder(1);

            infoList = new InformationList(width, height, InfoScreen.data.getAsJsonObject(), info_list);
            adder.add(infoList);
        }
    }

    public void search(String text) {
        for (Element tab : ((TabNavigationWidget) this.children().get(0)).children()) {
            newTab tabElm = (newTab) ((TabButtonWidget) tab).getTab();
            if (!text.isEmpty()) {
                LinkedHashMap<String, ArrayList<String>> categoryElm = informationLines.get(tabElm.getTitle().getString());
                LinkedHashMap<String, ArrayList<String>> searchResults = new LinkedHashMap<>();

                for (String category : categoryElm.keySet()) {
                    ArrayList<String> matches = new ArrayList<>();
                    for (String stat : categoryElm.get(category)) {
                        if (stat.replaceAll("_" ," ").toLowerCase().contains(text.toLowerCase())) {
                            matches.add(stat);
                        }
                    }
                    if (!matches.isEmpty()) {
                        searchResults.put(category, matches);
                    }
                }
                if (searchResults.isEmpty()) {
                    searchResults.put("uncategorized", new ArrayList<>(List.of("noResults")));
                }
                tabElm.infoList.updateViewList(searchResults, true);
            } else {
                tabElm.infoList.updateViewList(informationLines.get(tabElm.getTitle().getString()), false);
            }
        }
    }
}
