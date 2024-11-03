package org.pigslayer.mergewars.Utils;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BlockUtils {

    public static Iterator<Block> iterateBlocks(Vector start, Vector end, World wld) {

        List<Block> blocks = new ArrayList<>();

        int startX = start.getBlockX();
        int startY = start.getBlockY();
        int startZ = start.getBlockZ();

        int endX = end.getBlockX();
        int endY = end.getBlockY();
        int endZ = end.getBlockZ();

        for(int x = startX; x <= endX; x++){
            for(int y = startY; y <= endY; y++){
                for(int z = startZ; z <= endZ; z++){
                    blocks.add(wld.getBlockAt(x,y,z));
                }
            }
        }

        return blocks.iterator();
    }

    public static int getHighestY(Block block, Set<Material> transparent){
        for(int i = block.getWorld().getMaxHeight(); i > 0; i--){
            Block newBlock = block.getWorld().getBlockAt(block.getX(),i,block.getZ());
            if(!newBlock.getType().isAir()&&!transparent.contains(newBlock.getType())){
                return i;
            }
        }
        throw new RuntimeException("Chunk is all AIR");
    }
}
