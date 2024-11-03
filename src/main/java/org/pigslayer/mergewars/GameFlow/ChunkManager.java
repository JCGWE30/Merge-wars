package org.pigslayer.mergewars.GameFlow;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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
    public static class ChunkBlock{
        public int localX;
        public int localY;
        public int localZ;
        public ChunkBlock(Block block) {
            this.localX = block.getX() % 16;
            this.localY = block.getY();
            this.localZ = block.getZ() % 16;
        }
        public Block getBlock(Chunk chunk) {
            return chunk.getBlock(localX,localY,localZ);
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

    public static final Map<UUID, Integer> highPoints = new ConcurrentHashMap<>();
    public static final Map<UUID, ChunkBlock[]> surfaceMap = new ConcurrentHashMap<>();
    public static final Map<Chunk, UUID> chunkMap = new ConcurrentHashMap<>();

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
        UUID id = UUID.randomUUID();
        highPoints.put(id, ChunkUtils.getHighestLocation(chunk));
        surfaceMap.put(id, ChunkUtils.skimTop(chunk));
        chunkMap.put(chunk, id);
        System.out.println("Processing complete on chunk "+atomic.getAndIncrement());
    }

    public static int getHighestY(UUID chunkId){
        return highPoints.get(chunkId);
    }

    public static Block[] getSurfaceMap(UUID sourceChunk,Chunk chunk){
        return Arrays.stream(surfaceMap.get(sourceChunk))
                .map(c->c.getBlock(chunk))
                .toArray(Block[]::new);
    }

    public static UUID getUUID(Chunk chunk) {
        return chunkMap.get(chunk);
    }
}
