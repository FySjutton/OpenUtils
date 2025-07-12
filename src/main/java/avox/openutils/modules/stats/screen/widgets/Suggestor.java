package avox.openutils.modules.stats.screen.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Suggestor {
    public final TextFieldWidget textField;
    private final TextRenderer textRenderer;

    public ArrayList<Integer> lengths = new ArrayList<>();
    public ArrayList<String> filteredSuggestions = new ArrayList<>();
    public ArrayList<String> displaySuggestions = new ArrayList<>();
    private final List<String> suggestions;
    private final List<String> entrySuggestions;

    public int scroll = 0;
    public int highlight = 0;
    public final int maxSuggestions;

    public int startX = 0;
    public int startY = 0;
    public int width = 0;
    public int height = 0;

    public double lastMouseX;
    public double lastMouseY;

    public boolean render = false;

    public Suggestor(TextFieldWidget textField, TextRenderer textRenderer, int maxHeight, ArrayList<String> suggestions) {
        this.textField = textField;
        this.textRenderer = textRenderer;
        this.maxSuggestions = maxHeight / 11;
        this.suggestions = suggestions;
        this.entrySuggestions = suggestions.stream().map(String::toLowerCase).toList();
    }

    public void updateSuggestionEntries() {
        String searchContent = textField.getText();

        filteredSuggestions.clear();
        lengths.clear();

        if (!entrySuggestions.contains(searchContent.toLowerCase())) {
            for (String suggestion : suggestions) {
                if (suggestion.toLowerCase().contains(searchContent.toLowerCase())) {
                    filteredSuggestions.add(suggestion);
                    lengths.add(textRenderer.getWidth(suggestion));
                }
            }
        }

        updateSuggestions();
    }

    public void updateSuggestions() {
        displaySuggestions.clear();
        if (filteredSuggestions.size() > maxSuggestions) {
            scroll = Math.clamp(scroll, 0, filteredSuggestions.size() - maxSuggestions);
        } else {
            scroll = 0;
        }

        int end = Math.min(scroll + maxSuggestions, filteredSuggestions.size());
        if (end == 0) {
            render = false;
            return;
        }

        displaySuggestions = new ArrayList<>(filteredSuggestions.subList(scroll, end));
        width = Collections.max(new ArrayList<>(lengths.subList(scroll, end)));
        height = displaySuggestions.size() * 11;
        if (!displaySuggestions.isEmpty()) {
            render = true;
        }
    }

    public void render(DrawContext context, double mouseX, double mouseY) {
        if (!render) {
            return;
        }
        startX = textField.getX();
        startY = textField.getY() + 20;
        int y = startY + 3;
        int x = startX + 3;

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();

        context.fill(x - 3, y - 3, x + width + 3, y + displaySuggestions.size() * 11, 0xcc000000);

        if ((lastMouseX != mouseX || lastMouseY != mouseY) && startX < mouseX && (startX + width) > mouseX && startY < mouseY && (startY + height) > mouseY) {
            highlight = (int) Math.ceil((mouseY - startY) / 11) - 1;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        for (String suggestion : displaySuggestions) {
            if (displaySuggestions.indexOf(suggestion) == highlight) {
                context.drawTextWithShadow(textRenderer, suggestion, x, y, 0xffffe736);
            } else {
                context.drawTextWithShadow(textRenderer, suggestion, x, y, 0xFFFFFFFF);
            }
            y += 11;
        }
        matrices.popMatrix();
    }

    public void mouseScrolled(boolean up, double mouseX, double mouseY) {
        scroll += up ? 1 : -1;
        updateSuggestions();
    }

    public void mouseClicked() {
        textField.setText(filteredSuggestions.get(scroll + highlight));
    }
}