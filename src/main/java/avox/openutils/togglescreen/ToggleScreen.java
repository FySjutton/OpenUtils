package avox.openutils.togglescreen;

import avox.openutils.config.ConfigSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ToggleScreen extends Screen {
    public ToggleScreen() {
        super(Text.of("Toggle Modules"));
    }

    @Override
    protected void init() {
        super.init();

        ToggleWidget toggleWidget = new ToggleWidget(client, width, height - 50, 20, 25);
        addDrawableChild(toggleWidget);

        addDrawableChild(ButtonWidget.builder(Text.of("Open full config"), btn -> MinecraftClient.getInstance().setScreen(ConfigSystem.configScreen(null))).dimensions(5, height - 25, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        context.drawText(textRenderer, "Quick Settings...", 7, 7, 0xFFFFFFFF, true);
    }
}