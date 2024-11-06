package org.pigslayer.mergewars.GameFlow.Team;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_21_R1.block.data.CraftBlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.pigslayer.mergewars.GameFlow.ChunkManager;
import org.pigslayer.mergewars.MergeWars;
import org.pigslayer.mergewars.Utils.BlockUtils;

import java.io.*;
import java.lang.reflect.Field;
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

    public void setCeilingState(boolean state){
        System.out.println("Setting mass state to "+state);
        Vector vector1 = new Vector(corner1[0], barrierHeight, corner1[1]);
        Vector vector2 = new Vector(corner2[0], barrierHeight, corner2[1]);


        Iterator<Block> blocks = BlockUtils.iterateBlocks(vector1,vector2,world);

        while(blocks.hasNext()){
            Block block = blocks.next();
            Material mat = state ? Material.BARRIER : Material.AIR;
            block.setBlockData(mat.createBlockData());
        }
    }

    public void enterMass(Player player, boolean defaultPlace){
        if(defaultPlace){
            Location location = new Location(world, center[0], barrierHeight+3, center[1]);
            player.teleport(location);
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
        World world = donor.getWorld();
        CraftChunk fromCC = (CraftChunk) donor;
        CraftChunk toCC = (CraftChunk) recipient;

        ChunkAccess access = toCC.getHandle(ChunkStatus.FULL);

        LevelChunkSection[] fromSections = fromCC.getHandle(ChunkStatus.FULL).getSections();
        LevelChunkSection[] toSections = access.getSections();

        try {
            Field iField = ChunkSection.class.getDeclaredField("biomes");
            iField.setAccessible(true);

            for (int i = 0; i < fromSections.length; i++) {
                LevelChunkSection fromSection = fromSections[i];
                LevelChunkSection toSection = toSections[i];

                DataPaletteBlock<Holder<BiomeBase>> donorBiomes = (DataPaletteBlock<Holder<BiomeBase>>) iField.get(fromSection);
                iField.set(toSection, donorBiomes);
            }

            iField.setAccessible(false); // Set accessible to false after looping

        } catch (Exception e) {
            throw new RuntimeException("Failed to copy biomes", e);
        }

        int minHeight = world.getMinHeight();
        int maxHeight = world.getMaxHeight();

        for(int x = 0;x<16;x++){
            for(int z = 0;z<16;z++){
                for(int y = minHeight;y<maxHeight;y++){
                    CraftBlock newBlock = ((CraftBlock) recipient.getBlock(x, y, z));
                    newBlock.setBlockData(donor.getBlock(x,y,z).getBlockData(),false);
                }
            }
        }
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
