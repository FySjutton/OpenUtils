package avox.openutils.mixin;

import avox.openutils.modules.ResourceAdvancementRemoverModule;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static avox.openutils.OpenUtils.LOGGER;

@Mixin(ClientAdvancementManager.class)
public class ClientAdvancementManagerMixin {

    @Redirect(
            method = "onAdvancements",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/toast/ToastManager;add(Lnet/minecraft/client/toast/Toast;)V"
            )
    )
    private void blockAdvancementToasts(ToastManager toastManager, Toast toast) {
        if (!ResourceAdvancementRemoverModule.removeAdvancements) {
            toastManager.add(toast);
        } else {
            LOGGER.info("Toast blocked!");
            LOGGER.info("type" + toast.getType().toString());
            LOGGER.info("toast" + toast.toString());
        }
    }
}
