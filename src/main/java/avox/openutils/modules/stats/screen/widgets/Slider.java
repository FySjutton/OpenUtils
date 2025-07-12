package avox.openutils.modules.stats.screen.widgets;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class Slider extends SliderWidget {
    public Slider(int x, int y, int width, int height, Text text, double value) {
        super(x, y, width, height, text, value);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Text.of("Length: " + getDisplayScaled()));
    }

    @Override
    protected void applyValue() {}

    public int getDisplayScaled() {
        return (int) (Math.max(0, Math.min(1, value)) * 49) + 1;
    }
}