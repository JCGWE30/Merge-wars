package org.pigslayer.mergewars.GameFlow;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_21_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.pigslayer.mergewars.GameFlow.Team.Team;
import org.pigslayer.mergewars.MergeWars;
import org.pigslayer.mergewars.Scoreboard.ScoreboardManager;

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
