package avox.openutils;

import avox.openutils.config.ConfigSystem;
import avox.openutils.modules.MarketResetModule;
import avox.openutils.modules.quests.QuestModule;
import avox.openutils.modules.stats.StatsModule;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OpenUtils implements ModInitializer {
	public static final String MOD_ID = "openutils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ModuleManager moduleManager = new ModuleManager();

	public static boolean playerOn90gQopen = false;
	public static Subserver activeSubServer = Subserver.UNKNOWN;

	public static final List<DelayedTask> taskQueue = new ArrayList<>();

	@Override
	public void onInitialize() {
		// Register standard modules
		moduleManager.registerModule(StatsModule.INSTANCE);
		moduleManager.registerModule(QuestModule.INSTANCE);
		moduleManager.registerModule(MarketResetModule.INSTANCE);

		// Load the config
		ConfigSystem.CONFIG.load();
		ConfigSystem.applyModuleConfigs();

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
			activeSubServer = Subserver.UNKNOWN;
		});

		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
			taskQueue.add(new DelayedTask(20, () -> detectSubserver(client)));
		});
	}

	private void detectSubserver(MinecraftClient client) {
		// Detect if the player is playing on 90gQopen, note, any other IP than 90gqopen.se and the mod won't work.
		ServerInfo serverInfo = client.getCurrentServerEntry();
		if (serverInfo != null) {
			playerOn90gQopen = serverInfo.address.equalsIgnoreCase("90gqopen.se");
		} else {
			playerOn90gQopen = false;
		}
		activeSubServer = Subserver.UNKNOWN;

		// Goes through the scoreboard in order to determine what subserver the player is in, quite unstable :P
		if (client.world == null) return;
		Scoreboard scoreboard = client.world.getScoreboard();
		Collection<ScoreboardEntry> entries = client.world.getScoreboard().getScoreboardEntries(scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR));

		for (ScoreboardEntry entry : entries) {
			String lineContent = Team.decorateName(scoreboard.getScoreHolderTeam(entry.owner()), entry.name()).getString();

			if (lineContent.contains("Hub")) {
				activeSubServer = Subserver.HUB;
			} else if (lineContent.contains("Creative")) {
				activeSubServer = Subserver.CREATIVE;
			} else if (lineContent.contains("UHC") || lineContent.contains("Ultra Hardcore")) {
				activeSubServer = Subserver.UHC;
			} else if (lineContent.contains("Parkour")) {
				activeSubServer = Subserver.PARKOUR;
			} else if (lineContent.contains("Event")) {
				activeSubServer = Subserver.EVENT;
			} else if (lineContent.contains("Survival")) {
				if (lineContent.contains("Ägare")) {
					activeSubServer = Subserver.SURVIVAL_PLOT;
				} else {
					if (client.player == null) return;
					activeSubServer = client.player.getGameMode() == GameMode.ADVENTURE ? Subserver.SURVIVAL_SPAWN : Subserver.SURVIVAL_RESOURCE;
				}
			}
		}
	}

	public enum Subserver {
		UNKNOWN,
		SURVIVAL_SPAWN,
		SURVIVAL_PLOT,
		SURVIVAL_RESOURCE,
		PARKOUR,
		CREATIVE,
		HUB,
		EVENT,
		UHC
	}

	public static boolean playerInSurvival() {
		return activeSubServer.equals(Subserver.SURVIVAL_SPAWN) || activeSubServer.equals(Subserver.SURVIVAL_PLOT) || activeSubServer.equals(Subserver.SURVIVAL_RESOURCE);
	}

	public static class DelayedTask {
		int totalTicks;
		int ticksLeft;
		int specialAction;
		Runnable runnable;

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