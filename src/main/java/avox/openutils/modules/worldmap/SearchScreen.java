package avox.openutils.modules.worldmap;

import avox.openutils.modules.stats.screen.widgets.Suggestor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static avox.openutils.OpenUtils.addToast;
import static avox.openutils.modules.worldmap.WorldMapModule.*;

public class SearchScreen extends Screen {
    private final Consumer<Vec3d> onComplete;

    private SearchField widgetX;
    private SearchField widgetZ;

    private SearchField bannerInput;
    private Suggestor suggestor;
    private ButtonWidget searchButton;

    private ButtonWidget coordTabBtn;
    private ButtonWidget mapIdTabBtn;
    private ButtonWidget bannerBtn;

    private static Vec2f coordSearch;
    private static Vec2f mapSearch;
    private static Tab activeTab = Tab.COORD;

    public SearchScreen(Consumer<Vec3d> onComplete) {
        super(Text.of("Search Screen"));
        this.onComplete = onComplete;
    }

    @Override
    protected void init() {
        super.init();
        widgetX = new SearchField(client.textRenderer, width / 3, 20, Text.empty());
        widgetX.setChangedListener(text -> fieldChanged(text, widgetX));
        widgetX.setEditableColor(0xFFA83832);
        widgetX.setPosition(width / 2 - 2 - width / 3, height / 2);
        widgetX.setVisible(!activeTab.equals(Tab.BANNER));
        widgetX.setMaxLength(6);
        addDrawableChild(widgetX);

        widgetZ = new SearchField(client.textRenderer, width / 3, 20, Text.empty());
        widgetZ.setChangedListener(text -> fieldChanged(text, widgetZ));
        widgetZ.setEditableColor(0xFFA83832);
        widgetZ.setPosition(width / 2 + 2, height / 2);
        widgetZ.setVisible(!activeTab.equals(Tab.BANNER));
        widgetZ.setMaxLength(6);
        addDrawableChild(widgetZ);

        bannerInput = new SearchField(client.textRenderer, width / 2, 20, Text.empty());
        bannerInput.setPosition(width / 4, height / 2);
        bannerInput.setVisible(activeTab.equals(Tab.BANNER));
        bannerInput.setChangedListener(newValue -> {
            fieldChanged(newValue, bannerInput);
            suggestor.updateSuggestionEntries();
        });
        addDrawableChild(bannerInput);

        this.suggestor = new Suggestor(bannerInput, client.textRenderer, 100, new ArrayList<>(BannerManager.banners.stream().map(BannerManager.Banner::name).toList()));

        int buttonWidth = (width - 40) / 3;
        coordTabBtn = ButtonWidget.builder(Text.of("Koordinater"), btn -> tabChanged(Tab.COORD))
                .dimensions(20, height / 2 - 50, buttonWidth, 20)
                .build();
        addDrawableChild(coordTabBtn);
        coordTabBtn.active = !activeTab.equals(Tab.COORD);
        mapIdTabBtn = ButtonWidget.builder(Text.of("Map ID"), btn -> tabChanged(Tab.MAP))
                .dimensions(20 + buttonWidth, height / 2 - 50, buttonWidth, 20)
                .build();
        addDrawableChild(mapIdTabBtn);
        mapIdTabBtn.active = !activeTab.equals(Tab.MAP);
        bannerBtn = ButtonWidget.builder(Text.of("Banner"), btn -> tabChanged(Tab.BANNER))
                .dimensions(20 + buttonWidth * 2, height / 2 - 50, buttonWidth, 20)
                .build();
        addDrawableChild(bannerBtn);
        bannerBtn.active = !activeTab.equals(Tab.BANNER);

        ButtonWidget eraseButton = ButtonWidget.builder(Text.of("Rensa och stäng"), btn -> erase())
                .dimensions(width / 4, height / 2 + 50, width / 4, 20)
                .build();
        addDrawableChild(eraseButton);

        searchButton = ButtonWidget.builder(Text.of("Klar"), btn -> search(client, widgetX, widgetZ))
                .dimensions(width / 2 + 2, height / 2 + 50, width / 4, 20)
                .build();
        addDrawableChild(searchButton);

        if (!activeTab.equals(Tab.BANNER)) {
            setTextSilent(widgetX, String.valueOf(activeTab.equals(Tab.COORD) ? (coordSearch == null ? "" : (int)coordSearch.x) : (mapSearch == null ? "" : (int)mapSearch.x)));
            setTextSilent(widgetZ, String.valueOf(activeTab.equals(Tab.COORD) ? (coordSearch == null ? "" : (int)coordSearch.y) : (mapSearch == null ? "" : (int)mapSearch.y)));
            fieldChanged(widgetX.getText(), widgetX);
            fieldChanged(widgetZ.getText(), widgetZ);
        } else {
            fieldChanged(bannerInput.getText(), bannerInput);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawCenteredTextWithShadow(textRenderer, activeTab.equals(Tab.COORD) ? "Vilka koordinater?" : (activeTab.equals(Tab.MAP) ? "Vilket map ID?" : "Välj en banner!"), width / 2, height / 2 - 20, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, deltaTicks);
        if (activeTab.equals(Tab.BANNER)) {
            suggestor.render(context, mouseX, mouseY);
        } else {
            context.drawText(textRenderer, Text.literal("X").formatted(Formatting.YELLOW, Formatting.BOLD), width / 2 - 14, height / 2 + 10 - textRenderer.fontHeight / 2, 0xFFFFFFFF, true);
            context.drawText(textRenderer, Text.literal("Z").formatted(Formatting.YELLOW, Formatting.BOLD), width / 2 + width / 3 - 10, height / 2 + 10 - textRenderer.fontHeight / 2, 0xFFFFFFFF, true);
        }
    }

    private void tabChanged(Tab pressed) {
        if (pressed.equals(Tab.COORD)) {
            coordSearch = getValue(widgetX, widgetZ);
        } else if (pressed.equals(Tab.MAP)) {
            mapSearch = getValue(widgetX, widgetZ);
        }

        activeTab = pressed;
        coordTabBtn.active = !activeTab.equals(Tab.COORD);
        mapIdTabBtn.active = !activeTab.equals(Tab.MAP);
        bannerBtn.active = !activeTab.equals(Tab.BANNER);
        if (!activeTab.equals(Tab.BANNER)) {
            widgetX.setVisible(true);
            widgetZ.setVisible(true);
            bannerInput.setVisible(false);
            setTextSilent(widgetX, String.valueOf(activeTab.equals(Tab.COORD) ? (coordSearch == null ? "" : (int)coordSearch.x) : (mapSearch == null ? "" : (int)mapSearch.x)));
            setTextSilent(widgetZ, String.valueOf(activeTab.equals(Tab.COORD) ? (coordSearch == null ? "" : (int)coordSearch.y) : (mapSearch == null ? "" : (int)mapSearch.y)));
            fieldChanged(widgetX.getText(), widgetX);
            fieldChanged(widgetZ.getText(), widgetZ);
        } else {
            widgetX.setVisible(false);
            widgetZ.setVisible(false);
            bannerInput.setVisible(true);
            fieldChanged(bannerInput.getText(), bannerInput);
        }
    }

    private void fieldChanged(String text, SearchField widget) {
        if (activeTab.equals(Tab.BANNER)) {
            searchButton.active = !BannerManager.banners.stream().filter(banner -> text.equals(banner.name())).toList().isEmpty();
        } else {
            if (text.matches("-?\\d{1,5}")) {
                if (activeTab.equals(Tab.COORD)) {
                    coordSearch = getValue(widgetX, widgetZ);
                } else {
                    mapSearch = getValue(widgetX, widgetZ);
                }
                int number = Integer.parseInt(text);
                if ((activeTab.equals(Tab.COORD) ? (-10000 <= number && number <= 10000) : (0 <= number && number <= 39))) {
                    widget.setEditableColor(0xFFFFFFFF);
                    widget.error = false;
                    searchButton.active = allowedCoords();
                    return;
                }
            }
            widget.setEditableColor(0xFFA83832);
            widget.error = true;

            searchButton.active = allowedCoords();
        }
    }

    private Vec2f getValue(SearchField x, SearchField z) {
        if (x != null && z != null) {
            return new Vec2f(x.getText().isEmpty() ? 0 : Integer.parseInt(x.getText()), z.getText().isEmpty() ? 0 : Integer.parseInt(z.getText()));
        }
        return null;
    }

    private void search(MinecraftClient client, SearchField widgetX, SearchField widgetZ) {
        if (activeTab.equals(Tab.COORD)) {
            double worldSizeHalf = 10000;
            double mapArtSize = 512;

            double mapIdX = ((Double.parseDouble(widgetX.getText()) + 304) / mapArtSize) - (-worldSizeHalf / mapArtSize);
            double mapIdZ = (worldSizeHalf / mapArtSize) - ((Double.parseDouble(widgetZ.getText()) + 336) / mapArtSize);

            onComplete.accept(new Vec3d(originBottomLeftMap.x + mapIdX, 58, originBottomLeftMap.z - mapIdZ));
        } else if (activeTab.equals(Tab.MAP)) {
            onComplete.accept(new Vec3d(originBottomLeftMap.x + Integer.parseInt(widgetX.getText()) + 0.5, 58, originBottomLeftMap.z - Integer.parseInt(widgetZ.getText()) + 0.5));
        } else {
            List<BannerManager.Banner> banners = BannerManager.banners.stream().filter(b -> b.name().equals(bannerInput.getText())).toList();
            if (!banners.isEmpty()) {
                try {
                    onComplete.accept(banners.getFirst().worldmap_location());
                } catch (Exception e) {
                    addToast(client, "§cWorldMap Error!", "Okänt error!");
                }
            } else {
                addToast(client, "§cWorldMap Error!", "Flera banners hittades!");
            }
        }

        close();
    }

    private void erase() {
        WorldMapModule.arrow = null;
        if (activeTab.equals(Tab.COORD)) {
            coordSearch = null;
        } else if (activeTab.equals(Tab.MAP)) {
            mapSearch = null;
        }
        close();
    }

    private void setTextSilent(SearchField field, String text) {
        var oldListener = field.changedListener;
        field.setChangedListener(s -> {});
        field.setText(text);
        field.setChangedListener(oldListener);
    }

    private boolean allowedCoords() {
        return !widgetX.error && !widgetZ.error;
    }

    public enum Tab {
        COORD,
        MAP,
        BANNER
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
}

