package avox.openutils.modules.stock.screen;

import avox.openutils.modules.stock.StockModule;
import avox.openutils.modules.stock.screen.selectWidget.SelectPartWidget;
import avox.openutils.modules.stock.screen.selectWidget.SelectWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

public class StockScreen extends Screen {
    private final MinecraftClient client;
    private ListWidget listWidget;
    private SelectWidget selectWidget;

    public StockScreen() {
        super(Text.of("Stock Screen"));
        client = MinecraftClient.getInstance();
    }

    @Override
    protected void init() {
        listWidget = new ListWidget(width, height);
        addDrawableChild(listWidget);

        addDrawableChild(ButtonWidget.builder(Text.of("Öppna vanliga"), btn -> {
            if (client.getNetworkHandler() == null) return;
            StockModule.ignoreNextScreen = true;
            close();
            client.getNetworkHandler().sendChatCommand("stock guild");
        }).dimensions(10, client.getWindow().getScaledHeight() - 25, 100, 20).build());

        this.selectWidget = new SelectWidget(client, width, height, Map.of("books", true, "trims", true, "potions", true));
        addSelectableChild(selectWidget);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        selectWidget.render(context, mouseX, mouseY, deltaTicks);
    }
}
