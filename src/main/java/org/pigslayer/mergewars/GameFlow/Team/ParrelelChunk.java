package org.pigslayer.mergewars.GameFlow.Team;

import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParrelelChunk {
    private static List<ParrelelChunk> parrelelChunks = new ArrayList<ParrelelChunk>();

    private UUID chunkId;
    private Chunk chunk;
    private boolean isOriginal;

    public ParrelelChunk(Chunk chunk) {
        this.chunkId = UUID.randomUUID();
        this.chunk = chunk;
        this.isOriginal = true;
        parrelelChunks.add(this);
    }

    public ParrelelChunk(ParrelelChunk chunk,Chunk copy) {
        this.chunkId = chunk.chunkId;
        this.chunk = copy;
        this.isOriginal = false;
        parrelelChunks.add(this);
    }

    public boolean equals(ParrelelChunk chunk) {
        return chunkId.equals(chunk.chunkId);
    }

    public Chunk getReal() {
        return chunk;
    }

    public ParrelelChunk getOriginal() {
        return parrelelChunks.stream()
                .filter(c->c.chunkId == this.chunkId)
                .filter(c->c.isOriginal)
                .findFirst()
                .orElse(null);
    }

    public static ParrelelChunk convert(Chunk chunk) {
        return parrelelChunks.stream()
                .filter(c->match(c,chunk))
                .findFirst()
                .orElse(null);
    }

    private static boolean match(ParrelelChunk chunk,Chunk comparator){
        Chunk real = chunk.getReal();
        return real.getWorld() == comparator.getWorld() && real.getX() == comparator.getX() && real.getZ() == comparator.getZ();
    }

    @Override
    public String toString() {
        return "ParrelelChunk{id=" + chunkId + ", chunk=" + chunk.toString() + "}";
    }
}
