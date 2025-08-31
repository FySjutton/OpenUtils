package avox.openutils.modules.worldmap;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class SearchField extends TextFieldWidget {
    public boolean error = true;

    public SearchField(TextRenderer textRenderer, int width, int height, Text text) {
        super(textRenderer, width, height, text);
    }
}