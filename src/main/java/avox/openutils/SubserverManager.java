package avox.openutils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class SubserverManager {
    public static boolean playerOn90gQopen = false;
    private static Subserver activeSubServer = Subserver.UNKNOWN;

    private static final List<Consumer<Subserver>> listeners = new ArrayList<>();

    public static void registerListener(Consumer<Subserver> listener) {
        listeners.add(listener);
    }

    public static Subserver getActiveSubServer() {
        return activeSubServer;
    }

    public static void setSubServer(Subserver newSubServer) {
        if (activeSubServer != newSubServer) {
            activeSubServer = newSubServer;
        }
    }

    public static void updateListeners() {
        for (Consumer<Subserver> listener : listeners) {
            listener.accept(activeSubServer);
        }
    }

    public static void detectSubserver(MinecraftClient client) {
        ServerInfo serverInfo = client.getCurrentServerEntry();
        if (serverInfo != null) {
            playerOn90gQopen = serverInfo.address.equalsIgnoreCase("90gqopen.se");
        } else {
            playerOn90gQopen = false;
        }
        setSubServer(Subserver.UNKNOWN);

        if (client.world == null) {
            updateListeners();
            return;
        }

        Scoreboard scoreboard = client.world.getScoreboard();
        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR));

        for (ScoreboardEntry entry : entries) {
            String lineContent = Team.decorateName(scoreboard.getScoreHolderTeam(entry.owner()), entry.name()).getString();

            if (lineContent.contains("Hub")) {
                setSubServer(Subserver.HUB);
            } else if (lineContent.contains("Creative")) {
                setSubServer(Subserver.CREATIVE);
            } else if (lineContent.contains("UHC") || lineContent.contains("Ultra Hardcore")) {
                setSubServer(Subserver.UHC);
            } else if (lineContent.contains("Parkour")) {
                setSubServer(Subserver.PARKOUR);
            } else if (lineContent.contains("Event")) {
                setSubServer(Subserver.EVENT);
            } else if (lineContent.contains("Survival")) {
                if (lineContent.contains("Ägare")) {
                    setSubServer(Subserver.SURVIVAL_PLOT);
                } else {
                    if (client.player == null) {
                        updateListeners();
                        return;
                    }
                    setSubServer(client.player.getGameMode() == GameMode.ADVENTURE ? Subserver.SURVIVAL_SPAWN : Subserver.SURVIVAL_RESOURCE);
                }
            }
        }
        updateListeners();
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
}
