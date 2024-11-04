package org.pigslayer.mergewars.Utils;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.pigslayer.mergewars.GameFlow.ChunkManager;

import java.util.*;

public class ChunkNumberProjector {
    private static final byte[] xBytes = new byte[] {
            0x11,
            0x0A,
            0x04,
            0x0A,
            0x11,
    };
    private static final byte[][] numberBytes = new byte[][] {
            {0x02,0x06,0x02,0x02,0x07},
            {0x07,0x01,0x07,0x04,0x07},
            {0x07,0x01,0x07,0x01,0x07},
            {0x05,0x05,0x07,0x01,0x01}
    };
    private static final byte[] pointers = new byte[] {
            0x10,
            0x08,
            0x04,
            0x02,
            0x01
    };

    public static BlockState[] getWriting(Chunk chunk, int value, UUID originId){
        int startingX = 3;
        int startingZ = 6;

        List<BlockState> finalStates = new ArrayList<>();

        int y = ChunkManager.getHighestY(originId)+3;

        for(int i = 0; i < 5;i++){
            for(int j = 0; j < 5;j++){
                Block block = chunk.getBlock(i+startingX,y,j+startingZ);
                byte pointer = pointers[j];

                if((pointer & xBytes[i])>0x0){
                    BlockState state = block.getState();
                    state.setType(Material.OBSIDIAN);
                    finalStates.add(state);
                }
            }
        }

        startingX = 9;

        for(int i = 0; i < 3;i++){
            for(int j = 0; j < 5;j++){
                Block block = chunk.getBlock(i+startingX,y,j+startingZ);
                byte pointer = pointers[i+2];

                if((pointer & numberBytes[value-1][j])>0x0){
                    BlockState state = block.getState();
                    state.setType(Material.OBSIDIAN);
                    finalStates.add(state);
                }
            }
        }
        return finalStates.toArray(new BlockState[0]);
    }

    public static BlockState[] resetWriting(Chunk chunk,UUID originId){
        int startingX = 3;
        int startingZ = 6;
        int y = ChunkManager.getHighestY(originId)+3;

        List<BlockState> finalStates = new ArrayList<>();

        for(int i = 0; i < 12;i++){
            for(int j = 0; j < 5;j++){
                Block yPointer = chunk.getBlock(startingX+i,0,startingZ+j);
                Block block = chunk.getBlock(i+startingX,y,j+startingZ);

                BlockState state = block.getState();
                state.setType(Material.AIR);
                finalStates.add(state);
            }
        }
        return finalStates.toArray(new BlockState[0]);
    }
}
