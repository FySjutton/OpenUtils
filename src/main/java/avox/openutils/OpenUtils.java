package avox.openutils;

import avox.openutils.config.ConfigSystem;
import avox.openutils.modules.AdvancementRemoverModule;
import avox.openutils.modules.MarketResetModule;
import avox.openutils.modules.quests.QuestModule;
import avox.openutils.modules.stats.StatsModule;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static avox.openutils.SubserverManager.*;

public class OpenUtils implements ModInitializer {
	public static final String MOD_ID = "openutils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(Text.translatable("openutils.keybinding_category").getString()));

	public static final ModuleManager moduleManager = new ModuleManager();
	public static final List<DelayedTask> taskQueue = new ArrayList<>();

	@Override
	public void onInitialize() {
		// Register standard modules
		moduleManager.registerModule(StatsModule.INSTANCE);
		moduleManager.registerModule(QuestModule.INSTANCE);
		moduleManager.registerModule(MarketResetModule.INSTANCE);
		moduleManager.registerModule(AdvancementRemoverModule.INSTANCE);
//		moduleManager.registerModule(WorldMapModule.INSTANCE);

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			// Load the config
			ConfigSystem.CONFIG.load();
			ConfigSystem.applyModuleConfigs();
		});

		// Automatic subserver detection
		taskQueue.add(new DelayedTask(20 * 30, () -> detectSubserver(MinecraftClient.getInstance()), 1));

		// Setup default listeners
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			moduleManager.tick(client);

			if (!taskQueue.isEmpty()) {
				List<DelayedTask> toRun = new ArrayList<>(taskQueue);
				for (DelayedTask task : toRun) {
					task.ticksLeft--;
					if (task.ticksLeft <= 0) {
						task.runnable.run();
						taskQueue.remove(task);

						if (task.specialAction == 1) {
							taskQueue.add(new DelayedTask(task));
						}
					}
				}
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			playerOn90gQopen = false;
			setSubServer(Subserver.UNKNOWN);
			updateListeners();
		});

		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> taskQueue.add(new DelayedTask(20, () -> detectSubserver(client))));
	}

	public static boolean playerInSurvival() {
		return getActiveSubServer().equals(Subserver.SURVIVAL_SPAWN) || getActiveSubServer().equals(Subserver.SURVIVAL_PLOT) || getActiveSubServer().equals(Subserver.SURVIVAL_RESOURCE);
	}

	public static class DelayedTask {
		public int totalTicks;
		public int ticksLeft;
		public int specialAction;
		public Runnable runnable;

		public DelayedTask(int ticks, Runnable runnable) {
			this(ticks, runnable, -1);
		}

		public DelayedTask(int ticks, Runnable runnable, int specialAction) {
			this.totalTicks = ticks;
			this.ticksLeft = ticks;
			this.runnable = runnable;
			this.specialAction = specialAction;
		}

		public DelayedTask(DelayedTask delayedTask) {
			this(delayedTask.totalTicks, delayedTask.runnable, delayedTask.specialAction);
		}
	}

	public static void addToast(MinecraftClient client, String title, String message) {
		client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of(title), Text.of(message)));
	}

	public static String getToggledString(String mainString, boolean enabled) {
		return String.format(mainString, enabled ? "påslagen" : "avstängd");
	}
}