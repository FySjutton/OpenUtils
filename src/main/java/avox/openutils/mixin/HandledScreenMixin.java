package avox.openutils.mixin;

import avox.openutils.OpenUtils;
import avox.openutils.modules.quests.QuestManager;
import avox.openutils.modules.quests.QuestModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static avox.openutils.OpenUtils.playerInSurvival;
import static avox.openutils.OpenUtils.taskQueue;
import static avox.openutils.modules.quests.QuestModule.QUEST_TITLE;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void mouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (QuestModule.INSTANCE.getConfig().moduleEnabled && playerInSurvival() && client.currentScreen.getTitle().getString().equals(QUEST_TITLE)) {
            taskQueue.add(new OpenUtils.DelayedTask(5, QuestManager::updateQuestList));
        }
    }
}