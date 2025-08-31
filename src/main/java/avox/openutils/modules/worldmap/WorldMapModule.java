package avox.openutils.modules.worldmap;

import avox.openutils.Module;
import avox.openutils.SubserverManager;
import avox.openutils.modules.stock.StockModule;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static avox.openutils.OpenUtils.LOGGER;
import static avox.openutils.OpenUtils.playerInSurvival;

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
                    renderTextureAtBlock(matrixStack, consumer, context.camera().getPos(), Identifier.of("openutils", "textures/gui/map_pin.png"), arrow);
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

    public static void renderTextureAtBlock(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Vec3d camPos, Identifier texture, Vec3d position) {
        matrices.push();
        matrices.translate(position.getX() - camPos.x, position.getY() + 1.1 - camPos.y, position.getZ() - camPos.z);

        float cameraYaw = MinecraftClient.getInstance().gameRenderer.getCamera().getYaw();
        matrices.multiply(new Quaternionf().rotateY((float) Math.toRadians(-cameraYaw)));
        matrices.scale(2, 2, 2);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));

        buffer.vertex(matrix, -0.5f, 0.5f, 0).color(255,255,255,255).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(0f, 1f, 0f);
        buffer.vertex(matrix,  0.5f, 0.5f, 0).color(255,255,255,255).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(0f, 1f, 0f);
        buffer.vertex(matrix,  0.5f,-0.5f, 0).color(255,255,255,255).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(0f, 1f, 0f);
        buffer.vertex(matrix, -0.5f,-0.5f, 0).color(255,255,255,255).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(0f, 1f, 0f);

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
