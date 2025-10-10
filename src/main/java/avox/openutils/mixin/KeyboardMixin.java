package avox.openutils.mixin;

import avox.openutils.modules.worldmap.SearchScreen;
import avox.openutils.modules.worldmap.WorldMapModule;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.currentScreen == null && WorldMapModule.INSTANCE.getConfig().moduleEnabled && WorldMapModule.withinArea && action == GLFW.GLFW_PRESS) {
            boolean ctrlDown = (input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0;

            if (ctrlDown && input.key() == GLFW.GLFW_KEY_F) {
                client.setScreen(new SearchScreen((location) -> WorldMapModule.arrow = location));

                ci.cancel();
            }
        }
    }
}
