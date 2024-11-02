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
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.pigslayer.mergewars.GameFlow.ChunkManager;
import org.pigslayer.mergewars.MergeWars;
import org.pigslayer.mergewars.Utils.BlockUtils;
import org.pigslayer.mergewars.Utils.ChunkUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LandMass {
    private final ParrelelChunk[] sourceChunks;
    private ParrelelChunk[] chunks;
    private World world;
    private int offSet;
    private double[] center = new double[2];
    private double[] corner1 = new double[2];
    private double[] corner2 = new double[2];
    private int barrierHeight;

    public LandMass(ParrelelChunk[] chunks, World world, int offSet) {
        this.sourceChunks = chunks;
        this.world = world;
        this.offSet = offSet;
        setupLandMass();
        calculateCenter();
    }

    public void setCeilingState(boolean state){
        Vector vector1 = new Vector(corner1[0], barrierHeight, corner1[1]);
        Vector vector2 = new Vector(corner2[0], barrierHeight, corner2[1]);


        Iterator<Block> blocks = BlockUtils.iterateBlocks(vector1,vector2,world);

        while(blocks.hasNext()){
            Block block = blocks.next();
            Material mat = state ? Material.BARRIER : Material.AIR;
            block.setType(mat);
            System.out.println("Setting "+block.getLocation()+" to "+mat);
        }
    }

    public void enterMass(Player player, boolean defaultPlace){
        if(defaultPlace){
            Location location = new Location(world, center[0], barrierHeight+3, center[1]);
            player.teleport(location);
        }

        for(ParrelelChunk p:chunks){
            Chunk c = p.getReal();
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
        List<Chunk> chunks = new ArrayList<>();
        List<ParrelelChunk> parrelelChunks = new ArrayList<>();
        for(ParrelelChunk pDonor : sourceChunks){
            Chunk donor = pDonor.getReal();
            Chunk recipient = world.getChunkAt(donor.getX()+offSet, donor.getZ());
            chunks.add(recipient);
            parrelelChunks.add(pasteChunk(pDonor, recipient));
        }
        this.chunks = parrelelChunks.toArray(new ParrelelChunk[0]);
    }

    private void calculateCenter(){
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (ParrelelChunk pChunk : chunks) {
            Chunk chunk = pChunk.getReal();
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();

            minX = Math.min(minX, chunkX);
            maxX = Math.max(maxX, chunkX);
            minZ = Math.min(minZ, chunkZ);
            maxZ = Math.max(maxZ, chunkZ);
            barrierHeight = Math.max(ChunkManager.getHighestY(pChunk),barrierHeight);
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

    private ParrelelChunk pasteChunk(ParrelelChunk donor, Chunk recipient){
        CraftChunk fromCC = (CraftChunk) donor.getReal();
        CraftChunk toCC = (CraftChunk) recipient;

        IChunkAccess access = toCC.getHandle(ChunkStatus.f);

        ChunkSection[] fromSection = fromCC.getHandle(ChunkStatus.f).d();
        ChunkSection[] toSection = access.d();

        System.arraycopy(fromSection, 0, toSection, 0, fromSection.length);

        return new ParrelelChunk(donor,recipient);
    }

    public void loadMass() {
        for(ParrelelChunk chunk:chunks){
            chunk.getReal().load(false);
        }
    }

    public World getWorld() {
        return world;
    }

    public int getBarrierHeight() {
        return barrierHeight;
    }
}
