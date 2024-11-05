package org.pigslayer.mergewars.GameFlow.Team;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_21_R1.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.pigslayer.mergewars.GameFlow.ChunkManager;
import org.pigslayer.mergewars.MergeWars;
import org.pigslayer.mergewars.Utils.BlockUtils;
import org.pigslayer.mergewars.Utils.ChunkUtils;

import java.util.*;

public class LandMass {
    private final Chunk[] sourceChunks;
    private HashMap<Chunk, UUID> chunks = new HashMap<>();
    private World world;
    private int offSet;
    private double[] center = new double[2];
    private double[] corner1 = new double[2];
    private double[] corner2 = new double[2];
    private int barrierHeight;

    public LandMass(Chunk[] chunks, World world, int offSet) {
        this.sourceChunks = chunks;
        this.world = world;
        this.offSet = offSet;
        setupLandMass();
        calculateCenter();
    }

    private static boolean canRun = true;

    public void setCeilingState(boolean state){
        if(!state){
            if(!canRun) return;
            canRun = false;
        }
        System.out.println("Setting mass state to "+state);
        Vector vector1 = new Vector(corner1[0], barrierHeight, corner1[1]);
        Vector vector2 = new Vector(corner2[0], barrierHeight, corner2[1]);


        Iterator<Block> blocks = BlockUtils.iterateBlocks(vector1,vector2,world);

        while(blocks.hasNext()){
            Block block = blocks.next();
            Material mat = state ? Material.BARRIER : Material.STONE;
            block.setBlockData(mat.createBlockData());
        }
    }

    public void enterMass(Player player, boolean defaultPlace){
        if(defaultPlace){
            Location location = new Location(world, center[0], barrierHeight+3, center[1]);
            player.teleport(location);
        }

        for(Chunk c:chunks.keySet()){
            PacketContainer lightPacket = new PacketContainer(PacketType.Play.Server.LIGHT_UPDATE);
            lightPacket.getIntegers().write(0, c.getX())
                    .write(1, c.getZ());

            MergeWars.getProtocolManager().sendServerPacket(player, lightPacket);
        }

        PacketContainer centerPacket = new PacketContainer(PacketType.Play.Server.SET_BORDER_CENTER);
        centerPacket.getDoubles().write(0, center[0]).write(1, center[1]);

        PacketContainer sizePacket = new PacketContainer(PacketType.Play.Server.SET_BORDER_SIZE);
        sizePacket.getDoubles().write(0,128.0);

        MergeWars.getProtocolManager().sendServerPacket(player, sizePacket);
        MergeWars.getProtocolManager().sendServerPacket(player, centerPacket);
    }

    private void setupLandMass(){
        for(Chunk chunk : sourceChunks){
            Chunk recipient = world.getChunkAt(chunk.getX()+offSet, chunk.getZ());
            chunks.put(recipient,ChunkManager.getUUID(chunk));
            pasteChunk(chunk, recipient);
        }
    }

    private void calculateCenter(){
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (Map.Entry<Chunk,UUID> entry : chunks.entrySet()) {
            Chunk chunk = entry.getKey();
            UUID id = entry.getValue();

            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();

            minX = Math.min(minX, chunkX);
            maxX = Math.max(maxX, chunkX);
            minZ = Math.min(minZ, chunkZ);
            maxZ = Math.max(maxZ, chunkZ);
            barrierHeight = Math.max(ChunkManager.getHighestY(id),barrierHeight);
        }

        barrierHeight+=50;

        corner1[0] = minX*16;
        corner1[1] = minZ*16;
        corner2[0] = (maxX*16)+15;
        corner2[1] = (maxZ*16)+15;

        double centerChunkX = (minX + maxX + 1) / 2.0;
        double centerChunkZ = (minZ + maxZ + 1) / 2.0;

        double centerBlockX = centerChunkX * 16;
        double centerBlockZ = centerChunkZ * 16;

        center = new double[] { centerBlockX, centerBlockZ };
    }

    private void pasteChunk(Chunk donor, Chunk recipient){
        CraftChunk fromCC = (CraftChunk) donor;
        CraftChunk toCC = (CraftChunk) recipient;

        IChunkAccess access = toCC.getHandle(ChunkStatus.f);

        ChunkSection[] fromSection = fromCC.getHandle(ChunkStatus.f).d();
        ChunkSection[] toSection = access.d();

        System.arraycopy(fromSection, 0, toSection, 0, fromSection.length);
    }

    public void loadMass() {
        for(Chunk chunk : chunks.keySet()){
            chunk.load(false);
        }
    }

    public UUID[] getChunkIds(){
        return chunks.values().toArray(new UUID[0]);
    }

    public UUID getId(Chunk chunk){
        return chunks.get(chunk);
    }

    public boolean isInArea(Player player) {
        return player.getWorld()==world;
    }

    public Chunk getChunk(UUID newChunk) {
        for(Chunk chunk : chunks.keySet()){
            if(chunks.get(chunk).equals(newChunk)){
                return chunk;
            }
        }
        throw new RuntimeException("Error fetching chunk");
    }
}
