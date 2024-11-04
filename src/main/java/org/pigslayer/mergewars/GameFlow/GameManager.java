package org.pigslayer.mergewars.GameFlow;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.pigslayer.mergewars.GameFlow.GamePhases.SetupManager;
import org.pigslayer.mergewars.GameFlow.Team.Team;

import java.util.*;


public class GameManager {
    private static class VoidGenerator extends ChunkGenerator {
    }

    private static GameManager instance;

    private List<Team> activeTeams = new ArrayList<>();

    public static void initialize(){
        instance = new GameManager();
    }

    public static void startGame(HashMap<String,List<UUID>> teams){
        instance.handleFullStart(teams);
    }

    private void handleFullStart(HashMap<String,List<UUID>> teams){
        WorldCreator creator = new WorldCreator("TestWorld");
        creator.generator(new VoidGenerator());
        World wld = creator.createWorld();

        int offset = 0;

        for(Map.Entry<String,List<UUID>> entry:teams.entrySet()){
            List<Player> players = entry.getValue().stream().map(Bukkit::getPlayer).toList();
            activeTeams.add(new Team(entry.getKey(),players,offset,wld));
            offset += 200;
        }

        SetupManager.runSetup(activeTeams);
    }

    public static List<Team> getActiveTeams() {
        return instance.activeTeams;
    }
}
