package org.pigslayer.mergewars.Utils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.pigslayer.mergewars.GameFlow.ChunkManager;

import java.util.ArrayList;
import java.util.List;
import static org.pigslayer.mergewars.GameFlow.ChunkManager.*;

public class ChunkUtils {

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

    public static ChunkBlock[] skimTop(Chunk chunk){
        int highest = chunk.getWorld().getMaxHeight();
        int lowest = chunk.getWorld().getMinHeight();
        List<ChunkBlock> surfaceBlocks = new ArrayList<>();

        for(int i = 0;i < 16; i++){
            for(int j = 0; j < 16; j++){
                for(int k = highest; k > lowest; k--){
                    Block block = chunk.getBlock(i,k,j);
                    if(!block.getType().isAir()){
                        surfaceBlocks.add(new ChunkBlock(block));
                        break;
                    }
                }
            }
        }

        return surfaceBlocks.toArray(new ChunkBlock[0]);
    }

    public static boolean matchChunk(Chunk chunk1,Chunk chunk2){
        return chunk1.getX() == chunk2.getX() && chunk1.getZ() == chunk2.getZ();
    }
}
