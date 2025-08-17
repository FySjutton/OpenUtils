package avox.openutils.modules.stock.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class SearchField extends TextFieldWidget {
    private Timer timer;
    private static final long DELAY = 800;

    public SearchField(TextRenderer textRenderer, int x, int y, int width, int height, Consumer<String> search) {
        super(textRenderer, x, y, width, height, Text.of("Search Bar"));
        super.setChangedListener(text -> {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    search.accept(text);
                    timer.cancel();
                }
            }, DELAY);
        });
    }
}