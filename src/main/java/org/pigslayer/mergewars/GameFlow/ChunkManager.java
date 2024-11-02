package org.pigslayer.mergewars.GameFlow;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.pigslayer.mergewars.Utils.ChunkUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkManager {
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

    public static final Map<Chunk, Integer> highPoints = new ConcurrentHashMap<>();
    public static final Map<Chunk, Block[]> surfaceMap = new ConcurrentHashMap<>();

    public static void cacheChunks(){
        WorldCreator chunkPallete = new WorldCreator("ChunkPallete");
        chunkPallete.biomeProvider(new OnlyPlains());

        World pallete = chunkPallete.createWorld();

        if(pallete == null) throw new RuntimeException("Error generating world");

        Random random = new Random();

        int xOffset = random.nextInt(-10000,10000);
        int yOffset = random.nextInt(-10000,10000);

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
        highPoints.put(chunk, ChunkUtils.getHighestLocation(chunk));
        surfaceMap.put(chunk, ChunkUtils.skimTop(chunk));
        System.out.println("Processing complete on chunk "+atomic.getAndIncrement());
    }

    public static int getHighestY(Chunk chunk){
        return getHighestY(chunk.getX(),chunk.getZ());
    }

    public static int getHighestY(int x,int z){
        Chunk chunk = highPoints.keySet().stream()
                .filter(c->c.getX()==x&&c.getZ()==z)
                .findAny()
                .orElse(null);
        if(chunk==null) return -1;
        return highPoints.get(chunk);
    }

    public static Block[] getSurfaceMap(Chunk query){
        int x = query.getX();
        int z = query.getZ();
        Chunk chunk = surfaceMap.keySet().stream()
                .filter(c->c.getX()==x&&c.getZ()==z)
                .findAny()
                .orElse(null);
        if(chunk==null) return null;
        return surfaceMap.get(chunk);
    }
}
