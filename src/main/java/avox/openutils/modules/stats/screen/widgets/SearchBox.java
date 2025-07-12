package avox.openutils.modules.stats.screen.widgets;

import avox.openutils.modules.stats.screen.StatViewer;
import avox.openutils.modules.stats.screen.types.EntryType;
import avox.openutils.modules.stats.screen.widgets.navigation.StatCategory;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import avox.openutils.modules.stats.InformationFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class SearchBox extends ElementListWidget<SearchBox.Entry> {
    private final MinecraftClient client;
    private final Identifier SEARCH_ICON = Identifier.ofVanilla("icon/search");
    public final StatViewer statViewer;
    private ButtonWidget searchButton;
    private final Function<JsonObject, ArrayList<StatCategory>> getCategories;
    private final Function<String, String> getAPIString;
    private final Function<String, Boolean> verifyInput;
    private final Text title;
    public Suggestor suggestor;

    public SearchBox(MinecraftClient client, int width, int height, StatViewer statViewer, Text title, Function<JsonObject, ArrayList<StatCategory>> parseJsonFunction, Function<String, String> getAPIString, Function<String, Boolean> verifyInput, ArrayList<String> suggestions) {
        super(client, width / 3, height - 24 - 15 - 10, 24 + 10, 25);
        this.client = client;
        this.statViewer = statViewer;
        this.title = title;
        setX(10);

        addEntry(new Entry(EntryType.Title, ""));
        addEntry(new Entry(EntryType.SearchBox, ""));

        this.suggestor = new Suggestor(getEntry(1).textFieldWidget, client.textRenderer, 100, suggestions);

        getCategories = parseJsonFunction;
        this.getAPIString = getAPIString;
        this.verifyInput = verifyInput;
    }

    @Override
    protected int getScrollbarX() {
        return width;
    }

    @Override
    public int getRowWidth() {
        return width - 20;
    }

    public class Entry extends ElementListWidget.Entry<SearchBox.Entry> {
        public CheckboxWidget checkboxWidget;
        private TextFieldWidget textFieldWidget;
        public Slider sliderWidget;
        private boolean titleEntry = false;

        public Entry(EntryType entryType, String data) {
            if (entryType.equals(EntryType.Title)) {
                titleEntry = true;
            } else if (entryType.equals(EntryType.Checkbox)) {
                checkboxWidget = CheckboxWidget.builder(Text.of(data), client.textRenderer).build();
            } else if (entryType.equals(EntryType.SearchBox)) {
                textFieldWidget = new TextFieldWidget(client.textRenderer, width - 42, 20, Text.empty());
                textFieldWidget.setChangedListener(newValue -> {
                    searchButton.active = verifyInput.apply(newValue);
                    suggestor.updateSuggestionEntries();
                });
                searchButton = ButtonWidget.builder(Text.empty(), btn -> search()).dimensions(0, 0, 20, 20).build();
                searchButton.active = false;
            } else if (entryType.equals(EntryType.Slider)) {
                sliderWidget = new Slider(0, 0, width - 20, 20, Text.empty(), (double) 9 / 49);
            }
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            List<Selectable> children = new ArrayList<>();
            if (checkboxWidget != null) {
                children.add(checkboxWidget);
            }
            if (textFieldWidget != null) {
                children.add(textFieldWidget);
                children.add(searchButton);
            }
            if (sliderWidget != null) {
                children.add(sliderWidget);
            }
            return children;
        }

        @Override
        public List<? extends Element> children() {
            List<Element> children = new ArrayList<>();
            if (checkboxWidget != null) {
                children.add(checkboxWidget);
            }
            if (textFieldWidget != null) {
                children.add(textFieldWidget);
                children.add(searchButton);
            }
            if (sliderWidget != null) {
                children.add(sliderWidget);
            }
            return children;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (titleEntry) {
                context.drawCenteredTextWithShadow(client.textRenderer, title, 15 + entryWidth / 2, y + entryHeight / 2 - client.textRenderer.fontHeight / 2, 0xFFFFFFFF);
            }

            if (checkboxWidget != null) {
                checkboxWidget.setPosition(15, y);
                checkboxWidget.render(context, mouseX, mouseY, tickDelta);
            }
            if (textFieldWidget != null) {
                textFieldWidget.setPosition(15, y);
                textFieldWidget.render(context, mouseX, mouseY, tickDelta);

                searchButton.setPosition(15 + entryWidth - 20, y);
                searchButton.render(context, mouseX, mouseY, tickDelta);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SEARCH_ICON, 15 + entryWidth - 17, y + 4, 12, 12);
            }
            if (sliderWidget != null) {
                sliderWidget.setPosition(15, y);
                sliderWidget.render(context, mouseX, mouseY, tickDelta);
            }
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        this.suggestor.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (suggestor.render && suggestor.startX < mouseX && (suggestor.startX + suggestor.width) > mouseX && suggestor.startY < mouseY && (suggestor.startY + suggestor.height) > mouseY) {
            this.suggestor.mouseClicked();
            return false;
        } else {
            if (suggestor.textField.isFocused() && !suggestor.textField.isHovered()) {
                suggestor.textField.setFocused(false);
                suggestor.render = false;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (suggestor.render) {
            if (suggestor.startX < mouseX && suggestor.startX + suggestor.width > mouseX && suggestor.startY < mouseY && suggestor.startY + suggestor.height > mouseY) {
                verticalAmount = MathHelper.clamp(verticalAmount, -1.0, 1.0);
                this.suggestor.mouseScrolled(verticalAmount < 0, mouseX, mouseY);
                return false;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public void addEntry(EntryType entryType, String data) {
        addEntry(new Entry(entryType, data));
    }

    private void search() {
        String searchText = getEntry(1).textFieldWidget.getText().toLowerCase();
        if (!verifyInput.apply(searchText)) {
            return;
        }

        searchButton.active = false;

        statViewer.updateEntries(new ArrayList<>());
        statViewer.specialMessage = "Loading...";
        statViewer.drawMessage = true;

        CompletableFuture.runAsync(() -> {
            try {
                JsonObject data = InformationFetcher.fetchFromAPI(getAPIString.apply(searchText), statViewer);
                if (data != null) {
                    ArrayList<StatCategory> categories = getCategories.apply(data);
                    client.execute(() -> {
                        statViewer.updateEntries(categories);
                        Executors.newSingleThreadScheduledExecutor().schedule(() -> client.execute(() -> searchButton.active = true), 3, TimeUnit.SECONDS);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}