package net.powato.lifesteal.world;

import net.minecraft.nbt.NbtList;
import net.minecraft.world.PersistentState;

public class SafeChunkClass extends PersistentState {
    private static final String KEY = "SafeChunkClass";
    private final NbtList ChunkList = new NbtList();

    public SafeChunkClass() {
        super();
    }


}
