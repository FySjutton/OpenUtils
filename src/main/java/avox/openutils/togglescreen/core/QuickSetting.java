package avox.openutils.togglescreen.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.List;
import java.util.function.Supplier;

public interface QuickSetting {
    String getTitle();
    void onClick(MinecraftClient client);

    default int getColor() {
        return 0xff696969;
    }

    default List<Widget> getWidgets() {
        return List.of();
    }

    record Widget(ClickableWidget widget, Supplier<Boolean> active) {}
}
