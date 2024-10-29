package open.openutils.informationScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class infoLines {
    public static LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> informationLines = new LinkedHashMap<>();

    static {
        LinkedHashMap<String, ArrayList<String>> INFO = new LinkedHashMap<>();
        INFO.put("uncategorized", new ArrayList<>(Arrays.asList("id", "uuid", "username", "gQmynt", "onlinetime", "last_online", "last_server", "rank", "friend_count", "playtime_privacy", "parkourservern_whitelist", "banned")));
        informationLines.put("INFO", INFO);

        LinkedHashMap<String, ArrayList<String>> SURVIVAL = new LinkedHashMap<>();
        SURVIVAL.put("uncategorized", new ArrayList<>(Arrays.asList("survival_money", "survival_experience", "survival_plot_claims", "survival_warp_slots", "survival_quests_completed", "survival_quest_streak", "survival_level")));
        informationLines.put("SURVIVAL", SURVIVAL);

        LinkedHashMap<String, ArrayList<String>> CREATIVE = new LinkedHashMap<>();
        CREATIVE.put("uncategorized", new ArrayList<>(List.of("creative_rank")));
        informationLines.put("CREATIVE", CREATIVE);

        LinkedHashMap<String, ArrayList<String>> MB = new LinkedHashMap<>();
        MB.put("uncategorized", new ArrayList<>(new ArrayList<>(Arrays.asList("mb_games_played", "mb_wins", "mb_win_percent", "mb_winstreak", "mb_points", "mb_hat"))));
        informationLines.put("MB", MB);

        LinkedHashMap<String, ArrayList<String>> UHC = new LinkedHashMap<>();
        UHC.put("uncategorized", new ArrayList<>(new ArrayList<>(Arrays.asList("uhc_points", "uhc_games_played", "uhc_wins", "uhc_win_percent", "uhc_kills", "uhc_deaths"))));
        informationLines.put("UHC", UHC);

        LinkedHashMap<String, ArrayList<String>> event = new LinkedHashMap<>();
        event.put("INFO", new ArrayList<>(Arrays.asList("event_wins", "total_win_percent", "gold", "gold_earned", "mvps", "participation", "party_invites", "lobby_visibility", "random_skin", "spectator_visibility", "lobby_parkour_time", "lobby_parkour_reward")));
        event.put("ANVIL", new ArrayList<>(Arrays.asList("anvil_games_played", "anvil_wins", "anvil_win_percent", "anvil_gold_earned")));
        event.put("BORDER RUNNERS", new ArrayList<>(Arrays.asList("border_runners_games_played", "border_runners_wins", "border_runners_win_percent", "border_runners_gold_earned", "border_runners_rounds_survived", "border_runners_powerups_used", "border_runners_most_rounds_survived")));
        event.put("DRAGONS", new ArrayList<>(Arrays.asList("dragons_games_played", "dragons_wins", "dragons_win_percent", "dragons_gold_earned", "dragons_arrows_shot", "dragons_arrows_hit", "dragons_leaps_used", "dragons_crates_destroyed")));
        event.put("INFECTION", new ArrayList<>(Arrays.asList("infection_games_played", "infection_wins", "infection_win_percent", "infection_gold_earned", "infection_alpha_games", "infection_infected_kills", "infection_survivor_kills", "infection_most_kills_infected", "infection_most_kills_survivor")));
        event.put("MAZE", new ArrayList<>(Arrays.asList("maze_games_played", "maze_wins", "maze_win_percent", "maze_gold_earned")));
        event.put("OITC", new ArrayList<>(Arrays.asList("oitc_games_played", "oitc_wins", "oitc_win_percent", "oitc_gold_earned", "oitc_melee_kills", "oitc_ranged_kills", "oitc_deaths", "oitc_arrows_shot", "oitc_highest_kill_streak", "oitc_longest_bow_kill")));
        event.put("PAINTBALL", new ArrayList<>(Arrays.asList("paintball_games_played", "paintball_wins", "paintball_win_percent", "paintball_gold_earned", "paintball_shots_fired", "paintball_shots_hit", "paintball_kills", "paintball_deaths", "paintball_powerups_used", "paintball_most_kills", "paintball_highest_kill_streak")));
        event.put("PARKOUR RUN", new ArrayList<>(Arrays.asList("parkour_games_played", "parkour_wins", "parkour_win_percent", "parkour_gold_earned", "parkour_rounds_survived", "parkour_most_rounds_survived")));
        event.put("RED ROVER", new ArrayList<>(Arrays.asList("red_rover_games_played", "red_rover_wins", "red_rover_win_percent", "red_rover_gold_earned", "red_rover_killer_games", "red_rover_rounds_survived", "red_rover_kills", "red_rover_dashes", "red_rover_most_rounds_survived")));
        event.put("SNOW FIGHT", new ArrayList<>(Arrays.asList("snow_fight_games_played", "snow_fight_wins", "snow_fight_win_percent", "snow_fight_gold_earned", "snow_fight_kills", "snow_fight_snowballs_thrown", "snow_fight_snowballs_hit")));
        event.put("SPLEEF", new ArrayList<>(Arrays.asList("spleef_games_played", "spleef_wins", "spleef_win_percent", "spleef_gold_earned", "spleef_blocks_broken", "spleef_snowballs_thrown", "spleef_most_blocks_broken")));
        event.put("SUMO", new ArrayList<>(Arrays.asList("sumo_games_played", "sumo_wins", "sumo_win_percent", "sumo_gold_earned", "sumo_kills", "sumo_most_kills")));
        event.put("SURVIVAL GAMES", new ArrayList<>(Arrays.asList("sg_games_played", "sg_wins", "sg_win_percent", "sg_gold_earned", "sg_kills", "sg_deaths", "sg_chests_looted", "sg_most_kills")));
        event.put("TNT RUN", new ArrayList<>(Arrays.asList("tnt_run_games_played", "tnt_run_wins", "tnt_run_win_percent", "tnt_run_gold_earned", "tnt_run_walked_over_blocks", "tnt_run_leaps_used", "tnt_run_most_blocks_broken")));
        informationLines.put("EVENT", event);
    }
}
