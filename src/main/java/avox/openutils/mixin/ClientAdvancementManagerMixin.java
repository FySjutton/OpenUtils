package avox.openutils.mixin;

import avox.openutils.modules.AdvancementRemoverModule;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
        if (!AdvancementRemoverModule.removeAdvancements) {
            toastManager.add(toast);
        }
    }
}
