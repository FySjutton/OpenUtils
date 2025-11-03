package avox.openutils.togglescreen.core;

import net.minecraft.client.MinecraftClient;

public interface QuickToggleable extends QuickSetting {
    boolean isEnabled();
    void onToggle();

    @Override
    default int getColor() {
        return isEnabled() ? 0xff65ff57 : 0xffff5757;
    }

    @Override
    default void onClick(MinecraftClient client) {
        onToggle();
    }
}
