package avox.openutils.mixin;

import avox.openutils.modules.quests.QuestModule;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static avox.openutils.OpenUtils.playerInSurvival;
import static avox.openutils.modules.quests.QuestManager.newActionBar;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (QuestModule.INSTANCE.getConfig().moduleEnabled && playerInSurvival()) {
            newActionBar(message);
        }
    }
}