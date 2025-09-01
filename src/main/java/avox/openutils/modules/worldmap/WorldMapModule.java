package avox.openutils.modules.worldmap;

import avox.openutils.Module;
import avox.openutils.SubserverManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.*;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.lang.Math;
import java.util.*;
import java.util.List;

import static avox.openutils.modules.worldmap.BannerManager.banners;

public class WorldMapModule extends Module<WorldMapModule.Config> {
    public static final WorldMapModule INSTANCE = new WorldMapModule(MinecraftClient.getInstance());
    public static final Vec3d originBottomLeftMap = new Vec3d(8843, 58, -7366);
    public static final Vec3d originBottomLeft = new Vec3d(8839, 58, -7362);
    public static final Vec3d originTopRight = new Vec3d(8886, 65, -7409);

    public static boolean withinArea = false;
    public Vec2f target;
    public static Vec3d arrow;

    public static class Config extends ModuleConfig {
        @SerialEntry
        public boolean removeItemFrameNames = true;
    }

    private WorldMapModule(MinecraftClient client) {
        super("world_map", 10, Config.class);

        HudElementRegistry.addFirst(
            Identifier.of("openutils", "world_map"),
            this::renderHud
        );

        WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
            if (config.moduleEnabled && withinArea && arrow != null) {
                MatrixStack matrixStack = context.matrixStack();
                VertexConsumerProvider consumer = context.consumers();
                if (matrixStack != null && consumer != null) {
                    renderTextureAtBlock(matrixStack, consumer, context.camera().getPos(), 2, Identifier.of("openutils", "textures/gui/map_pins/red.png"), arrow, 128, 128, false, 1);
                }
            }
            if (config.moduleEnabled && withinArea) {
                MatrixStack matrixStack = context.matrixStack();
                VertexConsumerProvider consumer = context.consumers();

                BannerManager.Banner candidateBanner = null;
                double closestCandidateDist = Double.MAX_VALUE;
                float transparentDistance = 1.5f;
                float maxNameDistance = 10f;

                Vec3d cameraPos = context.camera().getPos();
                Vec3d lookEnd = cameraPos.add(client.player.getRotationVec(1.0f).normalize().multiply(maxNameDistance));

                for (BannerManager.Banner banner : banners) {
                    if (matrixStack == null || consumer == null || banner.worldMapLocation().equals(arrow)) continue;

                    Vec3d bannerCenter = banner.worldMapLocation().add(0.0, 0.5, 0.0);
                    double dist = cameraPos.distanceTo(bannerCenter);

                    boolean semiTransparent = dist < transparentDistance;

                    Identifier texture = BannerManager.getBanner(banner.color());
                    renderTextureAtBlock(matrixStack, consumer, cameraPos, 0.75f, texture, banner.worldMapLocation(), 128, 128, semiTransparent, 0.5f);

                    if (dist <= maxNameDistance) {
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
                    Vec3d bannerCenter = candidateBanner.worldMapLocation().add(0.0, 0.5, 0.0);
                    boolean semiTransparent = cameraPos.distanceTo(bannerCenter) < transparentDistance;

                    Vec3d namePos = candidateBanner.worldMapLocation().add(0.0, 1.0, 0.0);
                    renderNameTag(client, matrixStack, consumer, cameraPos, namePos, candidateBanner.name(), LightmapTextureManager.MAX_LIGHT_COORDINATE, semiTransparent, 0.5f);
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var mapidCommand = ClientCommandManager.literal("mapid")
                    .executes(context -> executeMapId(client));

            var namespacedCommand = ClientCommandManager.literal("openutils:mapid")
                    .executes(context -> executeMapId(client));

            dispatcher.register(mapidCommand);
            dispatcher.register(namespacedCommand);
        });
    }

    public boolean rayIntersectsBox(Vec3d start, Vec3d end, Vec3d min, Vec3d max) {
        double tmin = (min.x - start.x) / (end.x - start.x);
        double tmax = (max.x - start.x) / (end.x - start.x);
        if (tmin > tmax) { double tmp = tmin; tmin = tmax; tmax = tmp; }

        double tymin = (min.y - start.y) / (end.y - start.y);
        double tymax = (max.y - start.y) / (end.y - start.y);
        if (tymin > tymax) { double tmp = tymin; tymin = tymax; tymax = tmp; }

        if ((tmin > tymax) || (tymin > tmax)) return false;
        if (tymin > tmin) tmin = tymin;
        if (tymax < tmax) tmax = tymax;

        double tzmin = (min.z - start.z) / (end.z - start.z);
        double tzmax = (max.z - start.z) / (end.z - start.z);
        if (tzmin > tzmax) { double tmp = tzmin; tzmin = tzmax; tzmax = tmp; }

        return !(tmin > tzmax || tzmin > tmax);
    }

    private int executeMapId(MinecraftClient client) {
        if (config.moduleEnabled && SubserverManager.getActiveSubServer().equals(SubserverManager.Subserver.SURVIVAL_PLOT) && client.player != null && client.world != null) {
            double worldSizeHalf = 10000;
            double mapArtSize = 512;
            int minTile = (int) Math.floor((-worldSizeHalf + 64) / mapArtSize);
            int maxTile = (int) Math.floor((worldSizeHalf + 64) / mapArtSize);

            Vec3d location = client.player.getPos();

            int tileX = (int) Math.floor((location.x + 64) / mapArtSize);
            int tileZ = (int) Math.floor((location.z + 64) / mapArtSize);

            int mapIdX = tileX - minTile;
            int mapIdZ = maxTile - tileZ;

            client.player.sendMessage(Text.of("§7Du befinner dig just nu i Map ID: §e§l" + mapIdX + ", " + mapIdZ + "§7!"), false);

            return 1;
        }
        return 0;
    }

    @Override
    public void tick(MinecraftClient client) {
        if (config.moduleEnabled) {
            if (client.player != null && client.world != null) {
                if (withinWorldMap(client.player.getPos())) {
                    if (!withinArea) {
                        BannerManager.fetchBanners();
                    }
                    withinArea = true;

                    if (client.currentScreen == null) {
                        boolean ctrlDown = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL);
                        boolean fDown = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_F);

                        if (ctrlDown && fDown) {
                            client.setScreen(new SearchScreen((location) -> arrow = location));
                        }
                    }

                    HitResult hit = client.player.raycast(10, 0.0F, false);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        if (hit.getPos().y == 58d) {
                            BlockHitResult blockHit = (BlockHitResult) hit;
                            Vec3d pos = blockHit.getPos();

                            int x = -10304 + (int) Math.floor((pos.getX() - originBottomLeftMap.x) * 512);
                            int z = 9664 + (int) Math.floor((pos.getZ() - originBottomLeftMap.z) * 512);
                            target = new Vec2f(x, z);
                            return;
                        }
                    }
                    target = null;
                    return;
                }
            }
        }
        target = null;
        withinArea = false;
    }

    public void renderHud(DrawContext context, RenderTickCounter renderTickCounter) {
        if (!withinArea) return;
        MinecraftClient client = MinecraftClient.getInstance();
        boolean aboveMap = target != null;
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int blockHeight = 10 + (aboveMap ? 15 : 0);
        List<Integer> widths = new ArrayList<>();
        
        String line1 = "";
        String line2 = "";
        String line3 = "CTRL+F för att hitta position.";
        widths.add(client.textRenderer.getWidth(line3));
        int textWidth;

        if (aboveMap) {
            line1 = "X: " + (int)target.x + " | Y: " + (int)target.y;
            line2 = "Map ID: " + (int)(Math.floor((10304 + target.x) / 512)) + ", " + (int)Math.floor(1 + -(-9664 + target.y) / 512);
            widths.add(client.textRenderer.getWidth(line1) + 12);
            widths.add(client.textRenderer.getWidth(line2) + 12);
        }

        textWidth = Collections.max(widths);
        context.fill(width / 2 - textWidth / 2 - 5, height - 50 - blockHeight, width / 2 + textWidth / 2 + 5, height - 50, 0x80000000);

        if (aboveMap) {
            context.drawCenteredTextWithShadow(client.textRenderer, line1, width / 2 + 4, height - 50 - blockHeight + 2, 0xFFFFFFFF);
        }
        
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of("openutils", "icon.png"), width / 2 - textWidth / 2 - 3, height - 50 - blockHeight + (aboveMap ? 1 : 0), 0, 0, 10, 10, 10, 10);

        Matrix3x2fStack matrixStack = context.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.scale(0.5f, 0.5f);
        if (target != null) {
            context.drawCenteredTextWithShadow(client.textRenderer, line2, (width / 2 + 4) * 2, (height - 50 - blockHeight + 11 + 2 ) * 2, 0xFFc4c4c4);
        }
        context.drawCenteredTextWithShadow(client.textRenderer, line3, width / 2 * 2, (height - 50 + 4) * 2 - 20, 0xFF878787);
        matrixStack.popMatrix();
    }

    public static void renderTextureAtBlock(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Vec3d camPos, float scale, Identifier texture, Vec3d position, int texWidth, int texHeight, boolean semiTransparent, float transparency) {
        matrices.push();

        float aspect = (float) texHeight / (float) texWidth;
        float heightWorld = aspect * scale;
        float halfW = scale * 0.5f;
        float halfH = heightWorld * 0.5f;

        float cameraYaw = MinecraftClient.getInstance().gameRenderer.getCamera().getYaw();
        double yawRad = Math.toRadians(cameraYaw);

        Vec3d right = new Vec3d(Math.cos(yawRad), 0.0, Math.sin(yawRad)).normalize();
        Vec3d up = new Vec3d(0.0, 1.0, 0.0);

        Vec3d center = new Vec3d(position.getX(), position.getY() + halfH, position.getZ());

        Vec3d topLeft  = center.subtract(right.multiply(halfW)).add(up.multiply(halfH));
        Vec3d topRight = center.add(right.multiply(halfW)).add(up.multiply(halfH));
        Vec3d botRight = center.add(right.multiply(halfW)).subtract(up.multiply(halfH));
        Vec3d botLeft  = center.subtract(right.multiply(halfW)).subtract(up.multiply(halfH));

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
        int light = 0xF000F0;

        int alpha = semiTransparent ? (int)(255 * transparency) : 255;

        buffer.vertex(matrix, (float)(topLeft.x - camPos.x), (float)(topLeft.y - camPos.y), (float)(topLeft.z - camPos.z))
                .color(255, 255, 255, alpha).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0f, 1f, 0f);
        buffer.vertex(matrix, (float)(topRight.x - camPos.x), (float)(topRight.y - camPos.y), (float)(topRight.z - camPos.z))
                .color(255, 255, 255, alpha).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0f, 1f, 0f);
        buffer.vertex(matrix, (float)(botRight.x - camPos.x), (float)(botRight.y - camPos.y), (float)(botRight.z - camPos.z))
                .color(255, 255, 255, alpha).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0f, 1f, 0f);
        buffer.vertex(matrix, (float)(botLeft.x - camPos.x), (float)(botLeft.y - camPos.y), (float)(botLeft.z - camPos.z))
                .color(255, 255, 255, alpha).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0f, 1f, 0f);

        matrices.pop();
    }


    public static void renderNameTag(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Vec3d camPos, Vec3d position, String text, int light, boolean semiTransparent, float transparency) {
        TextRenderer textRenderer = client.textRenderer;
        if (client.player == null) return;

        matrices.push();
        matrices.translate(position.x - camPos.x, position.y - camPos.y, position.z - camPos.z);

        float cameraYaw = client.gameRenderer.getCamera().getYaw();
        matrices.multiply(new Quaternionf().rotateY((float)Math.toRadians(-cameraYaw)));

        float scale = 0.025f;
        matrices.scale(-scale, -scale, scale);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float xOffset = -textRenderer.getWidth(text) / 2.0f;

        int baseBackgroundAlpha = (int)(MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F);
        int backgroundAlpha = semiTransparent ? (int)(baseBackgroundAlpha * transparency) : baseBackgroundAlpha;
        int backgroundColor = backgroundAlpha << 24;

        int textAlpha = semiTransparent ? (int)(255 * transparency) : 255;
        int textColor = (textAlpha << 24) | 0xFFFFFF;

        textRenderer.draw(text, xOffset, 0, textColor, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, backgroundColor, light);
        textRenderer.draw(text, xOffset, 0, textColor, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.applyEmission(light, 2));

        matrices.pop();
    }

    @Override
    protected Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public void loadConfig(ConfigCategory.Builder category) {
        category.group(OptionGroup.createBuilder()
                .name(Text.of("/warp Worldmap"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Använd Modul"))
                        .description(OptionDescription.of(Text.of("Slå på eller av worldmap tools.")))
                        .binding(true, () -> config.moduleEnabled, val -> config.moduleEnabled = val)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.of("Ta Bort ItemFrame Namn"))
                        .description(OptionDescription.of(Text.of("Om alla item frame namn i Worldmap ska tas bort (left alt så syns de ändå).")))
                        .binding(true, () -> config.removeItemFrameNames, val -> config.removeItemFrameNames = val)
                        .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                        .build())
                .build());
    }

    public Config getConfig() {
        return config;
    }

    public static boolean withinWorldMap(Vec3d position) {
        if (SubserverManager.getActiveSubServer().equals(SubserverManager.Subserver.SURVIVAL_PLOT)) {
            double x = position.x;
            double y = position.y;
            double z = position.z;

            return x >= originBottomLeft.x && x <= originTopRight.x
                    && y >= originBottomLeft.y && y <= originTopRight.y
                    && z >= originTopRight.z && z <= originBottomLeft.z;
        }
        return false;
    }
}
