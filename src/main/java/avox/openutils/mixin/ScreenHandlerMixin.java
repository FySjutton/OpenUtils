package avox.openutils.mixin;

import avox.openutils.modules.stock.StockModule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @Inject(method = "onClosed", at = @At("HEAD"))
    private void onClosed(PlayerEntity player, CallbackInfo ci) {
        StockModule.screenOpen = false;
        StockModule.ignoreNextScreen = false;
    }
}