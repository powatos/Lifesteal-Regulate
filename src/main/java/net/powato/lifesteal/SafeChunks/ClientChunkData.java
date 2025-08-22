package net.powato.lifesteal.SafeChunks;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.*;

public class ClientChunkData {
    private static final Map<RegistryKey<World>, Set<ChunkPos>> highlightedChunks = new HashMap<>();

    public static void setChunks(String ChunkData) {

        highlightedChunks.clear();

        if (ChunkData == "") {
            return;
        }

        String[] Chunks = ChunkData.substring(1).split("\\|");
        for (String Chunk : Chunks){
            String[] values = Chunk.split(",");
            
            RegistryKey<World> dimension = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(values[2])); // cast string
            int x = Integer.parseInt(values[0]);
            int y = Integer.parseInt(values[1]);

            highlightedChunks.computeIfAbsent(dimension, k -> new HashSet<>()).add(new ChunkPos(x, y));

        }

        System.out.println(highlightedChunks);
    }

    public static Set<ChunkPos> GetChunks(RegistryKey<World> dimension){
        return highlightedChunks.getOrDefault(dimension, Collections.emptySet());
    }

}
