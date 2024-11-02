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

    public Chunk[] getChunks(World world){

        if(world==null)
            throw new RuntimeException("Error generating world");

        Random random = new Random();
        int x = random.nextInt(-1,1);
        int z = random.nextInt(-1,1);

        List<Chunk> chunks = new ArrayList<>();

        for(int i = 0;i<8;i++){
            for(int j = 0;j<8;j++){
                int curx = x + (16*i);
                int curz = z + (16*j);
                chunks.add(world.getChunkAt(curx >> 4,curz >> 4));
            }
        }

        return chunks.toArray(new Chunk[0]);
    }

    private void recalculateLighting(Chunk chunk){
        CraftChunk craftChunk = (CraftChunk) chunk;
        ((CraftWorld) craftChunk.getWorld()).getHandle();
        IChunkAccess access = craftChunk.getHandle(ChunkStatus.f);
        for(Player p:Bukkit.getOnlinePlayers()){
            PacketContainer container = new PacketContainer(PacketType.Play.Server.LIGHT_UPDATE);
            container.getIntegers().write(0, chunk.getX());
            container.getIntegers().write(1, chunk.getZ());
            MergeWars.getProtocolManager().sendServerPacket(p, container);
        }
    }
}
