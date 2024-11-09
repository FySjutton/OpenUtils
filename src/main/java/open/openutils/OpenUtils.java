package open.openutils;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.command.CommandSource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import open.openutils.informationScreen.InfoScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.stream.Collectors;

import static open.openutils.ConfigSystem.configFile;


public class OpenUtils implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("openutils");
	private MBSocket musicSocket;
	private Thread socketThread;

	private String playerName = "";
	private double oldMBVolume;

	private boolean remindedForToday = false;

	private void createMusicSocket() {
		LOGGER.info("Creating new MB socket.");
		if (musicSocket != null) {
			musicSocket.closeSocket();
		}
		if (socketThread != null) {
			socketThread.interrupt();
		}

		musicSocket = new MBSocket();
		socketThread = new Thread(() -> {
			musicSocket.setupSocket(playerName);
		});

		socketThread.start();
	}

	@Override
	public void onInitialize() {
		new ConfigSystem().checkConfig();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			try {
				ClientPlayerEntity player = client.player;
				if (player != null) {
					String currentPlayerName = player.getName().getString();
					if (!playerName.equals(currentPlayerName)) {
						playerName = currentPlayerName;
						createMusicSocket();
					}

					int minutes = Calendar.getInstance().get(Calendar.MINUTE);
					int change = configFile.getAsJsonObject().get("timer_change").getAsInt();
					if (minutes == getReminderTime(change, 0) || minutes == getReminderTime(change, 20) || minutes == getReminderTime(change, 40)) {
						if (!remindedForToday) {
							remindedForToday = true;
							if (configFile.getAsJsonObject().get("send_toast").getAsBoolean()) {
								client.getToastManager().add(
										new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
												Text.literal("Marknadsgränsen har återställts!"),
												Text.literal("Du kan nu sälja igen!")
										)
								);
							}
							if (configFile.getAsJsonObject().get("use_sound").getAsBoolean()) {
								player.playSound(SoundEvents.BLOCK_BELL_USE);
							}
						}
					} else {
						remindedForToday = false;
					}
				}
				if (musicSocket != null) {
					double mbVolume = client.options.getSoundVolume(SoundCategory.RECORDS) * client.options.getSoundVolume(SoundCategory.MASTER);
					if (mbVolume != oldMBVolume) {
						musicSocket.set_volume(mbVolume);
						oldMBVolume = mbVolume;
					}
				}
			} catch (Exception ignored) {}
		});

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> lookupCommand = ClientCommandManager.literal("lookup")
					.executes(createFeedbackExecutor("lookup"))
					.then(ClientCommandManager.argument("player", StringArgumentType.string())
						.suggests((context, builder) -> CommandSource.suggestMatching(getOnlinePlayerNames(), builder))
						.executes(context -> {
							String playerName = StringArgumentType.getString(context, "player");
							MinecraftClient client = MinecraftClient.getInstance();
							// When executing a command the current screen will automatically be closed (the chat hud), this delays the new screen to open, so it won't close instantly
							client.send(() -> {
								JsonElement info = new FetchInformation().fetchProfile(playerName);
								if (info != null) {
									MinecraftClient.getInstance().setScreen(new InfoScreen(info));
								}
							});
							return 1;
						})
					);
			LiteralCommandNode<FabricClientCommandSource> regLookupCommand = dispatcher.register(lookupCommand);

			registerAlias(dispatcher, "searchAPI", regLookupCommand);
			registerAlias(dispatcher, "openUtils:searchAPI", regLookupCommand);
			registerAlias(dispatcher, "openUtils:lookup", regLookupCommand);
		});
	}

	private void registerAlias(CommandDispatcher<FabricClientCommandSource> dispatcher, String alias, LiteralCommandNode<FabricClientCommandSource> targetCommand) {
		dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal(alias)
				.executes(createFeedbackExecutor(alias))
				.redirect(targetCommand));
	}

	private Command<FabricClientCommandSource> createFeedbackExecutor(String alias) {
		return context -> {
			context.getSource().sendFeedback(Text.of(Text.translatable("openutils.no_player").getString() + " §7/" + alias + " <player>"));
			return 1;
		};
	}

	private static Collection<String> getOnlinePlayerNames() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			if (client.player.networkHandler != null) {
				return client.player.networkHandler.getPlayerList().stream()
						.map(player -> player.getProfile().getName())
						.collect(Collectors.toList());
			}
		}
		return Collections.emptyList();
	}

	private int getReminderTime(int change, int normal) {
		if ((normal - change) < 0) {
			return 60 + (normal - change);
		}
		return normal - change;
	}
}