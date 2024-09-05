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
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (player != null) {
				String currentPlayerName = player.getName().getString();
				if (!playerName.equals(currentPlayerName)) {
					playerName = currentPlayerName;
					createMusicSocket();
				}
			}
			if (musicSocket != null) {
//				LOGGER.info(String.valueOf(client.options.getSoundVolume(SoundCategory.RECORDS) * client.options.getSoundVolume(SoundCategory.MASTER)));
				double mbVolume = client.options.getSoundVolume(SoundCategory.RECORDS) * client.options.getSoundVolume(SoundCategory.MASTER);
				if (mbVolume != oldMBVolume) {
					musicSocket.set_volume(mbVolume);
					oldMBVolume = mbVolume;
				}
			}

			int minutes = Calendar.getInstance().get(Calendar.MINUTE);
			if (minutes == 0 || minutes == 20 || minutes == 40) {
				if (!remindedForToday) {
					remindedForToday = true;
					client.getToastManager().add(
							new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
									Text.literal("Marknadsgränsen har återställts!"),
									Text.literal("Du kan nu sälja igen!")
							)
					);
					player.playSound(SoundEvents.BLOCK_BELL_USE);
				}
			} else {
				remindedForToday = false;
			}


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

			LiteralArgumentBuilder<FabricClientCommandSource> setVolume = ClientCommandManager.literal("set_volume")
					.executes(createFeedbackExecutor("set_volume"))
					.then(ClientCommandManager.argument("volume", DoubleArgumentType.doubleArg())
							.executes(context -> {
								double volume = DoubleArgumentType.getDouble(context, "volume");
								MinecraftClient client = MinecraftClient.getInstance();
								client.send(() -> musicSocket.set_volume(volume));
								return 1;
							})
					);
			LiteralCommandNode<FabricClientCommandSource> setVolumeCommand = dispatcher.register(setVolume);
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
		if (client.player.networkHandler != null) {
			return client.getNetworkHandler().getPlayerList().stream()
					.map(player -> player.getProfile().getName())
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}