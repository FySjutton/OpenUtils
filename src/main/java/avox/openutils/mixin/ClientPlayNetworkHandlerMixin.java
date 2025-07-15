package avox.openutils.mixin;

import avox.openutils.modules.ResourceAdvancementRemoverModule;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.recipe.display.RecipeDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static avox.openutils.OpenUtils.LOGGER;

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
        if (!ResourceAdvancementRemoverModule.removeAdvancements) {
            RecipeToast.show(toastManager, display);
        } else {
            LOGGER.info("Recipe blocked!");
//            LOGGER.info("type" + display.getType().toString());
//            LOGGER.info("toast" + toast.toString());
        }
    }
}
