package avox.openutils.togglescreen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ToggleScreen extends Screen {
    public ToggleScreen() {
        super(Text.of("Toggle Modules"));
    }

    @Override
    protected void init() {
        super.init();

        ToggleWidget toggleWidget = new ToggleWidget(client, width, height - 40, 20, 40);
        addDrawableChild(toggleWidget);
    }
}