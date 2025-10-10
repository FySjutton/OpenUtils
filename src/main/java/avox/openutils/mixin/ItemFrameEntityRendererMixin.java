package avox.openutils.mixin;

import avox.openutils.modules.worldmap.WorldMapModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntityRenderer.class)
public abstract class ItemFrameEntityRendererMixin {
    @Inject(
            method = "hasLabel(Lnet/minecraft/entity/decoration/ItemFrameEntity;D)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventLabelIfInMap(ItemFrameEntity itemFrameEntity, double d, CallbackInfoReturnable<Boolean> cir) {
        if (WorldMapModule.INSTANCE.getConfig().removeItemFrameNames && WorldMapModule.withinArea && !MinecraftClient.getInstance().isAltPressed()) {
            cir.setReturnValue(false);
        }
    }
}