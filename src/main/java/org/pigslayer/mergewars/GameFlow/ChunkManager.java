package org.pigslayer.mergewars.GameFlow;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.pigslayer.mergewars.GameFlow.Team.ParrelelChunk;
import org.pigslayer.mergewars.Utils.ChunkUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkManager {
    public static class ChunkBlock{
        public int localX;
        public int localY;
        public int localZ;
        public ChunkBlock(Block block) {
            this.localX = block.getX() % 15;
            this.localY = block.getY();
            this.localZ = block.getZ() % 15;
        }
        public Block getBlock(ParrelelChunk chunk) {
            Chunk realChunk = chunk.getReal();
            return realChunk.getBlock(localX, localY, localZ);
        }
    }
    private static class OnlyPlains extends BiomeProvider{

        @Override
        public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
            return Biome.PLAINS;
        }

        @Override
        public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
            return Collections.singletonList(Biome.PLAINS);
        }
    }

    public static final Map<ParrelelChunk, Integer> highPoints = new ConcurrentHashMap<>();
    public static final Map<ParrelelChunk, ChunkBlock[]> surfaceMap = new ConcurrentHashMap<>();

    public static void cacheChunks(){
        WorldCreator chunkPallete = new WorldCreator("ChunkPallete");
        chunkPallete.biomeProvider(new OnlyPlains());

        World pallete = chunkPallete.createWorld();

        if(pallete == null) throw new RuntimeException("Error generating world");

        Random random = new Random();

        int xOffset = random.nextInt(10000,30000);
        int yOffset = random.nextInt(10000,30000);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);

        AtomicInteger atomic = new AtomicInteger(1);

        for(int i = 0;i<8;i++){
            for(int j = 0;j<8;j++){
                Chunk chunk = pallete.getChunkAt(xOffset+i,yOffset+j);
                chunk.load(true);
                executor.submit(()->processChunk(chunk,atomic));
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Processing Complete");
    }

    private static void processChunk(Chunk chunk,AtomicInteger atomic){
        ParrelelChunk parrelel = new ParrelelChunk(chunk);
        highPoints.put(parrelel, ChunkUtils.getHighestLocation(chunk));
        surfaceMap.put(parrelel, ChunkUtils.skimTop(chunk));
        System.out.println("Processing complete on chunk "+atomic.getAndIncrement());
    }

    public static int getHighestY(ParrelelChunk chunk){
        return highPoints.get(chunk.getOriginal());
    }

    public static Block[] getSurfaceMap(ParrelelChunk chunk){
        return Arrays.stream(surfaceMap.get(chunk.getOriginal()))
                .map(c->c.getBlock(chunk))
                .toArray(Block[]::new);
    }
}
