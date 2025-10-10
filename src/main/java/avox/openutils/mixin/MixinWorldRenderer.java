package avox.openutils.mixin;

import avox.openutils.modules.worldmap.BannerManager;
import avox.openutils.modules.worldmap.WorldMapModule;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static avox.openutils.modules.worldmap.BannerManager.banners;
import static avox.openutils.modules.worldmap.WorldMapModule.*;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;renderParticles(Lnet/minecraft/client/render/FrameGraphBuilder;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"
            )
    )
    private void afterEntitiesRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        if (INSTANCE.getConfig().moduleEnabled && withinArea && arrow != null) {
            VertexConsumerProvider.Immediate consumer = bufferBuilders.getEntityVertexConsumers();
            MatrixStack matrixStack = new MatrixStack();
            if (consumer != null) {
                renderTextureAtBlock(matrixStack, consumer, camera.getPos(), 2, Identifier.of("openutils", "textures/gui/map_pins/red.png"), arrow, 128, 128);
            }
        }
        if (INSTANCE.getConfig().moduleEnabled && withinArea && INSTANCE.getConfig().viewMapPins) {
            MatrixStack matrixStack = new MatrixStack();
            VertexConsumerProvider.Immediate consumer = bufferBuilders.getEntityVertexConsumers();

            BannerManager.Banner candidateBanner = null;
            double closestCandidateDist = Double.MAX_VALUE;
            float hidePinDistance = INSTANCE.getConfig().closeInvisiblePins ? 1.5f : 0;
            float maxNameDistance = 10f;

            Vec3d cameraPos = camera.getPos();
            Vec3d lookEnd = cameraPos.add(MinecraftClient.getInstance().player.getRotationVec(1.0f).normalize().multiply(maxNameDistance));

            for (BannerManager.Banner banner : banners) {
                if (consumer == null || banner.worldMapLocation().equals(arrow)) continue;

                Vec3d bannerCenter = banner.worldMapLocation().add(0.0, 0.5, 0.0);
                double dist = cameraPos.distanceTo(bannerCenter);

                if (dist > hidePinDistance) {
                    Identifier texture = BannerManager.getBanner(banner.color());
                    renderTextureAtBlock(matrixStack, consumer, cameraPos, 0.75f, texture, banner.worldMapLocation(), 128, 128);
                }

                if (dist >= hidePinDistance && dist <= maxNameDistance) {
                    Vec3d min = bannerCenter.subtract(0.5, 0.5, 0.5);
                    Vec3d max = bannerCenter.add(0.5, 0.5, 0.5);

                    if (rayIntersectsBox(cameraPos, lookEnd, min, max)) {
                        if (dist < closestCandidateDist) {
                            closestCandidateDist = dist;
                            candidateBanner = banner;
                        }
                    }
                }
            }

            if (candidateBanner != null) {
                Vec3d namePos = candidateBanner.worldMapLocation().add(0.0, 1.0, 0.0);
                renderNameTag(MinecraftClient.getInstance(), matrixStack, consumer, cameraPos, namePos, candidateBanner.name(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
            }
        }
    }
}
