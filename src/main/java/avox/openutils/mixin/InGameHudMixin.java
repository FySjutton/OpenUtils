package avox.openutils.mixin;

import avox.openutils.OpenUtils;
import avox.openutils.modules.quests.QuestModule;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

import static avox.openutils.OpenUtils.playerInSurvival;
import static avox.openutils.modules.quests.QuestManager.newActionBar;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow private Text overlayMessage;
    @Shadow private int overlayRemaining;

    @Unique
    public Text serverMessage;
    @Unique
    public int serverMessageTicks;

    @Inject(method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V", at = @At("RETURN"))
    private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (QuestModule.INSTANCE.getConfig().moduleEnabled && playerInSurvival()) {
            newActionBar(message);
        }
        serverMessageTicks = 60;
        serverMessage = message;
    }

    @Inject(method = "tick(Z)V", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (serverMessageTicks > 0) {
            serverMessageTicks--;
            if (serverMessageTicks == 0) {
                serverMessage = null;
            }
        }

        if (!OpenUtils.getActiveActionBarSuppliers().isEmpty()) {
            overlayRemaining = 60;
            MutableText combined = Text.empty();
            if (serverMessage != null && !serverMessage.getString().isEmpty()) {
                combined.append(serverMessage);
            }

            for (Supplier<Text> supplier : OpenUtils.getActiveActionBarSuppliers()) {
                combined.append(Text.of(" ")).append(supplier.get());
            }
            overlayMessage = combined;
        } else {
            overlayMessage = serverMessage;
            if (serverMessage != null) {
                overlayRemaining = serverMessageTicks;
            }
        }
    }
}