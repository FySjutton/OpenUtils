package avox.openutils.mixin;

import avox.openutils.modules.AdvancementRemoverModule;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.recipe.display.RecipeDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Redirect(
            method = "onRecipeBookAdd",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/toast/RecipeToast;show(Lnet/minecraft/client/toast/ToastManager;Lnet/minecraft/recipe/display/RecipeDisplay;)V"
            )
    )
    private void blockAdvancementToasts(ToastManager toastManager, RecipeDisplay display) {
        if (!AdvancementRemoverModule.removeAdvancements) {
            RecipeToast.show(toastManager, display);
        }
    }
}
