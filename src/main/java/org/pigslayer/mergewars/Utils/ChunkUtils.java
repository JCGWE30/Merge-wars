package org.pigslayer.mergewars.Utils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ChunkUtils {

    public static short toChunkPosition(Location loc){
        int x = loc.getBlockX() & 0XF;
        int y = loc.getBlockY() & 0XFF;
        int z = loc.getBlockZ() & 0XF;

        return (short) (x << 16 | z << 8 | y);
    }

    public static Integer getHighestLocation(Chunk chunk){

        int highest = -Integer.MAX_VALUE;

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < chunk.getWorld().getMaxHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if(block.getType()== Material.AIR)
                        continue;
                    highest = Math.max(block.getLocation().getBlockY(), highest);
                }
            }
        }

        return highest;
    }

    public static Block[] skimTop(Chunk chunk){
        int highest = chunk.getWorld().getMaxHeight();
        int lowest = chunk.getWorld().getMinHeight();
        List<Block> surfaceBlocks = new ArrayList<>();

        for(int i = 0;i < 16; i++){
            for(int j = 0; j < 16; j++){
                for(int k = highest; k > lowest; k--){
                    Block block = chunk.getBlock(i,k,j);
                    if(!block.getType().isAir()){
                        surfaceBlocks.add(block);
                        break;
                    }
                }
            }
        }

        return surfaceBlocks.toArray(new Block[0]);
    }

    public static Chunk getChunkOffset(Chunk chunk,int offset){
        int x = chunk.getX() - offset;
        int z = chunk.getZ();

        return chunk.getWorld().getChunkAt(x, z);
    }
}
